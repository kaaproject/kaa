/*
 * Copyright 2014 CyberVision, Inc.
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
include "cli.thrift"

namespace java org.kaaproject.kaa.server.common.thrift.gen.bootstrap

enum ThriftChannelType {
  HTTP = 1,
  HTTP_LP = 2
}

struct ThriftIpParameters {
  1: string HostName
  2: shared.Integer Port
}

typedef ThriftIpParameters ThriftHttpParameters
typedef ThriftIpParameters ThriftHttpLpParameters

union ThriftCommunicationParameters {
  1: ThriftHttpParameters httpParams
  2: ThriftHttpLpParameters httpLpParams
}

struct ThriftSupportedChannel {
  1: ThriftChannelType type
  2: ThriftCommunicationParameters communicationParams
}

typedef list<ThriftSupportedChannel> ThriftSupportedChannels

struct ThriftOperationsServer {
  1: string Name
  2: shared.Integer priority
  3: binary publicKey
  4: ThriftSupportedChannels supportedChannels 
}

typedef list<ThriftOperationsServer> serversList

service BootstrapThriftService extends cli.CliThriftService{

/**
*   Set Operations Servers List
*/
  void onOperationsServerListUpdate(1: serversList operationsServersList);
  
}
