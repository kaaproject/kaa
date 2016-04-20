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

package org.kaaproject.kaa.common.dto.event;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class ApplicationEventMapDto implements HasId, Serializable {

    private static final long serialVersionUID = 5583771790553847550L;

    private String id;
    private String eventClassId;
    private String fqn;
    private ApplicationEventAction action;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getEventClassId() {
        return eventClassId;
    }

    public void setEventClassId(String eventClassId) {
        this.eventClassId = eventClassId;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public ApplicationEventAction getAction() {
        return action;
    }

    public void setAction(ApplicationEventAction action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result
                + ((eventClassId == null) ? 0 : eventClassId.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
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
        ApplicationEventMapDto other = (ApplicationEventMapDto) obj;
        if (action != other.action) {
            return false;
        }
        if (eventClassId == null) { 
            if (other.eventClassId != null) {
                return false;
            }
        } else if (!eventClassId.equals(other.eventClassId)) {
            return false;
        }
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ApplicationEventMapDto [eventClassId=");
        builder.append(eventClassId);
        builder.append(", fqn=");
        builder.append(fqn);
        builder.append(", action=");
        builder.append(action);
        builder.append("]");
        return builder.toString();
    }

}
