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

package org.kaaproject.kaa.sandbox.web.controller;

import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.shared.dto.BuildOutputData;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxService;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The Class SandboxController.
 */
@Controller
@RequestMapping("api")
public class SandboxController {

    private static final String DATA_TYPE = "dataType";

    private static final String PROJECT_ID = "projectId";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SandboxController.class);
    
    /** The sandbox service. */
    @Autowired
    SandboxService sandboxService;
    
    @ExceptionHandler(SandboxServiceException.class)
    public ResponseEntity<String> handleSandboxServiceException(SandboxServiceException ex) {
        ResponseEntity<String> entity = new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return entity;
    }
    
    /**
     * Gets all demo projects.
     *
     */
    @RequestMapping(value="demoProjects", method=RequestMethod.GET)
    @ResponseBody
    public List<Project> getDemoProjects() throws SandboxServiceException {
        return sandboxService.getDemoProjects();
    }
    
    /**
     * Check if project data is cached.
     *
     */
    @RequestMapping(value="isProjectDataExists", method=RequestMethod.GET)
    @ResponseBody
    public boolean checkProjectDataExists(
            @RequestParam(value=PROJECT_ID) String projectId,
            @RequestParam(value=DATA_TYPE) String dataType) throws SandboxServiceException {
        return sandboxService.checkProjectDataExists(projectId, ProjectDataType.valueOf(dataType.toUpperCase()));
    }
    
    /**
     * Check if project data is cached.
     *
     */
    @RequestMapping(value="buildProjectData", method=RequestMethod.POST)
    @ResponseBody
    public byte[] buildProjectData(
            @RequestParam(value=PROJECT_ID) String projectId, 
            @RequestParam(value=DATA_TYPE) String dataType) throws SandboxServiceException {
        BuildOutputData buildOutputData = new BuildOutputData();
        try {
            sandboxService.buildProjectData(null, buildOutputData, projectId, ProjectDataType.valueOf(dataType.toUpperCase()));
        } catch (SandboxServiceException e) {
            LOG.error("Failed to build project data!", e);
        }
        return buildOutputData.getOutputData();
    }

}
