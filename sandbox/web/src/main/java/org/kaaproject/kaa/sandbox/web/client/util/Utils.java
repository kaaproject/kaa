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
import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.KaaTheme;
import org.kaaproject.kaa.sandbox.web.client.SandboxResources.SandboxStyle;
import org.kaaproject.kaa.sandbox.web.client.i18n.SandboxConstants;
import org.kaaproject.kaa.sandbox.web.client.i18n.SandboxMessages;
import org.kaaproject.kaa.sandbox.web.shared.services.SandboxServiceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

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
    
    public static String getFeatureText(Feature feature) {
        switch (feature) {
            case CONFIGURATION:
                return constants.configuration();
            case DATA_COLLECTION:
                return constants.dataCollection();
            case EVENT:
                return constants.event();
            case NOTIFICATION:
                return constants.notification();
            case PROFILING:
                return constants.profiling();
            case USER_VERIFIER:
                return constants.userVerifier();
        }
        return null;
    }
    
    public static ImageResource getFeatureIcon(Feature feature) {
        switch (feature) {
            case CONFIGURATION:
                return resources.configFeature();
            case DATA_COLLECTION:
                return resources.dataCollectionFeature();
            case EVENT:
                return resources.eventFeature();
            case NOTIFICATION:
                return resources.notificationFeature();
            case PROFILING:
                return resources.profilingFeature();
            case USER_VERIFIER:
                return resources.userVerifierFeature();
        }
        return null;
    }
    
    public static String getFeatureBackgroundClass(Feature feature) {
        switch (feature) {
        case CONFIGURATION:
            return sandboxStyle.bgFeatureConfig();
        case DATA_COLLECTION:
            return sandboxStyle.bgFeatureDataCollection();
        case EVENT:
            return sandboxStyle.bgFeatureEvent();
        case NOTIFICATION:
            return sandboxStyle.bgFeatureNotification();
        case PROFILING:
            return sandboxStyle.bgFeatureProfiling();
        case USER_VERIFIER:
            return sandboxStyle.bgFeatureUserVerifier();
    }
    return null;
    }
    
    public static String getPlatformText(Platform platform) {
        switch(platform) {
            case ANDROID:
                return constants.android();
            case C:
                return constants.c();
            case CPP:
                return constants.cpp();
            case JAVA:
                return constants.java();
            case ESP8266:
                return constants.esp8266();
        }
        return null;
    }
    
    public static ImageResource getPlatformIcon(Platform platform) {
        switch(platform) {
            case ANDROID:
                return resources.androidPlatform();
            case C:
                return resources.cPlatform();
            case CPP:
                return resources.cppPlatform();
            case JAVA:
                return resources.javaPlatform();
            case ESP8266:
                return resources.esp8266Platform();
        }
        return null;
    }
    
    public static ImageResource getFilterPlatformIcon(Platform platform) {
        switch(platform) {
            case ANDROID:
                return resources.androidPlatformFilter();
            default:
                return getPlatformIcon(platform);
        }
    }
    
    public static String getPlatformBackgroundClass(Platform platform) {
        return sandboxStyle.bgPlatformCommon();
    }
    
    public static ImageResource getPlatformIconBig(Platform platform) {
        switch(platform) {
            case ANDROID:
                return resources.android();
            case C:
                return resources.c();
            case CPP:
                return resources.cpp();
            case JAVA:
                return resources.java();
            case ESP8266:
                return resources.esp8266();
        }
        return null;
    }

}
