/*
 * Copyright © 2017 Cask Data, Inc.
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

package io.cdap.cdap.internal.app.services;

import com.google.inject.Inject;
import io.cdap.cdap.app.runtime.ProgramRuntimeService;
import io.cdap.cdap.app.runtime.ProgramStateWriter;
import io.cdap.cdap.app.store.Store;
import io.cdap.cdap.common.conf.CConfiguration;
import io.cdap.cdap.common.namespace.NamespaceAdmin;
import io.cdap.cdap.data2.dataset2.DatasetFramework;

/**
 * A run record corrector that does not correct run records at all. Note that this still runs the
 * local dataset deleter that is started by its base class.
 */
public class NoopRunRecordCorrectorService extends RunRecordCorrectorService {

  @Inject
  NoopRunRecordCorrectorService(CConfiguration cConf, Store store,
      ProgramStateWriter programStateWriter,
      ProgramRuntimeService runtimeService, NamespaceAdmin namespaceAdmin,
      DatasetFramework datasetFramework) {
    super(cConf, store, programStateWriter, runtimeService, namespaceAdmin, datasetFramework);
  }
}
