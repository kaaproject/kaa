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
package org.kaaproject.kaa.sandbox.web.shared.dto;

import java.io.Serializable;

public class ProjectDataKey implements Serializable {
    
    public final static String PROJECT_ID_PARAMETER = "projectId";
    public final static String PROJECT_DATA_TYPE_PARAMETER = "type";
    
    private static final long serialVersionUID = 4915001976227336570L;

    private String projectId;
    
    private ProjectDataType projectDataType;
    
    public ProjectDataKey() {
    }

    public ProjectDataKey(String projectId, ProjectDataType projectDataType) {
        super();
        this.projectId = projectId;
        this.projectDataType = projectDataType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public ProjectDataType getProjectDataType() {
        return projectDataType;
    }

    public void setProjectDataType(ProjectDataType projectDataType) {
        this.projectDataType = projectDataType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((projectDataType == null) ? 0 : projectDataType.hashCode());
        result = prime * result
                + ((projectId == null) ? 0 : projectId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectDataKey other = (ProjectDataKey) obj;
        if (projectDataType != other.projectDataType)
            return false;
        if (projectId == null) {
            if (other.projectId != null)
                return false;
        } else if (!projectId.equals(other.projectId))
            return false;
        return true;
    }

    
    
}
