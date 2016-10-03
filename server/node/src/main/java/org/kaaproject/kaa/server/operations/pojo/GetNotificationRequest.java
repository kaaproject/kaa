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

package org.kaaproject.kaa.server.operations.pojo;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.sync.SubscriptionCommand;
import org.kaaproject.kaa.server.sync.TopicState;

import java.util.List;

/**
 * The Class for modeling of delta response. It is used to communicate with
 * {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService
 * DeltaService}
 *
 * @author ashvayka
 */

public class GetNotificationRequest {

  private final int topicHash;

  private List<SubscriptionCommand> subscriptionCommands;

  private List<String> acceptedUnicastNotifications;

  private List<TopicState> topicStates;

  private EndpointProfileDto profile;

  /**
   * Instantiates a new gets the notification request.
   *
   * @param topicHash                    the topicHash
   * @param profile                      the profile
   * @param subscriptionCommands         the subscription commands
   * @param acceptedUnicastNotifications the accepted unicast notifications
   * @param topicStates                  the topic states
   */
  public GetNotificationRequest(int topicHash, EndpointProfileDto profile,
                                List<SubscriptionCommand> subscriptionCommands,
                                List<String> acceptedUnicastNotifications,
                                List<TopicState> topicStates) {
    super();
    this.topicHash = topicHash;
    this.profile = profile;
    this.subscriptionCommands = subscriptionCommands;
    this.acceptedUnicastNotifications = acceptedUnicastNotifications;
    this.topicStates = topicStates;
  }


  public int getTopicHash() {
    return topicHash;
  }


  public List<SubscriptionCommand> getSubscriptionCommands() {
    return subscriptionCommands;
  }

  public List<String> getAcceptedUnicastNotifications() {
    return acceptedUnicastNotifications;
  }


  public List<TopicState> getTopicStates() {
    return topicStates;
  }


  public EndpointProfileDto getProfile() {
    return profile;
  }
}
