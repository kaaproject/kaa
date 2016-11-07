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

package org.kaaproject.kaa.server.admin.controller;

import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class ExceptionController {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(KaaAdminServiceException.class)
    public void handleKaaAdminServiceException(KaaAdminServiceException ex, HttpServletResponse response) {
        try {
            ServiceErrorCode errorCode = ex.getErrorCode();
            switch (errorCode) {
                case NOT_AUTHORIZED:
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                    break;
                case PERMISSION_DENIED:
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
                    break;
                case INVALID_ARGUMENTS:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                    break;
                case INVALID_SCHEMA:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                    break;
                case FILE_NOT_FOUND:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
                    break;
                case ITEM_NOT_FOUND:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
                    break;
                case BAD_REQUEST_PARAMS:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                    break;
                case GENERAL_ERROR:
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    break;
                case CONFLICT:
                    response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
                default:
                    break;
            }
        } catch (IOException e) {
            LOG.error("Can't handle exception", e);
        }
    }

}
