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

public final class Notification {
    private String topicId;
    private NotificationType type;
    private String uid;
    private Integer seqNumber;
    private ByteBuffer body;

    public Notification() {
    }

    /**
     * All-args constructor.
     */
    public Notification(String topicId, NotificationType type, String uid,
            Integer seqNumber, ByteBuffer body) {
        this.topicId = topicId;
        this.type = type;
        this.uid = uid;
        this.seqNumber = seqNumber;
        this.body = body;
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
     * Gets the value of the 'type' field.
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets the value of the 'type' field.
     *
     * @param value
     *            the value to set.
     */
    public void setType(NotificationType value) {
        this.type = value;
    }

    /**
     * Gets the value of the 'uid' field.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the value of the 'uid' field.
     *
     * @param value
     *            the value to set.
     */
    public void setUid(String value) {
        this.uid = value;
    }

    /**
     * Gets the value of the 'seqNumber' field.
     */
    public Integer getSeqNumber() {
        return seqNumber;
    }

    /**
     * Sets the value of the 'seqNumber' field.
     *
     * @param value
     *            the value to set.
     */
    public void setSeqNumber(Integer value) {
        this.seqNumber = value;
    }

    /**
     * Gets the value of the 'body' field.
     */
    public ByteBuffer getBody() {
        return body;
    }

    /**
     * Sets the value of the 'body' field.
     *
     * @param value
     *            the value to set.
     */
    public void setBody(ByteBuffer value) {
        this.body = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Notification that = (Notification) o;

        if (body != null ? !body.equals(that.body) : that.body != null) {
            return false;
        }
        if (seqNumber != null ? !seqNumber.equals(that.seqNumber) : that.seqNumber != null) {
            return false;
        }
        if (topicId != null ? !topicId.equals(that.topicId) : that.topicId != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = topicId != null ? topicId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (seqNumber != null ? seqNumber.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Notification [topicId=");
        builder.append(topicId);
        builder.append(", type=");
        builder.append(type);
        builder.append(", uid=");
        builder.append(uid);
        builder.append(", seqNumber=");
        builder.append(seqNumber);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }
}
