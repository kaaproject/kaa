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

package org.kaaproject.kaa.sandbox.demo;

import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerPlantAndroidDemoBuilder extends AbstractDemoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PowerPlantAndroidDemoBuilder.class);

    protected PowerPlantAndroidDemoBuilder() {
        super("demo/powerplantandroid");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Power plant android application' data...");

        loginTenantAdmin(client);

        ApplicationDto powerPlantAndroidApplciation = new ApplicationDto();
        powerPlantAndroidApplciation.setName("Power plant demo android");
        powerPlantAndroidApplciation = client.editApplication(powerPlantAndroidApplciation);

        sdkKey.setApplicationId(powerPlantAndroidApplciation.getId());
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);
        sdkKey.setLogSchemaVersion(1);

        logger.info("Finished loading 'Power plant android application' data.");
    }
}
