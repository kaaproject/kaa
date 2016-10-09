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

package org.kaaproject.kaa.client.profile;

import org.kaaproject.kaa.client.channel.ProfileTransport;

import java.io.IOException;

/**
 * Default {@link ProfileManager} implementation.
 *
 * @author Yaroslav Zeygerman
 * @author Andrew Shvayka
 */
public class DefaultProfileManager implements ProfileManager {
  private final ProfileTransport transport;
  private final ProfileSerializer serializer = new ProfileSerializer();
  private ProfileContainer container;

  public DefaultProfileManager(ProfileTransport transport) {
    this.transport = transport;
  }

  @Override
  public void setProfileContainer(ProfileContainer container) {
    this.container = container;
  }

  @Override
  public byte[] getSerializedProfile() throws IOException {
    return serializer.toByteArray(container);
  }

  @Override
  public void updateProfile() {
    transport.sync();
  }

  @Override
  public boolean isInitialized() {
    return container != null || serializer.isDefault();
  }
}
