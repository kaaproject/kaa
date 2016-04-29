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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class HistoryDto implements HasId, Serializable {

    private static final long serialVersionUID = 8451794973218317603L;

    private String id;
    private String applicationId;
    private int sequenceNumber;
    private ChangeDto change;
    private long lastModifyTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public ChangeDto getChange() {
        return change;
    }

    public void setChange(ChangeDto change) {
        this.change = change;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryDto that = (HistoryDto) o;

        if (lastModifyTime != that.lastModifyTime) {
            return false;
        }
        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (change != null ? !change.equals(that.change) : that.change != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + sequenceNumber;
        result = 31 * result + (change != null ? change.hashCode() : 0);
        result = 31 * result + (int) (lastModifyTime ^ (lastModifyTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HistoryDto{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", change=" + change +
                ", lastModifyTime=" + lastModifyTime +
                '}';
    }
}
