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
package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.io.File;

import org.apache.avro.Schema;
import org.apache.commons.io.FileUtils;
import org.apache.flume.Context;
import org.apache.flume.conf.Configurable;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.flume.ConfigurationConstants;

import com.google.common.base.Preconditions;

public class AvroSchemaSource implements Configurable, ConfigurationConstants {

    public static final String SCHEMA_SOURCE = "flume.avro.schema.source";
    
    private String schemaSourceType;
    private String kaaRestHost;
    private int kaaRestPort;
    private String kaaRestUser;
    private String kaaRestPassword;
    private String schemaLocalRoot;
    
    private AdminClient adminClient;
    
    @Override
    public void configure(Context context) {
        schemaSourceType = context.getString(CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE, DEFAULT_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE);
        if (schemaSourceType.equals(SCHEMA_SOURCE_REST)) {
            kaaRestHost = context.getString(CONFIG_KAA_REST_HOST, DEFAULT_KAA_REST_HOST);
            kaaRestPort = context.getInteger(CONFIG_KAA_REST_PORT, DEFAULT_KAA_REST_PORT);
            kaaRestUser = context.getString(CONFIG_KAA_REST_USER);
            kaaRestPassword = context.getString(CONFIG_KAA_REST_PASSWORD);
            
            Preconditions.checkArgument(kaaRestUser != null && kaaRestUser.length() > 0,
                    CONFIG_KAA_REST_USER + " must be specified for " + SCHEMA_SOURCE_REST + " avro schema source");
            Preconditions.checkArgument(kaaRestPassword != null && kaaRestPassword.length() > 0,
                    CONFIG_KAA_REST_PASSWORD + " must be specified for " + SCHEMA_SOURCE_REST + " avro schema source");
            
            adminClient = new AdminClient(kaaRestHost, kaaRestPort);
            adminClient.login(kaaRestUser, kaaRestPassword);

        }
        else {
            schemaLocalRoot = context.getString(CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT);

            Preconditions.checkArgument(schemaLocalRoot != null && schemaLocalRoot.length() > 0,
                    CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT + " must be specified for " + SCHEMA_SOURCE_LOCAL + " avro schema source");
        }
    }
    
    public Schema loadByKey(KaaSinkKey key) throws Exception {
        Schema schema = null;
        String schemaString = null;
        if (schemaSourceType.equals(SCHEMA_SOURCE_REST)) {
            LogSchemaDto logSchemaDto = adminClient.getLogSchemaByApplicationTokenAndSchemaVersion(key.getApplicationToken(), key.getSchemaVersion());
            schemaString = logSchemaDto.getSchema();
        }
        else {
            String applicationToken = key.getApplicationToken();
            int version = key.getSchemaVersion();
            String separator = System.getProperty("file.separator");
            File schemaFile = new File(schemaLocalRoot + separator + applicationToken + separator + "schema_v"+version);
            if (schemaFile.exists()) {
                schemaString = FileUtils.readFileToString(schemaFile);
            }
        }
        if (schemaString != null) {
            Schema.Parser parser = new Schema.Parser();
            schema = parser.parse(schemaString);
        }
        return schema;
    }

}
