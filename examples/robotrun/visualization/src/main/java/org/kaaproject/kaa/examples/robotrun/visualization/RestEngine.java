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

package org.kaaproject.kaa.examples.robotrun.visualization;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.EnumSymbol;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.server.common.admin.AdminClient;

public class RestEngine {
    
    private static final String ADMIN_HOST = "admin_host";
    private static final String ADMIN_PORT = "admin_port";
    private static final String ADMIN_USER = "admin_user";
    private static final String ADMIN_PASSWORD = "admin_password";
    
    private static final String REST_PROPERTIES_FILE = "rest.properties";
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private String adminHost;
    private int adminPort;
    private String adminUser;
    private String adminPassword;
    
    private AdminClient adminClient;
    
    private String appToken;
    private String endpointGroupId;
    
    private Schema baseSchema;
    private Schema bordersSchema;
    private Schema borderSchema;
    private Schema borderTypeSchema;
    
    private JsonEncoder jsonEncoder;
    private DatumWriter<GenericRecord> datumWriter;
    
    public RestEngine(String appToken) {
        this.appToken = appToken;
    }
    
    public void init() throws Exception {
        
        while (!loadProperties()) {
            editRestProperties();
        }
        
        adminClient = new AdminClient(adminHost, adminPort);
        adminClient.login(adminUser, adminPassword);
        
    }
    
    private ConfigurationDto getLastActiveConfiguration() throws Exception {
        
        if (endpointGroupId == null) {
            List<ApplicationDto> applications = adminClient.getApplications();
            ApplicationDto application = null;
            for (ApplicationDto app : applications) {
                if (app.getApplicationToken().equals(appToken)) {
                    application = app;
                    break;
                }
            }
            if (application == null) {
                throw new RuntimeException("Can't find application by application token " +  appToken);
            }
            List<EndpointGroupDto> endpointGroups = adminClient.getEndpointGroups(application.getId());
            EndpointGroupDto endpointGroup = null;
            for (EndpointGroupDto group : endpointGroups) {
                if (group.getWeight()==0) {
                    endpointGroup = group;
                }
            }
            if (endpointGroup == null) {
                throw new RuntimeException("Can't get default endpoint group for application with token " +  appToken);
            }
            endpointGroupId = endpointGroup.getId();
        }
        
        List<ConfigurationRecordDto> configurationRecords = adminClient.getConfigurationRecords(endpointGroupId, false);
        int version = -1;
        ConfigurationDto config = null;
        for (ConfigurationRecordDto configurationRecord : configurationRecords) {
            if (configurationRecord.getActiveConfiguration() != null && 
                    configurationRecord.getActiveConfiguration().getMajorVersion() > version) {
                    config = configurationRecord.getActiveConfiguration();
            }
        }
        return config;
    }
    
    public void createLabyrinth(int width, int height) throws Exception {
        ConfigurationDto configuration = getLastActiveConfiguration();
        configuration.setId(null);
        configuration.setBody(genLabyrinthJson(configuration, width, height));
        configuration.setStatus(UpdateStatus.INACTIVE);
        configuration = adminClient.editConfiguration(configuration);
        adminClient.activateConfiguration(configuration.getId());
    }
    
