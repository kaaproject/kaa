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

public class JDataCollectionDemobuider extends AbstractDemoBuilder{
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("jdatacollection_demo");
        projectConfig.setName("Java Notification Demo");
        projectConfig.setDescription("Application on java platform demonstrating data collection subsystem (IoT)");
        projectConfig.setPlatform(Platform.JAVA);
        projectConfig.setSourceArchive("java/jdatacollection_demo.tar.gz");
        projectConfig.setProjectFolder("jdatacollection_demo/JDataCollectionDemo");
        projectConfig.setSdkLibDir("jdatacollection_demo/JDataCollectionDemo/lib");
        projectConfig.setDestBinaryFile("jdatacollection_demo/JDataCollectionDemo/bin/JDataCollectionDemo.jar");
        projectConfigs.add(projectConfig);
    }
}
