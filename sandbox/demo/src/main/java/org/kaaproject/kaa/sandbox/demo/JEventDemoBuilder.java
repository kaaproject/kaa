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
package org.kaaproject.kaa.sandbox.demo;


import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;

public class JEventDemoBuilder extends  AbstractDemoBuilder{
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("notification_demo");
        projectConfig.setName("Android Notification Demo");
        projectConfig.setDescription("Application on android platform demonstrating notification subsystem (IoT)");
        projectConfig.setPlatform(Platform.JAVA);
        projectConfig.setSourceArchive("android/notification_demo.tar.gz");
        projectConfig.setProjectFolder("notification_demo/NotificationDemo");
        projectConfig.setSdkLibDir("notification_demo/NotificationDemo/libs");
        projectConfig.setDestBinaryFile("notification_demo/NotificationDemo/bin/Notification-debug.apk");
        projectConfigs.add(projectConfig);
    }
}
