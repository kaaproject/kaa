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

package org.kaaproject.kaa.server.admin.services.util;

import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static KaaAdminServiceException handleException(Exception exception) {
        return handleException(exception, true);
    }

    public static KaaAdminServiceException handleException(Exception exception, String message) {
        LOG.error("An unexpected exception occured!", exception);
        return new KaaAdminServiceException(message, ServiceErrorCode.GENERAL_ERROR);
    }

    public static KaaAdminServiceException handleException(Exception exception, boolean logException) {
        if (logException) {
            LOG.error("An unexpected exception occured!", exception);
        }

        String cause = "";
        if (exception.getCause() != null) {
            cause = exception.getCause().getClass().getCanonicalName();
        }

        if (exception instanceof KaaAdminServiceException) {
            return (KaaAdminServiceException) exception;
        } else if (exception instanceof NotFoundException) {
            return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.ITEM_NOT_FOUND);
        } else if (exception instanceof IllegalArgumentException || exception instanceof IncorrectParameterException
                || cause.contains("IncorrectParameterException")) {
            return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.BAD_REQUEST_PARAMS);
        } else {
            return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.GENERAL_ERROR);
        }
    }

    public static <T> T checkNotNull(T reference) throws KaaAdminServiceException {
        if (reference == null) {
            throw new KaaAdminServiceException("The requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
        }
        return reference;
    }

    public static AuthUserDto getCurrentUser() throws KaaAdminServiceException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof AuthUserDto) {
            return (AuthUserDto) authentication.getPrincipal();
        } else {
            throw new KaaAdminServiceException("You are not authorized to perform this operation!", ServiceErrorCode.NOT_AUTHORIZED);
        }
    }
}
