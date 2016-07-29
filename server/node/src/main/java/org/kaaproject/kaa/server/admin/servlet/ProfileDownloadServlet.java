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

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.shared.services.ProfileService;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ProfileDownloadServlet extends HttpServlet implements Servlet, ServletParams {

    private static final long serialVersionUID = 1584721028492234643L;

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ProfileDownloadServlet.class);

    private static final int BUFFER = 1024 * 100;
    
    private static final String JSON = "application/json";
    
    private static final ObjectMapper FORMATTER = new ObjectMapper();
    
    /** The Constant PROFILE_FILE_NAME_PATTERN. */
    private static final String PROFILE_FILE_NAME_PATTERN = "ep-{}-{}-profile-{}.json";

    @Autowired
    private ProfileService profileService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String endpointKey = URLDecoder.decode(request.getParameter(ENDPOINT_KEY_PARAMETER), "UTF-8");
        String profileTypeString = URLDecoder.decode(request.getParameter(PROFILE_TYPE_PARAMETER), "UTF-8");
        ProfileType profileType = ProfileType.valueOf(profileTypeString);
        
        try {
            EndpointProfileDto profile = profileService.getEndpointProfileByKeyHash(endpointKey);
            String json;
            String fileName = MessageFormatter.arrayFormat(PROFILE_FILE_NAME_PATTERN, 
                    new Object[] { endpointKey, profileType.name().toLowerCase(), System.currentTimeMillis() }).getMessage();
            if (profileType == ProfileType.CLIENT) {
                json = profile.getClientProfileBody();
            } else {
                json = profile.getServerProfileBody();
            }
            if (json == null || json.isEmpty()) {
                json = "{}";
            }
            Object jsonObject = FORMATTER.readValue(json, Object.class);
            byte[] body = FORMATTER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject).getBytes("UTF-8");
            ServletUtils.prepareDisposition(request, response, fileName);
            response.setContentType(JSON);
            response.setContentLength(body.length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(body);
            response.flushBuffer();
        } catch (Exception e) {
            LOG.error("Unexpected error in ProfileDownloadServlet.doGet: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get file: " + e.getMessage());
        }
    }
}
