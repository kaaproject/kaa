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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_APPLICATION_TOKEN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_CREDENTIALS_SERVICE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_SEQUENCE_NUMBER;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.ApplicationDto;

@Entity
@Table(name = APPLICATION_TABLE_NAME, uniqueConstraints = {
@UniqueConstraint(columnNames = {APPLICATION_TENANT_ID, APPLICATION_NAME})})
public class Application extends GenericModel<ApplicationDto> implements Serializable {

    private static final long serialVersionUID = 3402917989585810543L;

    @Column(name = APPLICATION_APPLICATION_TOKEN, unique = true)
    private String applicationToken;

    @Column(name = APPLICATION_NAME, nullable = false)
    private String name;

    @Column(name = APPLICATION_SEQUENCE_NUMBER)
    private int sequenceNumber;

    @ManyToOne
    @JoinColumn(name = APPLICATION_TENANT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tenant tenant;

    @Column(name = APPLICATION_CREDENTIALS_SERVICE_NAME)
    private String credentialsServiceName;

    public Application() {
    }

    public Application(Long id) {
        this.id = id;
    }

    public Application(ApplicationDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.applicationToken = dto.getApplicationToken();
            this.name = dto.getName();
            this.sequenceNumber = dto.getSequenceNumber();
            Long tenantId = getLongId(dto.getTenantId());
            if (tenantId != null) {
                this.tenant = new Tenant(tenantId);
            }
            this.credentialsServiceName = dto.getCredentialsServiceName();
        }
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

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getCredentialsServiceName() {
        return credentialsServiceName;
    }

    public void setCredentialsServiceName(String credentialsServiceName) {
        this.credentialsServiceName = credentialsServiceName;
    }

    public int incrementSequenceNumber() {
        return ++sequenceNumber;
    }

    @Override
    public String toString() {
        return "Application [id=" + id + ", applicationToken=" + applicationToken + ", name=" + name + ", sequenceNumber=" + sequenceNumber
                + ", tenant=" + tenant + ", credentialsServiceName=" + credentialsServiceName + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
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
        Application other = (Application) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (tenant == null) {
            if (other.tenant != null) {
                return false;
            }
        } else if (!tenant.equals(other.tenant)) {
            return false;
        }
        return true;
    }

    @Override
    protected ApplicationDto createDto() {
        return new ApplicationDto();
    }

    @Override
    protected GenericModel<ApplicationDto> newInstance(Long id) {
        return new Application(id);
    }

    @Override
    public ApplicationDto toDto() {
        ApplicationDto dto = createDto();
        dto.setId(getStringId());
        dto.setApplicationToken(applicationToken);
        dto.setName(name);
        dto.setSequenceNumber(sequenceNumber);
        if (tenant != null) {
            dto.setTenantId(tenant.getStringId());
        }
        dto.setCredentialsServiceName(credentialsServiceName);
        return dto;
    }
}
