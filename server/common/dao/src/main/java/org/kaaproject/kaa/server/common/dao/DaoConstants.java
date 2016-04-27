/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao;

public class DaoConstants {

    public static final String TENANT_PROPERTY = "tenant";
    public static final String ID_PROPERTY = "id";
    public static final String APPLICATION_TOKEN_PROPERTY = "applicationToken";
    public static final String APPLICATION_NAME_PROPERTY = "name";
    public static final String EXTERNAL_UID_PROPERTY = "externalUid";
    public static final String NAME_PROPERTY = "name";
    public static final String AUTHORITY_PROPERTY = "authority";
    public static final String ENDPOINT_PROFILE_SCHEMA_PROPERTY = "endpointProfileSchema";
    public static final String SERVER_PROFILE_SCHEMA_PROPERTY = "serverProfileSchema";
    public static final String ENDPOINT_GROUP_PROPERTY = "endpointGroup";
    public static final String ENDPOINT_GROUPS_PROPERTY = "endpointGroups";
    public static final String STATUS_PROPERTY = "status";
    public static final String SEQUENCE_NUMBER_PROPERTY = "sequenceNumber";
    public static final String APPLICATION_PROPERTY = "application";
    public static final String TOPIC_TYPE_PROPERTY = "type";
    public static final String WEIGHT_PROPERTY = "weight";
    public static final String TOPICS_PROPERTY = "topics";
    public static final String CONFIGURATION_SCHEMA_PROPERTY = "configurationSchema";
    public static final String ECF_PROPERTY = "ecf";
    public static final String VERSION_PROPERTY = "version";
    public static final String SCHEMA_VERSION_PROPERTY = "schemaVersion";
    public static final String EVENT_CLASS_TYPE_PROPERTY = "type";
    public static final String FQN_PROPERTY = "fqn";
    public static final String CLASS_NAME_PROPERTY = "className";
    public static final String SDK_TOKEN_PROPERTY = "token";
    public static final String CREATED_TIME_PROPERTY = "createdTime";

    public static final String TENANT_ALIAS = "tenant";
    public static final String ENDPOINT_PROFILE_SCHEMA_ALIAS = "endpointProfileSchema";
    public static final String SERVER_PROFILE_SCHEMA_ALIAS = "serverProfileSchema";
    public static final String ENDPOINT_GROUP_ALIAS = "endpointGroup";
    public static final String APPLICATION_ALIAS = "application";
    public static final String TOPIC_ALIAS = "topic";
    public static final String CONFIGURATION_SCHEMA_ALIAS = "configurationSchema";
    public static final String ECF_ALIAS = "ecf";

    public static final String TENANT_REFERENCE = TENANT_ALIAS + "." + ID_PROPERTY;
    public static final String ENDPOINT_PROFILE_SCHEMA_REFERENCE = ENDPOINT_PROFILE_SCHEMA_ALIAS + "." + ID_PROPERTY;
    public static final String SERVER_PROFILE_SCHEMA_REFERENCE = SERVER_PROFILE_SCHEMA_ALIAS + "." + ID_PROPERTY;
    public static final String ENDPOINT_PROFILE_SCHEMA_VERSION_REFERENCE = ENDPOINT_PROFILE_SCHEMA_ALIAS + "." + VERSION_PROPERTY;
    public static final String SERVER_PROFILE_SCHEMA_VERSION_REFERENCE = SERVER_PROFILE_SCHEMA_ALIAS + "." + VERSION_PROPERTY;
    public static final String ENDPOINT_GROUP_REFERENCE = ENDPOINT_GROUP_ALIAS + "." + ID_PROPERTY;
    public static final String APPLICATION_REFERENCE = APPLICATION_ALIAS + "." + ID_PROPERTY;
    public static final String TOPIC_REFERENCE = TOPIC_ALIAS + "." + ID_PROPERTY;
    public static final String CONFIGURATION_SCHEMA_REFERENCE = CONFIGURATION_SCHEMA_ALIAS + "." + ID_PROPERTY;
    public static final String ECF_REFERENCE = ECF_ALIAS + "." + ID_PROPERTY;

    public static final String OPTIMISTIC_LOCK_PROPERTY = "OPT_LOCK";

