/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.avro.ui.gwt.client.AvroUiResources;
import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.KaaTheme;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.i18n.SandboxConstants;
import org.kaaproject.kaa.sandbox.web.client.i18n.SandboxMessages;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;

import com.google.gwt.core.client.GWT;

public class Utils {

    public static final SandboxResources resources = GWT.create(
            SandboxResources.class);

    public static final SandboxConstants constants = GWT.create(
            SandboxConstants.class);

    public static final SandboxMessages messages = GWT.create(
            SandboxMessages.class);
    
    public static final AvroUiResources avroUiResources = 
            GWT.create(AvroUiResources.class);
    
    public static final KaaTheme kaaTheme = 
            resources.kaaTheme();
    
    public static final SandboxStyle sandboxStyle = 
            resources.sandboxStyle();
    
    public static final AvroUiStyle avroUiStyle =
            avroUiResources.avroUiStyle();

    public static void injectSandboxStyles() {
        kaaTheme.ensureInjected();
        sandboxStyle.ensureInjected();
        avroUiStyle.ensureInjected();
    }

    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof SandboxServiceException) {
            SandboxServiceException sandboxException = (SandboxServiceException)throwable;
            String message = constants.generalError();
            message += sandboxException.getMessage();
            return message;
        }
        else {
            return throwable.getMessage();
        }
    }

}
