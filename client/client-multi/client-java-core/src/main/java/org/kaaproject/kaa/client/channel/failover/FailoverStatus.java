/*
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

package org.kaaproject.kaa.client.channel.failover;

/**
 * Enum which describes status of the current failover state. Managed by
 * a failover strategy
 */
public enum FailoverStatus {
  ENDPOINT_VERIFICATION_FAILED,
  ENDPOINT_CREDENTIALS_REVOKED,
  BOOTSTRAP_SERVERS_NA,
  CURRENT_BOOTSTRAP_SERVER_NA,
  OPERATION_SERVERS_NA,
  NO_OPERATION_SERVERS_RECEIVED,
  NO_CONNECTIVITY
}