    /**
     * Common constants.
     */
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SEQUENCE_NUMBER = "sequence_number";
    public static final String TENANT_ID = "tenant_id";
    public static final String DESCRIPTION = "description";
    public static final String CREATED_TIME = "created_time";
    public static final String LAST_MODIFY_TIME = "last_modify_time";
    public static final String ACTIVATED_TIME = "activated_time";
    public static final String DEACTIVATED_TIME = "deactivated_time";
    public static final String APPLICATION_ID = "application_id";
    public static final String ENDPOINT_GROUP_ID = "endpoint_group_id";
    public static final String SCHEMA_ID = "schems_id";
    public static final String ENDPOINT_COUNT = "endpoint_count";
    public static final String CREATED_USERNAME = "created_username";
    public static final String TOPIC_ID = "topic_id";
    public static final String CHANGE_ID = "changes_id";
    public static final String CONFIGURATION_ID = "configuration_id";
    public static final String PROFILE_FILTER_ID = "profile_filter_id";
    public static final String EVENT_CLASS_FAMILY_ID = "events_class_family_id";
    public static final String FQN = "fqn";
    public static final String VERSION = "version";

    /**
     * Tenant constants.
     */
    public static final String TENANT_TABLE_NAME = "tenant";
    public static final String TENANT_ENTITY_NAME = "tenant";
    public static final String TENANT_NAME = NAME;

    /**
     * Application constants.
     */
    public static final String APPLICATION_TABLE_NAME = "application";
    public static final String APPLICATION_APPLICATION_TOKEN = "application_token";
    public static final String APPLICATION_NAME = NAME;
    public static final String APPLICATION_SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    public static final String APPLICATION_TENANT_ID = TENANT_ID;
    public static final String APPLICATION_CREDENTIALS_SERVICE_NAME = "credentials_service";

    /**
     * User constants.
     */
    public static final String USER_TABLE_NAME = "kaa_user";
    public static final String USER_NAME = "user_name";
    public static final String USER_TENANT_ID = TENANT_ID;
    public static final String USER_EXTERNAL_UID = "external_uid";
    public static final String USER_AUTHORITY = "authority";

    /**
     * Abstract structure constants.
     */
    public static final String ABSTRACT_STRUCTURE_TABLE_NAME = "abstract_structure";
    public static final String ABSTRACT_STRUCTURE_SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    public static final String ABSTRACT_STRUCTURE_SCHEMA_VERSION = "schems_version";
    public static final String ABSTRACT_STRUCTURE_DESCRIPTION = DESCRIPTION;
    public static final String ABSTRACT_STRUCTURE_CREATED_TIME = CREATED_TIME;
    public static final String ABSTRACT_STRUCTURE_LAST_MODIFY_TIME = LAST_MODIFY_TIME;
    public static final String ABSTRACT_STRUCTURE_ACTIVATED_TIME = ACTIVATED_TIME;
    public static final String ABSTRACT_STRUCTURE_DEACTIVATED_TIME = DEACTIVATED_TIME;
    public static final String ABSTRACT_STRUCTURE_CREATED_USERNAME = CREATED_USERNAME;
    public static final String ABSTRACT_STRUCTURE_MODIFIED_USERNAME = "modified_username";
    public static final String ABSTRACT_STRUCTURE_ACTIVATED_USERNAME = "activated_username";
    public static final String ABSTRACT_STRUCTURE_DEACTIVATED_USERNAME = "deactivated_username";
    public static final String ABSTRACT_STRUCTURE_ENDPOINT_COUNT = ENDPOINT_COUNT;
    public static final String ABSTRACT_STRUCTURE_STATUS = "status";
    public static final String ABSTRACT_STRUCTURE_APPLICATION_ID = APPLICATION_ID;
    public static final String ABSTRACT_STRUCTURE_ENDPOINT_GROUP_ID = ENDPOINT_GROUP_ID;
    public static final String ABSTRACT_STRUCTURE_OPTIMISTIC_LOCK = "optimistic_lock";


