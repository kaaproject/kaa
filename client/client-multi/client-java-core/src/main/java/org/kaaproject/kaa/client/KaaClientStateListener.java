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

/**
 * Notifies about Kaa client state changes and errors.
 *
 * @author Andrew Shvayka
 */
public interface KaaClientStateListener {

  /**
   * On successful start of Kaa client. Kaa client is successfully connected
   * to Kaa cluster and is ready for usage.
   */
  void onStarted();

  /**
   * On failure during Kaa client startup. Typically failure is related to
   * network issues.
   *
   * @param exception - cause of failure
   */
  void onStartFailure(KaaException exception);

  /**
   * On successful pause of Kaa client. Kaa client is successfully paused
   * and does not consume any resources now.
   */
  void onPaused();

  /**
   * On failure during Kaa client pause. Typically related to
   * failure to free some resources.
   *
   * @param exception - cause of failure
   */
  void onPauseFailure(KaaException exception);

  /**
   * On successful resume of Kaa client. Kaa client is successfully connected
   * to Kaa cluster and is ready for usage.
   */
  void onResume();

  /**
   * On failure during Kaa client resume. Typically failure is related to
   * network issues.
   *
   * @param exception - cause of failure
   */
  void onResumeFailure(KaaException exception);

  /**
   * On successful stop of Kaa client. Kaa client is successfully stopped
   * and does not consume any resources now.
   */
  void onStopped();

  /**
   * On failure during Kaa client stop. Typically related to
   * failure to free some resources.
   *
   * @param exception - cause of failure
   */
  void onStopFailure(KaaException exception);

}
