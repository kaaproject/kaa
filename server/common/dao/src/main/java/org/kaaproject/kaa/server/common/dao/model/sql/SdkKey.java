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

import org.apache.commons.codec.binary.Base64;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.DtoByteMarshaller;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.*;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = SDK_TOKEN_TABLE_NAME)
public final class SdkKey extends GenericModel<SdkPropertiesDto> implements Serializable {

    private static final long serialVersionUID = -5963289882951330950L;
    private static final String HASH_ALGORITHM = "SHA1";

    @Column(name = SDK_TOKEN_TOKEN)
    private String token;

    @Column(name = SDK_TOKEN_RAW_DATA, length = 1000)
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

    public SdkKey(SdkPropertiesDto dto) {
        if (dto != null) {
            try {
                this.id = getLongId(dto.getId());
                dto.setId(null);                // dto's id doesn't have to influence sdk token value
                List<String> aefMapIds = dto.getAefMapIds();
                // result token value for empty list and for null field should be identical
                if (aefMapIds != null && aefMapIds.isEmpty()) {
                    dto.setAefMapIds(null);
                }
                this.data = DtoByteMarshaller.toBytes(dto);
                dto.setAefMapIds(aefMapIds);
                dto.setId(this.id == null ? null : String.valueOf(this.id));

                MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
                digest.update(this.data);
                this.token = Base64.encodeBase64String(digest.digest());
                Long appId = getLongId(dto.getApplicationId());
                this.application = appId != null ? new Application(appId) : null;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
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
    protected SdkPropertiesDto createDto() {
        return new SdkPropertiesDto();
    }

    @Override
    public SdkPropertiesDto toDto() {
        return DtoByteMarshaller.fromBytes(data);
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