    /**
     * Base schems constants.
     */
    public static final String BASE_SCHEMA_TABLE_NAME = "base_schems";
    public static final String BASE_SCHEMA_VERSION = VERSION;
    public static final String BASE_SCHEMA_NAME = NAME;
    public static final String BASE_SCHEMA_DESCRIPTION = DESCRIPTION;
    public static final String BASE_SCHEMA_CREATED_USERNAME = CREATED_USERNAME;
    public static final String BASE_SCHEMA_CREATED_TIME = CREATED_TIME;
    public static final String BASE_SCHEMA_APPLICATION_ID = APPLICATION_ID;
    public static final String BASE_SCHEMA_CTL_SCHEMA_ID = "ctl_id";
    // Foreign keys constants.
    public static final String BASE_SCHEMA_FK_APP_ID = "fk_server_pf_schems_app_id";
    public static final String BASE_SCHEMA_FK_CTL_SCHEMA_ID = "fk_server_pf_schems_ctl_id";

    /**
     * Profile schems constants.
     */
    public static final String PROFILE_SCHEMA_TABLE_NAME = "profile_schems";

    /**
     * Server profile schems constants.
     */
    public static final String SERVER_PROFILE_SCHEMA_TABLE_NAME = "server_profile_schems";
    public static final String SERVER_PROFILE_SCHEMA_APP_ID = APPLICATION_ID;
    public static final String SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID = "ctl_id";
    public static final String SERVER_PROFILE_SCHEMA_CREATED_TIME = CREATED_TIME;
    // Foreign keys constants.
    public static final String SERVER_PROFILE_SCHEMA_FK_APP_ID = "fk_server_pf_schems_app_id";
    public static final String SERVER_PROFILE_SCHEMA_FK_CTL_SCHEMA_ID = "fk_server_pf_schems_ctl_id";


    /**
     * Profile filter constants.
     */
    public static final String PROFILE_FILTER_TABLE_NAME = "profile_filter";
    public static final String PROFILE_FILTER_BODY = "profile_filter_body";
    public static final String PROFILE_FILTER_ENDPOINT_SCHEMA_ID = "endpoint_schems_id";
    public static final String PROFILE_FILTER_SERVER_SCHEMA_ID = "server_schems_id";

    /**
     * Endpoint group constants.
     */
    public static final String ENDPOINT_GROUP_TABLE_NAME = "endpoint_group";
    public static final String ENDPOINT_GROUP_TOPICS_TABLE_NAME = "endpoint_group_topic";
    public static final String ENDPOINT_GROUP_WEIGHT = "weight";
    public static final String ENDPOINT_GROUP_APPLICATION_ID = APPLICATION_ID;
    public static final String ENDPOINT_GROUP_NAME = NAME;
    public static final String ENDPOINT_GROUP_SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    public static final String ENDPOINT_GROUP_ENDPOINT_COUNT = ENDPOINT_COUNT;
    public static final String ENDPOINT_GROUP_DESCRIPTION = DESCRIPTION;
    public static final String ENDPOINT_GROUP_CREATED_USERNAME = CREATED_USERNAME;
    public static final String ENDPOINT_GROUP_CREATED_TIME = CREATED_TIME;
    public static final String ENDPOINT_GROUP_TOPICS_TOPIC_ID = TOPIC_ID;
    public static final String ENDPOINT_GROUP_TOPICS_ENDPOINT_GROUP_ID = ENDPOINT_GROUP_ID;

    /**
     * Schema constants.
     */
    public static final String SCHEMA_TABLE_NAME = "schems";
    public static final String SCHEMA_VERSION = VERSION;
    public static final String SCHEMA_SCHEMA = "schems";
    public static final String SCHEMA_NAME = NAME;
    public static final String SCHEMA_DESCRIPTION = DESCRIPTION;
    public static final String SCHEMA_CREATED_USERNAME = CREATED_USERNAME;
    public static final String SCHEMA_CREATED_TIME = CREATED_TIME;
    public static final String SCHEMA_ENDPOINT_COUNT = ENDPOINT_COUNT;
    public static final String SCHEMA_APPLICATION_ID = APPLICATION_ID;

    /**
     * Configuration schems constants.
     */
    public static final String CONFIGURATION_SCHEMA_TABLE_NAME = "configuration_schems";
    public static final String CONFIGURATION_SCHEMA_BASE_SCHEMA = "base_schems";
    public static final String CONFIGURATION_SCHEMA_PROTOCOL_SCHEMA = "protocol_schems";
    public static final String CONFIGURATION_SCHEMA_OVERRIDE_SCHEMA = "override_schems";

