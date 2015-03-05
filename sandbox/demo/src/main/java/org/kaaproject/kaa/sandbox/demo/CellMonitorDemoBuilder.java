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

import java.util.Arrays;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CellMonitorDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(CellMonitorDemoBuilder.class);
    
    protected CellMonitorDemoBuilder() {
        super();
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client)
            throws Exception {
        
        LOG.info("Loading 'Cell Monitor Demo Application' data...");
        
        loginTenantAdmin(client);
        
        ApplicationDto cellMonitorApplication = new ApplicationDto();
        cellMonitorApplication.setName("Cell monitor");
        cellMonitorApplication = client.editApplication(cellMonitorApplication);
        
        sdkKey.setApplicationId(cellMonitorApplication.getId());
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);
        
        loginTenantDeveloper(client);
        
        LogSchemaDto logSchema = new LogSchemaDto();
        logSchema.setApplicationId(cellMonitorApplication.getId());
        logSchema.setName("Cell monitor configuration schema");
        logSchema.setDescription("Log schema describing cell monitor record with information about current cell location, signal strength and phone gps location.");
        logSchema = client.createLogSchema(logSchema, "demo/cellmonitor/cell_monitor_log.avsc");
        sdkKey.setLogSchemaVersion(logSchema.getMajorVersion());
        
        LogAppenderDto cellMonitorLogAppender = new LogAppenderDto();
        cellMonitorLogAppender.setName("Cell monitor log appender");
        cellMonitorLogAppender.setDescription("Log appender used to deliver log records from cell monitor application to local mongo db instance");
        cellMonitorLogAppender.setApplicationId(cellMonitorApplication.getId());
        cellMonitorLogAppender.setApplicationToken(cellMonitorApplication.getApplicationToken());
        cellMonitorLogAppender.setTenantId(cellMonitorApplication.getTenantId());
        cellMonitorLogAppender.setMinLogSchemaVersion(1);
        cellMonitorLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        cellMonitorLogAppender.setConfirmDelivery(true);
        cellMonitorLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH, 
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        cellMonitorLogAppender.setPluginTypeName("Mongo");
        cellMonitorLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender");
        cellMonitorLogAppender.setJsonConfiguration(FileUtils.readResource("demo/cellmonitor/mongo_appender.json"));
        cellMonitorLogAppender = client.editLogAppenderDto(cellMonitorLogAppender);
        
        LOG.info("Finished loading 'Cell Monitor Demo Application' data.");
    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("cellmonitor_demo");
        projectConfig.setName("Cell monitor");
        projectConfig.setDescription("Cell monitor application on android platform demonstrating data collection feature");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/cellmonitor_demo.tar.gz");
        projectConfig.setProjectFolder("cellmonitor_demo/CellMonitor");
        projectConfig.setSdkLibDir("cellmonitor_demo/CellMonitor/libs");
        projectConfig.setDestBinaryFile("cellmonitor_demo/CellMonitor/bin/CellMonitor-debug.apk");
        projectConfigs.add(projectConfig);
    }

}
