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

package org.kaaproject.kaa.common.dto.logs;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class LogEventDto implements HasId, Serializable {

    private static final long serialVersionUID = 4793296986403052115L;

    private String id;
    private String endpointKey;
    private long dateCreated;
    private String event;

    public LogEventDto () {

    }

    public LogEventDto(String endpointKey, long dateCreated, String event) {
        this.endpointKey = endpointKey;
        this.dateCreated = dateCreated;
        this.event = event;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