    /**
     * Configuration constants.
     */
    public static final String CONFIGURATION_TABLE_NAME = "configuration";
    public static final String CONFIGURATION_CONFIGURATION_BODY = "configuration_body";
    public static final String CONFIGURATION_CONFIGURATION_SCHEMA_ID = "configuration_schems_id";
    public static final String CONFIGURATION_CONFIGURATION_SCHEMA_VERSION = "configuration_schems_version";

    /**
     * Topic constants.
     */
    public static final String TOPIC_TABLE_NAME = "topic";
    public static final String TOPIC_NAME = NAME;
    public static final String TOPIC_DESCRIPTION = DESCRIPTION;
    public static final String TOPIC_CREATED_USERNAME = CREATED_USERNAME;
    public static final String TOPIC_CREATED_TIME = CREATED_TIME;
    public static final String TOPIC_SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    public static final String TOPIC_TYPE = "type";
    public static final String TOPIC_APPLICATION_ID = APPLICATION_ID;

    /**
     * History constants.
     */
    public static final String HISTORY_TABLE_NAME = "history";
    public static final String HISTORY_SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    public static final String HISTORY_LAST_MODIFY_TIME = LAST_MODIFY_TIME;
    public static final String HISTORY_APPLICATION_ID = APPLICATION_ID;
    public static final String HISTORY_CHANGE_ID = CHANGE_ID;

    /**
     * Change constants.
     */
    public static final String CHANGE_TABLE_NAME = "changes";
    public static final String CHANGE_PROFILE_FILTER_VERSION = "profile_filter_version";
    public static final String CHANGE_CONFIGURATION_VERSION = "configuration_version";
    public static final String CHANGE_TYPE = "type";
    public static final String CHANGE_ENDPOINT_GROUP_ID = ENDPOINT_GROUP_ID;
    public static final String CHANGE_TOPIC_ID = TOPIC_ID;
    public static final String CHANGE_CONFIGURATION_ID = CONFIGURATION_ID;
    public static final String CHANGE_PROFILE_FILTER_ID = PROFILE_FILTER_ID;

    /**
     * Event schems version constants.
     */
    public static final String EVENT_SCHEMA_VERSION_TABLE_NAME = "event_schems_versions";
    public static final String EVENT_SCHEMA_VERSION_SCHEMA = "schems";
    public static final String EVENT_SCHEMA_VERSION_VERSION = VERSION;
    public static final String EVENT_SCHEMA_VERSION_CREATED_USERNAME = CREATED_USERNAME;
    public static final String EVENT_SCHEMA_VERSION_CREATED_TIME = CREATED_TIME;
    public static final String EVENT_SCHEMA_VERSION_EVENT_CLASS_FAMILY_ID = EVENT_CLASS_FAMILY_ID;

    /**
     * Event class family constants.
     */
    public static final String EVENT_CLASS_FAMILY_TABLE_NAME = "events_class_family";
    public static final String EVENT_CLASS_FAMILY_TENANT_ID = TENANT_ID;
    public static final String EVENT_CLASS_FAMILY_NAME = NAME;
    public static final String EVENT_CLASS_FAMILY_NAMESPACE = "namespace";
    public static final String EVENT_CLASS_FAMILY_CLASS_NAME = "class_name";
    public static final String EVENT_CLASS_FAMILY_DESCRIPTION = DESCRIPTION;
    public static final String EVENT_CLASS_FAMILY_CREATED_USERNAME = CREATED_USERNAME;
    public static final String EVENT_CLASS_FAMILY_CREATED_TIME = CREATED_TIME;

    /**
     * Event class constants.
     */
    public static final String EVENT_CLASS_TABLE_NAME = "events_class";
    public static final String EVENT_CLASS_TENANT_ID = TENANT_ID;
    public static final String EVENT_CLASS_EVENT_CLASS_FAMILY_ID = EVENT_CLASS_FAMILY_ID;
    public static final String EVENT_CLASS_FQN = FQN;
    public static final String EVENT_CLASS_TYPE = "type";
    public static final String EVENT_CLASS_SCHEMA = "schems";
    public static final String EVENT_CLASS_VERSION = VERSION;

