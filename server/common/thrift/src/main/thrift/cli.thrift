/**
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


include "shared.thrift"

namespace java org.kaaproject.kaa.server.common.thrift.gen.cli
namespace cpp kaa

struct MemoryUsage {
  1: shared.Long max
  2: shared.Long total
  3: shared.Long free
}

enum CommandStatus {
  OK = 1,
  ERROR = 2
}

struct CommandResult {
  1: CommandStatus status
  2: string message
}

exception CliThriftException {
   1: shared.Integer errorCode,
   2: string message
}

service CliThriftService {
    
    string serverName() throws(1: CliThriftException cliException)
    
    void shutdown() throws(1: CliThriftException cliException)
    
    MemoryUsage getMemoryUsage(1: bool forceGC) throws(1: CliThriftException cliException)
    
    CommandResult executeCommand(1: string commandLine) throws(1: CliThriftException cliException)
    
}