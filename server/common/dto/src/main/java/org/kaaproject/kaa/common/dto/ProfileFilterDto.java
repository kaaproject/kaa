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

package org.kaaproject.kaa.common.dto;


import java.util.Set;

public class ProfileFilterDto extends AbstractStructureDto {

    private static final long serialVersionUID = 3068910692262107362L;

    private Set<Integer> endpointSchemaVersions;
    private Set<Integer> serverSchemaVersions;

    public Set<Integer> getEndpointSchemaVersions() {
        return endpointSchemaVersions;
    }

    public void setEndpointSchemaVersions(Set<Integer> endpointSchemaVersions) {
        this.endpointSchemaVersions = endpointSchemaVersions;
    }

    public Set<Integer> getServerSchemaVersions() {
        return serverSchemaVersions;
    }

    public void setServerSchemaVersions(Set<Integer> serverSchemaVersions) {
        this.serverSchemaVersions = serverSchemaVersions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProfileFilterDto that = (ProfileFilterDto) o;

        if (endpointSchemaVersions != null ? !endpointSchemaVersions.equals(that.endpointSchemaVersions) : that.endpointSchemaVersions != null) return false;
        return serverSchemaVersions != null ? serverSchemaVersions.equals(that.serverSchemaVersions) : that.serverSchemaVersions == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (endpointSchemaVersions != null ? endpointSchemaVersions.hashCode() : 0);
        result = 31 * result + (serverSchemaVersions != null ? serverSchemaVersions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileFilterDto{" +
                "endpointSchemaVersions=" + endpointSchemaVersions +
                ", serverSchemaVersions=" + serverSchemaVersions +
                "} " + super.toString();
    }
}