    /**
     * Application event map constants.
     */
    public static final String APPLICATION_EVENT_MAP_TABLE_NAME = "application_event_map";
    public static final String APPLICATION_EVENT_MAP_EVENT_CLASS_ID = "events_class_id";
    public static final String APPLICATION_EVENT_MAP_FQN = FQN;
    public static final String APPLICATION_EVENT_MAP_ACTION = "action";
    public static final String APPLICATION_EVENT_MAP_APPLICATION_EVENT_FAMILY_MAP_ID = "application_event_family_map_id";

    /**
     * Application event family map constants.
     */
    public static final String APPLICATION_EVENT_FAMILY_MAP_TABLE_NAME = "application_event_family_map";
    public static final String APPLICATION_EVENT_FAMILY_MAP_APPLICATION_ID = APPLICATION_ID;
    public static final String APPLICATION_EVENT_FAMILY_MAP_EVENT_CLASS_FAMILY_ID = EVENT_CLASS_FAMILY_ID;
    public static final String APPLICATION_EVENT_FAMILY_MAP_VERSION = VERSION;
    public static final String APPLICATION_EVENT_FAMILY_MAP_CREATED_USERNAME = CREATED_USERNAME;
    public static final String APPLICATION_EVENT_FAMILY_MAP_CREATED_TIME = CREATED_TIME;


    /**
     * Plugin constants.
     */
    public static final String PLUGIN_TABLE_NAME = "plugin";
    public static final String PLUGIN_NAME = NAME;
    public static final String PLUGIN_DESCRIPTION = DESCRIPTION;
    public static final String PLUGIN_CREATED_USERNAME = CREATED_USERNAME;
    public static final String PLUGIN_CREATED_TIME = CREATED_TIME;
    public static final String PLUGIN_APPLICATION_ID = APPLICATION_ID;
    public static final String PLUGIN_TYPE_NAME = "plugin_type_name";
    public static final String PLUGIN_CLASS_NAME = "plugin_class_name";
    public static final String PLUGIN_RAW_CONFIGURATION = "raw_configuration";

    /**
     * User verifier constants.
     */
    public static final String USER_VERIFIER_TABLE_NAME = "user_verifier";
    public static final String USER_VERIFIER_TOKEN = "verifier_token";
    public static final String USER_VERIFIER_TOKEN_PROPERTY = "verifierToken";

    /**
     * Log schems constants.
     */
    public static final String LOG_SCHEMA_TABLE_NAME = "log_schems";

    /**
     * Notification schems constants
     */
    public static final String NOTIFICATION_SCHEMA_TABLE_NAME = "notification_schems";
    public static final String NOTIFICATION_SCHEMA_TYPE_PROPERTY = "type";

    /**
     * Log appender constants.
     */
    public static final String LOG_APPENDER_TABLE_NAME = "log_appender";
    public static final String LOG_APPENDER_MIN_LOG_SCHEMA_VERSION = "min_log_schems_version";
    public static final String LOG_APPENDER_MAX_LOG_SCHEMA_VERSION = "max_log_schems_version";
    public static final String LOG_APPENDER_CONFIRM_DELIVERY = "confirm_delivery";

    /**
     * SDK profile constants
     */
    public static final String SDK_PROFILE_APPLICATION_ID = APPLICATION_ID;
    public static final String SDK_PROFILE_CONFIGURATION_SCHEMA_VERSION = "configuration_schems_version";
    public static final String SDK_PROFILE_CREATED_TIME = CREATED_TIME;
    public static final String SDK_PROFILE_CREATED_USERNAME = CREATED_USERNAME;
    public static final String SDK_PROFILE_DEFAULT_VERIFIER_TOKEN = "default_verifier_token";
    public static final String SDK_PROFILE_ENDPOINT_COUNT = ENDPOINT_COUNT;
    public static final String SDK_PROFILE_LOG_SCHEMA_VERSION = "log_schems_version";
    public static final String SDK_PROFILE_NAME = NAME;
    public static final String SDK_PROFILE_NOTIFICATION_SCHEMA_VERSION = "notification_schems_version";
    public static final String SDK_PROFILE_PROFILE_SCHEMA_VERSION = "profile_schems_version";
    public static final String SDK_PROFILE_TABLE_NAME = "sdk_token";
    public static final String SDK_PROFILE_TOKEN = "token";

