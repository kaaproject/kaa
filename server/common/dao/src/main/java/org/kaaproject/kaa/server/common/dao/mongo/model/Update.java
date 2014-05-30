/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

public final class Update implements Serializable {

    private static final long serialVersionUID = 4117929007713775914L;

    @Field("seq_num")
    private int sequenceNumber;
    private ProcessingStatus status;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Update)) {
            return false;
        }

        Update update = (Update) o;

        if (sequenceNumber != update.sequenceNumber) {
            return false;
        }
        if (status != update.status) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sequenceNumber;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Update{" +
                "sequenceNumber=" + sequenceNumber +
                ", status=" + status +
                '}';
    }
}
