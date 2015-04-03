/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.SdkKeyDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Long.getLong;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.*;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = SDK_TOKEN_TABLE_NAME)
public final class SdkKey extends GenericModel<SdkKeyDto> implements Serializable {

    private static final long serialVersionUID = -5963289882951330950L;

    @Column(name = SDK_TOKEN_SDK_KEY)
    private String token;

    @Column(name = SDK_TOKEN_RAW_DATA)
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = PLUGIN_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected Application application;

    public SdkKey() {
    }

    public SdkKey(Long id) {
        this.id = id;
    }

    public SdkKey(SdkKeyDto dto) {
        if (dto != null) {
            this.id = getLong(dto.getId());
            this.token = dto.getToken();
            this.data = dto.getData();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    protected SdkKeyDto createDto() {
        return new SdkKeyDto();
    }

    @Override
    public SdkKeyDto toDto() {
        SdkKeyDto dto = createDto();
        dto.setId(getStringId());
        dto.setToken(token);
        dto.setData(data);
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SdkKey sdkKey = (SdkKey) o;

        if (application != null ? !application.equals(sdkKey.application) : sdkKey.application != null) {
            return false;
        }
        if (!Arrays.equals(data, sdkKey.data)) {
            return false;
        }
        if (token != null ? !token.equals(sdkKey.token) : sdkKey.token != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + (application != null ? application.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SdkToken{" +
                "token='" + token + '\'' +
                ", data=" + Arrays.toString(data) +
                ", application=" + application +
                '}';
    }
}
