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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_VERIFIER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_VERIFIER_TOKEN;

@Entity
@Table(name = USER_VERIFIER_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public class UserVerifier extends Plugin<UserVerifierDto> implements Serializable {

    private static final long serialVersionUID = -6822520685170109625L;

    @Column(name = USER_VERIFIER_TOKEN)
    private String verifierToken;
    
    public UserVerifier() {
        super();
    }
    
    public UserVerifier(Long id) {
        this.id = id;
    }

    public UserVerifier(UserVerifierDto dto) {
        super(dto);
        if (dto != null) {
            this.verifierToken = dto.getVerifierToken();
        }
    }
    
    public String getVerifierToken() {
        return verifierToken;
    }

    public void setVerifierToken(String verifierToken) {
        this.verifierToken = verifierToken;
    }

    @Override
    public UserVerifierDto toDto() {
        UserVerifierDto dto = super.toDto();
        dto.setVerifierToken(verifierToken);
        return dto;
    }

    @Override
    protected UserVerifierDto createDto() {
        return new UserVerifierDto();
    }

    @Override
    protected GenericModel<UserVerifierDto> newInstance(Long id) {
        return new UserVerifier(id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((verifierToken == null) ? 0 : verifierToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserVerifier other = (UserVerifier) obj;
        if (verifierToken == null) {
            if (other.verifierToken != null) {
                return false;
            }
        } else if (!verifierToken.equals(other.verifierToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserVerifier [verifierToken=");
        builder.append(verifierToken);
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }    
    
}