    private String genLabyrinthJson(ConfigurationDto configuration, int width, int height) throws Exception {
        if (baseSchema == null) {
            baseSchema = getBaseSchema(configuration);
            bordersSchema = baseSchema.getField("hBorders").schema().getTypes().get(1);
            borderSchema = bordersSchema.getElementType();
            borderTypeSchema = borderSchema.getField("type").schema().getTypes().get(1);
            datumWriter = new GenericDatumWriter<GenericRecord>(baseSchema);
        }
        GenericRecord labyrinthRecord = new GenericData.Record(baseSchema);
        
        GenericArray<GenericRecord> hBordersArray = new GenericData.Array<>((height+1)*width, bordersSchema);
        for (int y=0;y<height+1;y++) {
            for (int x=0;x<width;x++) {
                GenericRecord borderRecord = new GenericData.Record(borderSchema);
                borderRecord.put("x", x);
                borderRecord.put("y", y);
                EnumSymbol borderTypeEnum = new GenericData.EnumSymbol(borderTypeSchema, BorderType.UNKNOWN.name());
                borderRecord.put("type", borderTypeEnum);
                hBordersArray.add(borderRecord);
            }
        }

        GenericArray<GenericRecord> vBordersArray = new GenericData.Array<>(height*(width+1), bordersSchema);
        for (int y=0;y<height;y++) {
            for (int x=0;x<width+1;x++) {
                GenericRecord borderRecord = new GenericData.Record(borderSchema);
                borderRecord.put("x", x);
                borderRecord.put("y", y);
                EnumSymbol borderTypeEnum = new GenericData.EnumSymbol(borderTypeSchema, BorderType.UNKNOWN.name());
                borderRecord.put("type", borderTypeEnum);
                vBordersArray.add(borderRecord);
            }
        }
        
        labyrinthRecord.put("hBorders", hBordersArray);
        labyrinthRecord.put("vBorders", vBordersArray);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jsonEncoder = EncoderFactory.get().jsonEncoder(baseSchema, baos);
        datumWriter.write(labyrinthRecord, jsonEncoder);
        jsonEncoder.flush();
        baos.flush();
        
        return new String(baos.toByteArray(), UTF8);
    }
    
    private Schema getBaseSchema(ConfigurationDto configuration) throws Exception {
        ConfigurationSchemaDto configSchemaDto = adminClient.getConfigurationSchema(configuration.getSchemaId());
        String baseSchemaString = configSchemaDto.getBaseSchema();
        Parser parser = new Parser();
        return parser.parse(baseSchemaString);
    }
    
    private boolean loadProperties() throws Exception {
        Properties props = new Properties();
        File restPropertiesFile = new File(REST_PROPERTIES_FILE);
        if (!restPropertiesFile.exists()) {
            restPropertiesFile.createNewFile();
        }
        FileInputStream fis = new FileInputStream(restPropertiesFile);
        props.load(fis);
        fis.close();
        
        adminHost = props.getProperty(ADMIN_HOST);
        try {
            adminPort = Integer.valueOf(props.getProperty(ADMIN_PORT));
        }
        catch (Exception e) {}
        adminUser = props.getProperty(ADMIN_USER);
        adminPassword = props.getProperty(ADMIN_PASSWORD);
        
        return !strIsEmpty(adminHost) && adminPort > 0
                && !strIsEmpty(adminUser) && !strIsEmpty(adminPassword);
    }
    
    private void editRestProperties() throws Exception {
        JTextField adminHost = new JTextField();
        JTextField adminPort = new JTextField();
        JTextField adminUser = new JTextField();
        JPasswordField adminPassword = new JPasswordField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Admin host"),
                adminHost,
                new JLabel("Admin port"),
                adminPort,
                new JLabel("Admin user"),
                adminUser,
                new JLabel("Admin password"),
                adminPassword
        };
        JOptionPane.showMessageDialog(null, inputs, "Enter admin server parameters", JOptionPane.PLAIN_MESSAGE);
        String adminHostStr = adminHost.getText().trim();
        String adminPortStr = adminPort.getText().trim();
        String adminUserStr = adminUser.getText().trim();
        String adminPasswordStr = new String(adminPassword.getPassword()).trim();
        
        Properties props = new Properties();
        props.put(ADMIN_HOST, adminHostStr);
        props.put(ADMIN_PORT, adminPortStr);
        props.put(ADMIN_USER, adminUserStr);
        props.put(ADMIN_PASSWORD, adminPasswordStr);
        File restPropertiesFile = new File(REST_PROPERTIES_FILE);
        if (!restPropertiesFile.exists()) {
            restPropertiesFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(restPropertiesFile);
        props.store(fos, "Admin server parameters");
        fos.flush();
        fos.close();
    }
    
    private static boolean strIsEmpty(String str) {
        return str == null || str.length()==0;
    }
}
