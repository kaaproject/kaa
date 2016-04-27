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

public final class SubscriptionCommand {
    private String topicId;
    private SubscriptionCommandType command;

    public SubscriptionCommand() {
    }

    /**
     * All-args constructor.
     */
    public SubscriptionCommand(String topicId, SubscriptionCommandType command) {
        this.topicId = topicId;
        this.command = command;
    }

    /**
     * All-args constructor.
     */
    public SubscriptionCommand(Long topicId, SubscriptionCommandType command) {
        this.topicId = Long.toString(topicId);
        this.command = command;
    }

    /**
     * Gets the value of the 'topicId' field.
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Gets the value of the 'topicId' field.
     */
    public long getTopicIdAsLong() {
        return Long.parseLong(topicId);
    }

    /**
     * Sets the value of the 'topicId' field.
     *
     * @param value
     *            the value to set.
     */
    public void setTopicId(String value) {
        this.topicId = value;
    }

    /**
     * Sets the value of the 'topicId' field.
     *
     * @param value
     *            the value to set.
     */
    public void setTopicId(Long value) {
        this.topicId = Long.toString(value);
    }

    /**
     * Gets the value of the 'command' field.
     */
    public SubscriptionCommandType getCommand() {
        return command;
    }

    /**
     * Sets the value of the 'command' field.
     *
     * @param value
     *            the value to set.
     */
    public void setCommand(SubscriptionCommandType value) {
        this.command = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((topicId == null) ? 0 : topicId.hashCode());
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
        SubscriptionCommand other = (SubscriptionCommand) obj;
        if (command != other.command) {
            return false;
        }
        if (topicId == null) {
            if (other.topicId != null) {
                return false;
            }
        } else if (!topicId.equals(other.topicId)) {
            return false;
        }
        return true;
    }
}
