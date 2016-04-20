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

public final class TopicState {
    private String topicId;
    private int seqNumber;

    public TopicState() {
    }

    /**
     * All-args constructor.
     */
    public TopicState(String topicId, int seqNumber) {
        this.topicId = topicId;
        this.seqNumber = seqNumber;
    }

    /**
     * All-args constructor.
     */
    public TopicState(Long topicId, int seqNumber) {
        this.topicId = Long.toString(topicId);
        this.seqNumber = seqNumber;
    }

    /**
     * Gets the value of the 'topicId' field.
     */
    public String getTopicId() {
        return topicId;
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
     * Gets the value of the 'seqNumber' field.
     */
    public int getSeqNumber() {
        return seqNumber;
    }

    /**
     * Sets the value of the 'seqNumber' field.
     *
     * @param value
     *            the value to set.
     */
    public void setSeqNumber(int value) {
        this.seqNumber = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + seqNumber;
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
        TopicState other = (TopicState) obj;
        if (seqNumber != other.seqNumber) {
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopicState [topicId=");
        builder.append(topicId);
        builder.append(", seqNumber=");
        builder.append(seqNumber);
        builder.append("]");
        return builder.toString();
    }
}
