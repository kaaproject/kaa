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
import java.util.Arrays;
import java.util.Date;

public class NotificationDto implements HasId, Serializable {

    private static final long serialVersionUID = -4470699717187588732L;

    private String id;
    private String applicationId;
    private String schemaId;
    private String topicId;
    private int nfVersion;
    private Date lastTimeModify;
    private NotificationTypeDto type;
    private byte[] body;
    private Date expiredAt;
    private int secNum = -1;

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

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public int getNfVersion() {
        return nfVersion;
    }

    public void setNfVersion(int nfVersion) {
        this.nfVersion = nfVersion;
    }

    public Date getLastTimeModify() {
        return lastTimeModify;
    }

    public void setLastTimeModify(Date lastTimeModify) {
        this.lastTimeModify = lastTimeModify;
    }

    public NotificationTypeDto getType() {
        return type;
    }

    public void setType(NotificationTypeDto type) {
        this.type = type;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
    }

    public int getSecNum() {
        return secNum;
    }

    public void setSecNum(int secNum) {
        this.secNum = secNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationDto)) {
            return false;
        }

        NotificationDto that = (NotificationDto) o;

        if (secNum != that.secNum) {
            return false;
        }
        if (nfVersion != that.nfVersion) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (!Arrays.equals(body, that.body)) {
            return false;
        }
        if (expiredAt != null ? !expiredAt.equals(that.expiredAt) : that.expiredAt != null) {
            return false;
        }
        if (lastTimeModify != null ? !lastTimeModify.equals(that.lastTimeModify) : that.lastTimeModify != null) {
            return false;
        }
        if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) {
            return false;
        }
        if (topicId != null ? !topicId.equals(that.topicId) : that.topicId != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
        result = 31 * result + (topicId != null ? topicId.hashCode() : 0);
        result = 31 * result + nfVersion;
        result = 31 * result + (lastTimeModify != null ? lastTimeModify.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
        result = 31 * result + (expiredAt != null ? expiredAt.hashCode() : 0);
        result = 31 * result + secNum;
        return result;
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", schemaId='" + schemaId + '\'' +
                ", topicId='" + topicId + '\'' +
                ", nfVersion=" + nfVersion +
                ", lastTimeModify=" + lastTimeModify +
                ", type=" + type +
                ", body=" + Arrays.toString(body) +
                ", expiredAt=" + expiredAt +
                ", secNum=" + secNum +
                '}';
    }
}
