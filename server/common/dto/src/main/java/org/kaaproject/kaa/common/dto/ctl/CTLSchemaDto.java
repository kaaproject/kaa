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

package org.kaaproject.kaa.common.dto.ctl;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 * 
 * @since v0.8.0
 */
public class CTLSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = 6967757225688280884L;

    private String id;
    private String fqn;
    private int version;
    private CTLSchemaScope scope;
    private String tenantId;
    private String applicationId;
    private String body;
    private List<CTLDependencyDto> dependencies;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public CTLSchemaScope getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScope scope) {
        this.scope = scope;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<CTLDependencyDto> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<CTLDependencyDto> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int result = 1;

        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + version;

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (this.getClass() != o.getClass()) {
            return false;
        }

        CTLSchemaDto other = (CTLSchemaDto) o;

        if (this.applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!this.applicationId.equals(other.applicationId)) {
            return false;
        }

        if (this.body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!this.body.equals(other.body)) {
            return false;
        }

        if (this.dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!this.dependencies.equals(other.dependencies)) {
            return false;
        }

        if (this.fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!this.fqn.equals(other.fqn)) {
            return false;
        }

        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }

        if (this.scope != other.scope) {
            return false;
        }

        if (this.tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!this.tenantId.equals(other.tenantId)) {
            return false;
        }

        if (this.version != other.version) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTLSchemaDto [id=");
        builder.append(id);
        builder.append(", fqn=");
        builder.append(fqn);
        builder.append(", version=");
        builder.append(version);
        builder.append(", scope=");
        builder.append(scope);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", body=");
        builder.append(body);
        builder.append(", dependencies=");
        builder.append(dependencies);
        builder.append("]");
        return builder.toString();
    }
}
