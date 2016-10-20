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

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationService;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class EndpointConfigurationServlet extends HttpServlet implements  ServletParams {

  private static final long serialVersionUID = 1584721028492234643L;

  private static final Logger LOG = LoggerFactory.getLogger(ProfileDownloadServlet.class);

  private static final int BUFFER = 1024 * 100;

  private static final String JSON = "application/json";

  private static final ObjectMapper FORMATTER = new ObjectMapper();

  /**
   * The Constant PROFILE_FILE_NAME_PATTERN.
   */
  private static final String EP_CONF_FILE_NAME_PATTERN = "ep-{}-conf.json";

  @Autowired
  private ConfigurationService configurationService;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
        config.getServletContext());
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String endpointKey = URLDecoder.decode(request.getParameter(ENDPOINT_KEY_PARAMETER), "UTF-8");
    try {
      String json = configurationService.findEndpointConfigurationByEndpointKeyHash(endpointKey);

      String fileName = MessageFormatter.arrayFormat(EP_CONF_FILE_NAME_PATTERN,
          new Object[]{endpointKey}).getMessage();

      Object jsonObject = FORMATTER.readValue(json, Object.class);
      byte[] body = FORMATTER.writerWithDefaultPrettyPrinter()
          .writeValueAsString(jsonObject)
          .getBytes("UTF-8");
      ServletUtils.prepareDisposition(request, response, fileName);
      response.setContentType(JSON);
      response.setContentLength(body.length);
      response.setBufferSize(BUFFER);
      response.getOutputStream().write(body);
      response.flushBuffer();
    } catch (Exception ex) {
      LOG.error("Unexpected error in ProfileDownloadServlet.doGet: ", ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get file: "
          + ex.getMessage());
    }
  }
}
