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

package org.kaaproject.kaa.server.sync;

import java.util.List;

public final class NotificationClientSync {
    private int topicListHash;
    private List<TopicState> topicStates;
    private List<String> acceptedUnicastNotifications;
    private List<SubscriptionCommand> subscriptionCommands;

    public NotificationClientSync() {
    }

    /**
     * All-args constructor.
     */
    public NotificationClientSync(int topicListHash,
            List<TopicState> topicStates,
            List<String> acceptedUnicastNotifications,
            List<SubscriptionCommand> subscriptionCommands) {
        this.topicListHash = topicListHash;
        this.topicStates = topicStates;
        this.acceptedUnicastNotifications = acceptedUnicastNotifications;
        this.subscriptionCommands = subscriptionCommands;
    }

    /**
     * Gets the value of the 'topicListHash' field.
     */
    public int getTopicListHash() {
        return topicListHash;
    }

    /**
     * Sets the value of the 'topicListHash' field.
     *
     * @param value
     *            the value to set.
     */
    public void setTopicListHash(Integer value) {
        this.topicListHash = value;
    }

    /**
     * Gets the value of the 'topicStates' field.
     */
    public List<TopicState> getTopicStates() {
        return topicStates;
    }

    /**
     * Sets the value of the 'topicStates' field.
     *
     * @param value
     *            the value to set.
     */
    public void setTopicStates(List<TopicState> value) {
        this.topicStates = value;
    }

    /**
     * Gets the value of the 'acceptedUnicastNotifications' field.
     */
    public List<String> getAcceptedUnicastNotifications() {
        return acceptedUnicastNotifications;
    }

    /**
     * Sets the value of the 'acceptedUnicastNotifications' field.
     *
     * @param value
     *            the value to set.
     */
    public void setAcceptedUnicastNotifications(List<String> value) {
        this.acceptedUnicastNotifications = value;
    }

    /**
     * Gets the value of the 'subscriptionCommands' field.
     */
    public List<SubscriptionCommand> getSubscriptionCommands() {
        return subscriptionCommands;
    }

    public void setSubscriptionCommands(List<SubscriptionCommand> subscriptionCommands) {
        this.subscriptionCommands = subscriptionCommands;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((acceptedUnicastNotifications == null) ? 0 : acceptedUnicastNotifications.hashCode());
        result = prime * result + ((subscriptionCommands == null) ? 0 : subscriptionCommands.hashCode());
        result = prime * result + topicListHash;
        result = prime * result + ((topicStates == null) ? 0 : topicStates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationClientSync other = (NotificationClientSync) obj;
        if (acceptedUnicastNotifications == null) {
            if (other.acceptedUnicastNotifications != null) {
                return false;
            }
        } else if (!acceptedUnicastNotifications.equals(other.acceptedUnicastNotifications)) {
            return false;
        }
        if (subscriptionCommands == null) {
            if (other.subscriptionCommands != null) {
                return false;
            }
        } else if (!subscriptionCommands.equals(other.subscriptionCommands)) {
            return false;
        }
        if (topicListHash != other.topicListHash) {
            return false;
        }
        if (topicStates == null) {
            if (other.topicStates != null) {
                return false;
            }
        } else if (!topicStates.equals(other.topicStates)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NotificationClientSync [topicListHash=");
        builder.append(topicListHash);
        builder.append(", topicStates=");
        builder.append(topicStates);
        builder.append(", acceptedUnicastNotifications=");
        builder.append(acceptedUnicastNotifications);
        builder.append(", subscriptionCommands=");
        builder.append(subscriptionCommands);
        builder.append("]");
        return builder.toString();
    }
}
