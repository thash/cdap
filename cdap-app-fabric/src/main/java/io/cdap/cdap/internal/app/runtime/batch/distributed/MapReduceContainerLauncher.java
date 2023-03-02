/*
 * Copyright © 2015-2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.cdap.internal.app.runtime.batch.distributed;

import io.cdap.cdap.common.app.MainClassLoader;
import io.cdap.cdap.common.lang.ClassLoaders;
import io.cdap.cdap.common.logging.common.UncaughtExceptionHandler;
import io.cdap.cdap.internal.app.runtime.batch.MapReduceClassLoader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * The class launches MR container (AM or Task). The {@link #launch(String, String[])}} method is
 * expected to be called by classes generated by {@link ContainerLauncherGenerator}.
 */
public class MapReduceContainerLauncher {

  private static final Logger LOG = LoggerFactory.getLogger(MapReduceContainerLauncher.class);

  /**
   * Launches the given main class. The main class will be loaded through the {@link
   * MapReduceClassLoader}.
   *
   * @param mainClassName the main class to launch
   * @param args arguments for the main class
   */
  @SuppressWarnings("unused")
  public static void launch(String mainClassName, String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    List<URL> urls = ClassLoaders.getClassLoaderURLs(systemClassLoader, new ArrayList<>());

    // Remove the URL that contains the given main classname to avoid infinite recursion.
    // This is needed because we generate a class with the same main classname in order to intercept the main()
    // method call from the container launch script.
    URL resource = systemClassLoader.getResource(mainClassName.replace('.', '/') + ".class");
    if (resource == null) {
      throw new IllegalStateException("Failed to find resource for main class " + mainClassName);
    }

    if (!urls.remove(ClassLoaders.getClassPathURL(mainClassName, resource))) {
      throw new IllegalStateException("Failed to remove main class resource " + resource);
    }

    // Create a MainClassLoader for dataset rewrite
    URL[] classLoaderUrls = urls.toArray(new URL[urls.size()]);
    ClassLoader mainClassLoader = new MainClassLoader(classLoaderUrls,
        systemClassLoader.getParent());

    // Install the JUL to SLF4J Bridge
    try {
      mainClassLoader.loadClass(SLF4JBridgeHandler.class.getName())
          .getDeclaredMethod("install")
          .invoke(null);
    } catch (Exception e) {
      // Log the error and continue
      LOG.warn("Failed to invoke SLF4JBridgeHandler.install() required for jul-to-slf4j bridge", e);
    }

    ClassLoaders.setContextClassLoader(mainClassLoader);

    // Creates the MapReduceClassLoader. It has to be loaded from the MainClassLoader.
    try {
      final ClassLoader classLoader = (ClassLoader) mainClassLoader.loadClass(
              MapReduceClassLoader.class.getName())
          .newInstance();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (classLoader instanceof AutoCloseable) {
            try {
              ((AutoCloseable) classLoader).close();
            } catch (Exception e) {
              System.err.println("Failed to close ClassLoader " + classLoader);
              e.printStackTrace();
            }
          }
        }
      });

      Thread.currentThread().setContextClassLoader(classLoader);

      // Setup logging and stdout/stderr redirect
      // Invoke MapReduceClassLoader.getTaskContextProvider()
      classLoader.getClass().getDeclaredMethod("getTaskContextProvider").invoke(classLoader);
      // Invoke StandardOutErrorRedirector.redirectToLogger()
      classLoader.loadClass("io.cdap.cdap.common.logging.StandardOutErrorRedirector")
          .getDeclaredMethod("redirectToLogger", String.class)
          .invoke(null, mainClassName);

      Class<?> mainClass = classLoader.loadClass(mainClassName);
      Method mainMethod = mainClass.getMethod("main", String[].class);
      mainMethod.setAccessible(true);

      LOG.info("Launch main class {}.main({})", mainClassName, Arrays.toString(args));
      mainMethod.invoke(null, new Object[]{args});
      LOG.info("Main method returned {}", mainClassName);
    } catch (Throwable t) {
      // print to System.err since the logger may not have been redirected yet
      System.err.println(
          String.format("Exception raised when calling %s.main(String[]) method", mainClassName));
      t.printStackTrace();
      throw t;
    }
  }
}
