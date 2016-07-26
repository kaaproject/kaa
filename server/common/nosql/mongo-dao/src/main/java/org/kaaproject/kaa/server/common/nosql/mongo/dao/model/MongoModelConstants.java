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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

public class MongoModelConstants {

    private MongoModelConstants() {
    }

    /**
     * Generic constants.
     */
    public static final String ID = "_id";
    public static final String BODY = "body";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ENDPOINT_KEY_HASH = "endpoint_key_hash";
    public static final String APPLICATION_ID = "application_id";

    /**
     * {@link EndpointGroupState} constants.
     */
    public static final String ENDPOINT_GROUP_ID = "endpoint_group_id";
    public static final String PROFILE_FILTER_ID = "profile_filter_id";
    public static final String CONFIGURATION_ID = "configuration_id";

    /**
     * {@link EventClassFamilyVersionState} constants
     */
    public static final String ECF_ID = "ecf_id";
    public static final String EVENT_CLASS_FAMILY_VERSION = "ecf_version";

    /**
     * {@link MongoEndpointConfiguration} constants
     */
    public static final String ENDPOINT_CONFIGURATION = "endpoint_configuration";

    /**
     * {@link MongoEndpointNotification} constants
     */
    public static final String ENDPOINT_NOTIFICATION = "endpoint_notification";
    public static final String EP_NF_ENDPOINT_KEY_HASH = ENDPOINT_KEY_HASH;

    /**
     * {@link MongoEndpointNotification} constants
     */
    public static final String NOTIFICATION = "notification";
    public static final String NF_APPLICATION_ID = APPLICATION_ID;
    public static final String NF_SCHEMA_ID = "notification_schema_id";
    public static final String NF_TOPIC_ID = "topic_id";
    public static final String NF_LAST_MODIFY_TIME = "last_modify_time";
    public static final String NF_TYPE = "notification_type";
    public static final String NF_EXPIRED_AT = "expired_at";
    public static final String NF_SEQ_NUM = "seq_num";
    public static final String NF_VERSION = "nf_version";
    public static final String NF_BODY = BODY;

    /**
     * {@link MongoEndpointUser} constants
     */
    public static final String ENDPOINT_USER = "endpoint_user";
    public static final String EP_USER_USERNAME = "username";
    public static final String EP_USER_EXTERNAL_ID = "external_id";
    public static final String EP_USER_TENANT_ID = "tenant_id";
    public static final String EP_USER_ACCESS_TOKEN = ACCESS_TOKEN;
    public static final String EP_USER_ENDPOINT_IDS = "endpoint_ids";

    /**
     * {@link MongoEndpointProfile} constants
     */
    public static final String ENDPOINT_PROFILE = "endpoint_profile";
    public static final String EP_APPLICATION_ID = APPLICATION_ID;
    public static final String EP_ENDPOINT_KEY = "endpoint_key";
    public static final String EP_ENDPOINT_KEY_HASH = ENDPOINT_KEY_HASH;
    public static final String EP_USER_ID = "endpoint_user_id";
    public static final String EP_ACCESS_TOKEN = ACCESS_TOKEN;
    public static final String EP_GROUP_STATE = "group_state";
    public static final String EP_SEQ_NUM = "seq_num";
    public static final String EP_CHANGED_FLAG = "changed_flag";
    public static final String EP_PROFILE_HASH = "profile_hash";
    public static final String EP_PROFILE_VERSION = "profile_version";
    public static final String EP_SERVER_PROFILE_VERSION_PROPERTY = "srv_profile_version";
    public static final String EP_CONFIGURATION_HASH = "configuration_hash";
    public static final String EP_USER_CONFIGURATION_HASH = "user_configuration_hash";
    public static final String EP_CONFIGURATION_VERSION = "configuration_version";
    public static final String EP_TOPIC_HASH = "topic_hash";
    public static final String EP_SIMPLE_TOPIC_HASH = "simple_topic_hash";
    public static final String EP_NOTIFICATION_VERSION = "ep_nf_version";
    public static final String EP_NF_HASH = "nf_hash";
    public static final String EP_SYSTEM_NF_VERSION = "system_nf_version";
    public static final String EP_USER_NF_VERSION = "user_nf_version";
    public static final String EP_LOG_SCHEMA_VERSION = "log_schema_version";
    public static final String EP_ECF_VERSION_STATE = "ecf_version_state";
    public static final String EP_SERVER_HASH = "server_hash";
    public static final String EP_SDK_TOKEN = "sdk_token";
    public static final String EP_USE_RAW_SCHEMA = "use_raw_schema";
    
    public static final String EP_SERVER_PROFILE_PROPERTY = "srv_profile";

    /**
     * {@link MongoTopicListEntry} constants
     */
    public static final String TOPIC_LIST_ENTRY = "topic_list_entry";
    public static final String TOPIC_LIST_SIMPLE_HASH = "simple_hash";
    public static final String TOPIC_LIST_TOPIC_IDS = "topic_ids";

    /**
     * MongoEndpointUserConfiguration constants.
     */
    public static final String USER_CONFIGURATION = "user_configuration";
    public static final String USER_CONF_USER_ID = "user_id";
    public static final String USER_CONF_APP_TOKEN = "app_token";
    public static final String USER_CONF_SCHEMA_VERSION = "schema_version";
    public static final String USER_CONF_BODY = BODY;

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointRegistration}
     */
    public static final String ENDPOINT_REGISTRATION = "endpoint_registration";
    public static final String EP_REGISTRATION_APPLICATION_ID = APPLICATION_ID;
    public static final String EP_REGISTRATION_ENDPOINT_ID = "endpoint_id";
    public static final String EP_REGISTRATION_CREDENTIALS_ID = "credentials_id";
    public static final String EP_REGISTRATION_SERVER_PROFILE_VERSION = "server_profile_version";
    public static final String EP_REGISTRATION_SERVER_PROFILE_BODY = "server_profile";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoCredentials}
     */
    public static final String CREDENTIALS = "credentials";
    public static final String CREDENTIALS_APPLICATION_ID = APPLICATION_ID;
    public static final String CREDENTIALS_ID = ID;
    public static final String CREDENTIALS_BODY = "credentials_body";
    public static final String CREDENTIAL_STATUS = "credentials_status";
}
