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

package org.kaaproject.kaa.client;

import org.kaaproject.kaa.client.exceptions.KaaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleKaaClientStateListener implements KaaClientStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(KaaClientStateListener.class);

  @Override
  public void onStarted() {
    LOG.info("Kaa client started");
  }

  @Override
  public void onStartFailure(KaaException exception) {
    LOG.info("Kaa client startup failure", exception);
  }

  @Override
  public void onPaused() {
    LOG.info("Kaa client paused");
  }

  @Override
  public void onPauseFailure(KaaException exception) {
    LOG.info("Kaa client pause failure", exception);
  }

  @Override
  public void onResume() {
    LOG.info("Kaa client resumed");
  }

  @Override
  public void onResumeFailure(KaaException exception) {
    LOG.info("Kaa client resume failure", exception);
  }

  @Override
  public void onStopped() {
    LOG.info("Kaa client stopped");
  }

  @Override
  public void onStopFailure(KaaException exception) {
    LOG.info("Kaa client stop failure", exception);
  }

}
