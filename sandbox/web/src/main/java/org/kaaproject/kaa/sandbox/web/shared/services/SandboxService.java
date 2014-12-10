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

package org.kaaproject.kaa.sandbox.web.shared.services;

import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.shared.dto.BuildOutputData;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/sandboxService")
public interface SandboxService extends RemoteService {

    public boolean changeKaaHostEnabled() throws SandboxServiceException;
    
    public void changeKaaHost(String uuid, String host) throws SandboxServiceException;
    
    public List<Project> getDemoProjects() throws SandboxServiceException;
    
    public boolean checkProjectDataExists(String projectId, ProjectDataType dataType) throws SandboxServiceException;
 
    public void buildProjectData(String uuid, BuildOutputData outputData, String projectId, ProjectDataType dataType) throws SandboxServiceException;
}
