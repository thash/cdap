/*
 * Copyright © 2014 Cask Data, Inc.
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
 * the License
 */

package co.cask.cdap.cli.command.lineage;

import co.cask.cdap.api.service.Service;
import co.cask.cdap.cli.ArgumentName;
import co.cask.cdap.cli.CLIConfig;
import co.cask.cdap.cli.util.AbstractCommand;
import co.cask.cdap.client.LineageClient;
import co.cask.cdap.proto.Id;
import co.cask.common.cli.Arguments;
import com.google.inject.Inject;

import java.io.PrintStream;

/**
 * Call an endpoint of a {@link Service}.
 */
public class GetStreamLineageCommand extends AbstractCommand {

  private final LineageClient client;

  @Inject
  public GetStreamLineageCommand(CLIConfig cliConfig, LineageClient client) {
    super(cliConfig);
    this.client = client;
  }

  @Override
  public void perform(Arguments arguments, PrintStream output) throws Exception {
    long currentTime = System.currentTimeMillis();
    Id.Stream stream = Id.Stream.from(cliConfig.getCurrentNamespace(), arguments.get(ArgumentName.STREAM.toString()));
    long start = getTimestamp(arguments.getOptional("start", "min"), currentTime);
    long end = getTimestamp(arguments.getOptional("end", "max"), currentTime);
    Integer levels = arguments.getIntOptional("levels", null);

    client.getLineage(stream, start, end, levels);
  }

  @Override
  public String getPattern() {
    return String.format("get lineage stream <%s> [start <start>] [end <end>] [levels <levels>]", ArgumentName.STREAM);
  }

  @Override
  public String getDescription() {
    return "Gets the lineage of a stream";
  }
}
