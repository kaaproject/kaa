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
package org.kaaproject.kaa.sandbox.web.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.sandbox.web.services.cache.CacheService;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataKey;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ProjectFileServlet extends HttpServlet implements Servlet {

    private static final long serialVersionUID = -3684552082514584898L;
    
    private static final int BUFFER = 1024 * 100;
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ProjectFileServlet.class);
    
    @Autowired
    private CacheService cacheService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
          config.getServletContext());
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProjectDataKey key = extractKey(request);
        if (key != null) {
            try {
                FileData fileData = cacheService.getProjectFile(key);
                if (fileData != null) {
                    if (fileData.getContentType() != null) {
                        response.setContentType(fileData.getContentType());
                    }
                    ServletUtils.prepareDisposition(request, response, fileData.getFileName());
                    response.setContentLength(fileData.getFileData().length);
                    response.setBufferSize(BUFFER);
                    response.getOutputStream().write(fileData.getFileData());
                    response.flushBuffer();
                }
                else {
                    logger.error("File data not found in cache for requested parameters: projectId [{}]  type [{}]", key.getProjectId(), key.getProjectDataType());
                }
            }
            catch (Exception e) {
                logger.error("Unexpected error in ProjectFileServlet.doGet: ", e);
            }
        }
    }
    
    protected ProjectDataKey extractKey(HttpServletRequest request) {
        ProjectDataKey result = null;
        String projectId = request.getParameter(ProjectDataKey.PROJECT_ID_PARAMETER);
        String type = request.getParameter(ProjectDataKey.PROJECT_DATA_TYPE_PARAMETER);
        ProjectDataType dataType = null;
        if (type != null && type.length()>0) {
            try {
                dataType = ProjectDataType.valueOf(type.toUpperCase());
            }
            catch (Exception e) {
            }
        }
        if (projectId != null && projectId.length()>0 && dataType != null) {
            result = new ProjectDataKey(projectId, dataType);
        }
        if (result == null) {
            logger.error("Invalid parameters: projectId [{}]  type [{}]", projectId, type);
        }
        return result;
    }
    
}
