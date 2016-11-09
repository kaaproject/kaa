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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.RedirectionTransport;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;

public class DefaultRedirectionTransport implements RedirectionTransport {

  private BootstrapManager manager;

  @Override
  public void setBootstrapManager(BootstrapManager manager) {
    this.manager = manager;
  }

  @Override
  public void onRedirectionResponse(RedirectSyncResponse response) {
    if (response != null && manager != null) {
      manager.useNextOperationsServerByAccessPointId(response.getAccessPointId());
    }
  }

}
