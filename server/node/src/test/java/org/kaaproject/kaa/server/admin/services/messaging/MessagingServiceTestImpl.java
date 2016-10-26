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

package org.kaaproject.kaa.server.admin.services.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingServiceTestImpl implements MessagingService {

  private static final Logger LOG = LoggerFactory.getLogger(MessagingServiceImpl.class);

  @Override
  public void configureMailSender() {
    LOG.info("[TEST]: Configure Mail Sender.");
  }

  @Override
  public void sendTempPassword(String username, String password, String email) throws Exception {
    LOG.info("[TEST]: Invoked sendTempPassword: [username: {}, password: {}, email: {}]", username, password, email);
  }

  @Override
  public void sendPasswordResetLink(String passwordResetHash, String username, String email) {
    LOG.info("[TEST]: Invoked sendPasswordResetLink: [passwordResetHash: {}, username: {}, email: {}]",
        passwordResetHash, username, email);
  }

  @Override
  public void sendPasswordAfterReset(String username, String password, String email) {
    LOG.info("[TEST]: Invoked sendPasswordAfterReset: [username: {}, password: {}, email: {}]", username, password, email);
  }
}