    /**
     * CTL schems constants
     */
    public static final String CTL_SCHEMA_TABLE_NAME = "ctl";
    public static final String CTL_SCHEMA_META_INFO_ID = "metainfo_id";
    public static final String CTL_SCHEMA_VERSION = VERSION;
    public static final String CTL_SCHEMA_BODY = "body";
    public static final String CTL_SCHEMA_DEFAULT_RECORD = "default_record";
    public static final String CTL_SCHEMA_CREATED_USERNAME = CREATED_USERNAME;
    public static final String CTL_SCHEMA_CREATED_TIME = CREATED_TIME;
    // CTL schems foreign keys
    public static final String CTL_SCHEMA_META_INFO_FK = "fk_ctl_metainfo_id";
    public static final String CTL_SCHEMA_JOIN_TABLE_PARENT_FK = "fk_ctl_pr_id";
    public static final String CTL_SCHEMA_JOIN_TABLE_CHILD_FK = "fk_ctl_ch_id";
    // CTL schems constraints
    public static final String CTL_SCHEMA_UNIQUE_CONSTRAINT = "ctl_unique_constraint";
    public static final String CTL_SCHEMA_JOIN_TABLE_NAME = "ctl_dependency";
    public static final String CTL_SCHEMA_JOIN_TABLE_PARENT_ID = "parent_id";
    public static final String CTL_SCHEMA_JOIN_TABLE_CHILD_ID = "child_id";
    // Properties and alias constants
    public static final String CTL_SCHEMA_DEPENDENCY_PROP = "dependencySet";
    public static final String CTL_SCHEMA_DEPENDENCY_ALIAS = "dep";
    public static final String CTL_SCHEMA_DEPENDENCY_ID_ALIAS = CTL_SCHEMA_DEPENDENCY_ALIAS + "." + ID;

    /**
     * CTL schems meta info constants
     */
    public static final String CTL_SCHEMA_META_INFO_TABLE_NAME = "ctl_metainfo";
    public static final String CTL_SCHEMA_META_INFO_FQN = FQN;
    public static final String CTL_SCHEMA_META_INFO_TENANT_ID = TENANT_ID;
    public static final String CTL_SCHEMA_META_INFO_APPLICATION_ID = APPLICATION_ID;
    public static final String CTL_SCHEMA_META_INFO_UNIQUE_CONSTRAINT = "ctl_metainfo_unique_constraint";

    // CTL schems meta info foreign keys
    public static final String CTL_SCHEMA_META_INFO_TENANT_FK = "fk_ctl_metainfo_tenant_id";
    public static final String CTL_SCHEMA_META_INFO_APPLICATION_FK = "fk_ctl_metainfo_app_id";

    // Properties and alias constants

    // Properties and alias constants
    public static final String CTL_SCHEMA_META_INFO_TENANT_ID_ALIAS = TENANT_REFERENCE;
    public static final String CTL_SCHEMA_META_INFO_APPLICATION_ID_ALIAS = APPLICATION_REFERENCE;
    public static final String CTL_SCHEMA_META_INFO_PROPERTY = "metaInfo";
    public static final String CTL_SCHEMA_META_INFO_ALIAS = "mi";
    public static final String CTL_SCHEMA_META_INFO_ALIAS_FQN = CTL_SCHEMA_META_INFO_ALIAS + "." + CTL_SCHEMA_META_INFO_FQN;
    public static final String CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID = CTL_SCHEMA_META_INFO_ALIAS + "." + TENANT_REFERENCE;
    public static final String CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID = CTL_SCHEMA_META_INFO_ALIAS + "." + APPLICATION_REFERENCE;
    public static final String CTL_SCHEMA_META_INFO_ALIAS_ID = CTL_SCHEMA_META_INFO_ALIAS + "." + ID_PROPERTY;

    public static final String LAST_PAGE_MESSAGE = "It is the last page";
    public static final String PROFILE = "profile";
    public static final String OPT_LOCK = "opt_lock";
    public static final String APPLIED = "[applied]";

    private DaoConstants() {
        throw new UnsupportedOperationException("Not supported");
    }
}
