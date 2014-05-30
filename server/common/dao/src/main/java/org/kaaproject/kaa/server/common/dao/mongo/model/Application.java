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

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;


@Document(collection = Application.COLLECTION_NAME)
public final class Application implements ToDto<ApplicationDto>, Serializable {

    private static final long serialVersionUID = 6197879464079691539L;

    public static final String COLLECTION_NAME = "application";

    @Id
    private String id;
    @Indexed
    @Field("application_token")
    private String applicationToken;
    private String name;
    @Field("seq_num")
    private int sequenceNumber;
    @Field("tenant_id")
    private ObjectId tenantId;
    @Field("upd")
    private Update update;

    public Application() {
    }

    public Application(ApplicationDto dto) {
        this.id = dto.getId();
        this.applicationToken = dto.getApplicationToken();
        this.name = dto.getName();
        this.sequenceNumber = dto.getSequenceNumber();
        this.tenantId = idToObjectId(dto.getTenantId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public ObjectId getTenantId() {
        return tenantId;
    }

    public void setTenantId(ObjectId tenantId) {
        this.tenantId = tenantId;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Application)) {
            return false;
        }

        Application that = (Application) o;

        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (applicationToken != null ? !applicationToken.equals(that.applicationToken) : that.applicationToken != null) {
            return false;
        }
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (update != null ? !update.equals(that.update) : that.update != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationToken != null ? applicationToken.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (update != null ? update.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", applicationToken='" + applicationToken + '\'' +
                ", name='" + name + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", tenantId=" + tenantId +
                ", update=" + update +
                '}';
    }

    @Override
    public ApplicationDto toDto() {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(id);
        dto.setApplicationToken(applicationToken);
        dto.setName(name);
        dto.setSequenceNumber(sequenceNumber);
        dto.setTenantId(idToString(tenantId));
        return dto;
    }
}
