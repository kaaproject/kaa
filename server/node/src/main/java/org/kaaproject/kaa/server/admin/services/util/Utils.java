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
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

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

  /**
   * Handle exception.
   *
   * @param exception is <code>Exception</code> instance
   * @param logException is true if need write logs
   * @return instance of <code>KaaAdminServiceException</code>
   */
  public static KaaAdminServiceException handleException(Exception exception,
                                                         boolean logException) {
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
    } else if (exception instanceof KaaOptimisticLockingFailureException) {
      return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.CONFLICT);
    } else if (exception instanceof IllegalArgumentException || exception instanceof IncorrectParameterException
        || cause.contains("IncorrectParameterException")) {
      return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.BAD_REQUEST_PARAMS);
    } else {
      return new KaaAdminServiceException(exception.getMessage(), ServiceErrorCode.GENERAL_ERROR);
    }
  }

  /**
   * Throw exception if reference is null and return this reference if not.
   *
   * @param reference is reference to check
   * @param <T> is type of reference
   * @return reference
   */
  public static <T> T checkNotNull(T reference) throws KaaAdminServiceException {
    if (reference == null) {
      throw new KaaAdminServiceException(
          "The requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
    }
    return reference;
  }

  /**
   * Throw exception if field isn't unique - the storedEmails already contains this field.
   *
   * @param field        is checking field
   * @param storedEmails is <code>Set</code> where search field
   * @param fieldName    is field name
   */
  public static void checkFieldUniquieness(String field,
                                           Set<String> storedEmails,
                                           String fieldName)
      throws KaaAdminServiceException {
    checkNotNull(field);
    boolean isAdded = storedEmails.add(field);
    if (!isAdded) {
      throw new KaaAdminServiceException(
          String.format("Entered %s is already used by another user!", fieldName),
          ServiceErrorCode.INVALID_ARGUMENTS
      );
    }
  }

  /**
   * Get authenticated user from spring security holder.
   *
   * @return user data transfer object
   */
  public static AuthUserDto getCurrentUser() throws KaaAdminServiceException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof AuthUserDto) {
      return (AuthUserDto) authentication.getPrincipal();
    } else {
      throw new KaaAdminServiceException(
          "You are not authorized to perform this operation!",
          ServiceErrorCode.NOT_AUTHORIZED);
    }
  }
}
