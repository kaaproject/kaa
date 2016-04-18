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

import java.nio.ByteBuffer;

public final class Event {
    private int seqNum;
    private String eventClassFQN;
    private ByteBuffer eventData;
    private String source;
    private String target;

    public Event() {
    }

    /**
     * All-args constructor.
     */
    public Event(int seqNum, String eventClassFQN, ByteBuffer eventData, String source, String target) {
        this.seqNum = seqNum;
        this.eventClassFQN = eventClassFQN;
        this.eventData = eventData;
        this.source = source;
        this.target = target;
    }

    /**
     * Gets the value of the 'seqNum' field.
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Sets the value of the 'seqNum' field.
     *
     * @param value
     *            the value to set.
     */
    public void setSeqNum(int value) {
        this.seqNum = value;
    }

    /**
     * Gets the value of the 'eventClassFQN' field.
     */
    public String getEventClassFQN() {
        return eventClassFQN;
    }

    /**
     * Sets the value of the 'eventClassFQN' field.
     *
     * @param value
     *            the value to set.
     */
    public void setEventClassFQN(String value) {
        this.eventClassFQN = value;
    }

    /**
     * Gets the value of the 'eventData' field.
     */
    public ByteBuffer getEventData() {
        return eventData;
    }

    /**
     * Sets the value of the 'eventData' field.
     *
     * @param value
     *            the value to set.
     */
    public void setEventData(ByteBuffer value) {
        this.eventData = value;
    }

    /**
     * Gets the value of the 'source' field.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the 'source' field.
     *
     * @param value
     *            the value to set.
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the 'target' field.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the 'target' field.
     *
     * @param value
     *            the value to set.
     */
    public void setTarget(String value) {
        this.target = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventClassFQN == null) ? 0 : eventClassFQN.hashCode());
        result = prime * result + ((eventData == null) ? 0 : eventData.hashCode());
        result = prime * result + seqNum;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        Event other = (Event) obj;
        if (eventClassFQN == null) {
            if (other.eventClassFQN != null) {
                return false;
            }
        } else if (!eventClassFQN.equals(other.eventClassFQN)) {
            return false;
        }
        if (eventData == null) {
            if (other.eventData != null) {
                return false;
            }
        } else if (!eventData.equals(other.eventData)) {
            return false;
        }
        if (seqNum != other.seqNum) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Event [seqNum=");
        builder.append(seqNum);
        builder.append(", eventClassFQN=");
        builder.append(eventClassFQN);
        builder.append(", eventData=");
        builder.append(eventData);
        builder.append(", source=");
        builder.append(source);
        builder.append(", target=");
        builder.append(target);
        builder.append("]");
        return builder.toString();
    }

}
