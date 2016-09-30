/**
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.admin.servlet;

import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigurationDownloadServlet extends HttpServlet implements ServletParams {

  private final int buffer = 1024 * 100;

  @Autowired
  private ConfigurationService configurationService;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    SpringBeanAutowiringSupport
        .processInjectionBasedOnServletContext(this, config.getServletContext());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String schemaId = URLDecoder.decode(req.getParameter(CONFIGURATION_SCHEMA_ID), "UTF-8");
    String endpointGroupId = URLDecoder.decode(req.getParameter(ENDPOINT_GROUP_ID), "UTF-8");

    ConfigurationRecordDto dto = configurationService
        .findConfigurationRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId);
    String body = dto.getActiveStructureDto().getBody();
    resp.setContentType("application/octet-stream");
    resp.setHeader("Content-Disposition:", "attachment;filename=configurationSchema.txt");
    resp.setBufferSize(buffer);
    resp.setContentLength(body.length());
    resp.getOutputStream().print(body);
  }
}