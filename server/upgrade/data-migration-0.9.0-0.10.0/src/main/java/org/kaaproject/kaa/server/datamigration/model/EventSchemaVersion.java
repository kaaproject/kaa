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

public class EventSchemaVersion {
    private Long    id;
    private String schems;
    private Long created_time;
    private String created_username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchems() {
        return schems;
    }

    public void setSchems(String schems) {
        this.schems = schems;
    }

    @Override
    public String toString() {
        return "EventSchemaVersion{" +
                "id=" + id +
                ", schems=" + schems +
                '}';
    }

    public Long getCreatedTime() {
        return created_time;
    }

    public Long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(Long created_time) {
        this.created_time = created_time;
    }

    public String getCreatedUsername() {
        return created_username;
    }

    public String getCreated_username() {
        return created_username;
    }

    public void setCreated_username(String created_username) {
        this.created_username = created_username;
    }
}
