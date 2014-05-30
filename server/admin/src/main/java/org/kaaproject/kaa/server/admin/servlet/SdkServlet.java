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

package org.kaaproject.kaa.server.admin.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.iharder.Base64;

import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.shared.dto.SdkKey;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class SdkServlet extends HttpServlet implements Servlet {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SdkServlet.class);

    private static final long serialVersionUID = 4151191758109799417L;

    private static final int BUFFER = 1024 * 100;

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
        String sdkKeyBase64 = request.getParameter(SdkKey.SDK_KEY_PARAMETER);
        try {
            SdkKey key = (SdkKey)Base64.decodeToObject(sdkKeyBase64, Base64.URL_SAFE, null);
            Sdk sdk = cacheService.getSdk(key);
            response.setContentType(key.getTargetPlatform().getContentType());
            prepareDisposition(request, response, sdk.getFileName());
            response.setContentLength(sdk.getData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(sdk.getData());
        }
        catch (Exception e) {
            logger.error("Unexpected error in SdkServlet.doGet: ", e);
        }
    }


    private void prepareDisposition(HttpServletRequest request, HttpServletResponse response, String fileName) {
          String userAgent = request.getHeader("user-agent");
          boolean isInternetExplorer = (userAgent.indexOf("MSIE") > -1);

          try {
              byte[] fileNameBytes = fileName.getBytes((isInternetExplorer) ? ("windows-1250") : ("utf-8"));
              String dispositionFileName = "";
              for (byte b: fileNameBytes) dispositionFileName += (char)(b & 0xff);

              String disposition = "attachment; filename=\"" + dispositionFileName + "\"";
              response.setHeader("Content-disposition", disposition);
          } catch(UnsupportedEncodingException ence) {
             //
          }
    }

}
