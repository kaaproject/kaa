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

package org.kaaproject.kaa.server.admin.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.iharder.Base64;

import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class RecordServlet extends HttpServlet implements Servlet, ServletParams {

    private static final long serialVersionUID = 1584721028492234643L;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RecordServlet.class);

    private static final int BUFFER = 1024 * 100;

    @Autowired
    private CacheService cacheService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String recordKeyBase64 = URLDecoder.decode(request.getParameter(RECORD_KEY_PARAMETER), "UTF-8");
        try {
            RecordKey key = (RecordKey) Base64.decodeToObject(recordKeyBase64, Base64.URL_SAFE, null);
            FileData recordLibrary = cacheService.getRecordData(key);
            ServletUtils.prepareDisposition(request, response, recordLibrary.getFileName());
            response.setContentLength(recordLibrary.getFileData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(recordLibrary.getFileData());
            response.flushBuffer();
        } catch (Exception e) {
            LOG.error("Unexpected error in RecordLibraryServlet.doGet: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get file: " + e.getMessage());
        }
    }
}
