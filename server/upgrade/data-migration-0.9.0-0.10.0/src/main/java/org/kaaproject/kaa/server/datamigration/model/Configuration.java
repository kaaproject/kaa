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

package org.kaaproject.kaa.server.datamigration.model;

import java.io.Serializable;

public class Configuration implements Serializable {

    private static final long serialVersionUID = -1176562073;

    private byte[] configuration_body;
    private Integer configuration_schems_version;
    private Long    id;
    private Long configuration_schems_id;

    public Configuration() {}

    public Configuration(Configuration value) {
        this.configuration_body = value.configuration_body;
        this.configuration_schems_version = value.configuration_schems_version;
        this.id = value.id;
        this.configuration_schems_id = value.configuration_schems_id;
    }

    public Configuration(
            byte[] configurationBody,
            Integer configurationSchemsVersion,
            Long id,
            Long configurationSchemsId
    ) {
        this.configuration_body = configurationBody;
        this.configuration_schems_version = configurationSchemsVersion;
        this.id = id;
        this.configuration_schems_id = configurationSchemsId;
    }

    public byte[] getConfiguration_body() {
        return this.configuration_body;
    }

    public void setConfiguration_body(byte[] configuration_body) {
        this.configuration_body = configuration_body;
    }

    public Integer getConfiguration_schems_version() {
        return this.configuration_schems_version;
    }

    public void setConfiguration_schems_version(Integer configuration_schems_version) {
        this.configuration_schems_version = configuration_schems_version;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfiguration_schems_id() {
        return this.configuration_schems_id;
    }

    public void setConfiguration_schems_id(Long configuration_schems_id) {
        this.configuration_schems_id = configuration_schems_id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("configuration.Configuration[");

        sb.append("configuration_body=").append(new String(configuration_body));
        sb.append(", configuration_schems_version=").append(configuration_schems_version);
        sb.append(", id=").append(id);
        sb.append(", configuration_schems_id=").append(configuration_schems_id);

        sb.append("]");
        return sb.toString();
    }
}
