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
package org.kaaproject.kaa.server.common.dao.impl.sql;

public class HibernateDaoConstants {

    public static final String TENANT_PROPERTY = "tenant";
    public static final String ID_PROPERTY = "id";
    public static final String APPLICATION_TOKEN_PROPERTY = "applicationToken";
    public static final String APPLICATION_NAME_PROPERTY = "name";
    public static final String EXTERNAL_UID_PROPERTY = "externalUid";
    public static final String NAME_PROPERTY = "name";
    public static final String AUTHORITY_PROPERTY = "authority";
    public static final String PROFILE_SCHEMA_PROPERTY = "profileSchema";
    public static final String ENDPOINT_GROUP_PROPERTY = "endpointGroup";
    public static final String ENDPOINT_GROUPS_PROPERTY = "endpointGroups";
    public static final String STATUS_PROPERTY = "status";
    public static final String SEQUENCE_NUMBER_PROPERTY = "sequenceNumber";
    public static final String MAJOR_VERSION_PROPERTY = "majorVersion";
    public static final String MINOR_VERSION_PROPERTY = "minorVersion";
    public static final String APPLICATION_PROPERTY = "application";
    public static final String TOPIC_TYPE_PROPERTY = "type";
    public static final String WEIGHT_PROPERTY = "weight";
    public static final String TOPICS_PROPERTY = "topics";
    public static final String CONFIGURATION_SCHEMA_PROPERTY = "configurationSchema";
    public static final String ECF_PROPERTY = "ecf";
    public static final String VERSION_PROPERTY = "version";
    public static final String EVENT_CLASS_TYPE_PROPERTY = "type";
    public static final String FQN_PROPERTY = "fqn";
    public static final String CLASS_NAME_PROPERTY = "className";

    public static final String VERSION_PROPERY = "version";

    public static final String TENANT_ALIAS = "tenant";
    public static final String PROFILE_SCHEMA_ALIAS = "profileSchema";
    public static final String ENDPOINT_GROUP_ALIAS = "endpointGroup";
    public static final String APPLICATION_ALIAS = "application";
    public static final String TOPIC_ALIAS = "topic";
    public static final String CONFIGURATION_SCHEMA_ALIAS = "configurationSchema";
    public static final String ECF_ALIAS = "ecf";

    public static final String TENANT_REFERENCE = TENANT_ALIAS + "." + ID_PROPERTY;
    public static final String PROFILE_SCHEMA_REFERENCE = PROFILE_SCHEMA_ALIAS + "." + ID_PROPERTY;
    public static final String ENDPOINT_GROUP_REFERENCE = ENDPOINT_GROUP_ALIAS + "." + ID_PROPERTY;
    public static final String APPLICATION_REFERENCE = APPLICATION_ALIAS + "." + ID_PROPERTY;
    public static final String TOPIC_REFERENCE = TOPIC_ALIAS + "." + ID_PROPERTY;
    public static final String CONFIGURATION_SCHEMA_REFERENCE = CONFIGURATION_SCHEMA_ALIAS + "." + ID_PROPERTY;
    public static final String ECF_REFERENCE = ECF_ALIAS + "." + ID_PROPERTY;

    private HibernateDaoConstants() {
        throw new UnsupportedOperationException("Not supported");
    }
}
