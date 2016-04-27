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

public final class EventSequenceNumberResponse {
    private int seqNum;

    public EventSequenceNumberResponse() {
    }

    /**
     * All-args constructor.
     */
    public EventSequenceNumberResponse(Integer seqNum) {
        this.seqNum = seqNum;
    }   

    /**
     * Gets the value of the 'seqNum' field.
     */
    public Integer getSeqNum() {
        return seqNum;
    }

    /**
     * Sets the value of the 'seqNum' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setSeqNum(Integer value) {
        this.seqNum = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventSequenceNumberResponse that = (EventSequenceNumberResponse) o;

        if (seqNum != that.seqNum) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return seqNum;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventSequenceNumberResponse [seqNum=");
        builder.append(seqNum);
        builder.append("]");
        return builder.toString();
    }

}
