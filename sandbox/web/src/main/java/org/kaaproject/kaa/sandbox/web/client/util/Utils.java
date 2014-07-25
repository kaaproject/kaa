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

package org.kaaproject.kaa.sandbox.web.client.util;

import java.util.Date;

import org.kaaproject.kaa.sandbox.web.client.SandboxConstants;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources;
import org.kaaproject.kaa.sandbox.web.client.i18n.SandboxMessages;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class Utils {

    public static final SandboxResources resources = GWT.create(
            SandboxResources.class);

    public static final SandboxConstants constants = GWT.create(
            SandboxConstants.class);

    public static final SandboxMessages messages = GWT.create(
            SandboxMessages.class);

    private static final DateTimeFormat simpleDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy");
    private static final DateTimeFormat simpleDateTimeFormat = DateTimeFormat.getFormat("MM/dd/yyyy h:mm a");


    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof SandboxServiceException) {
            SandboxServiceException sandboxException = (SandboxServiceException)throwable;
            String message = constants.general_error();
            message += sandboxException.getMessage();
            return message;
        }
        else {
            return throwable.getMessage();
        }
    }

    public static String millisecondsToDateString(long millis) {
        return simpleDateFormat.format(new Date(millis));
    }

    public static String millisecondsToDateTimeString(long millis) {
        return simpleDateTimeFormat.format(new Date(millis));
    }

}
