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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

public class CassandraModelConstants {

    private CassandraModelConstants() {
    }

    /**
     * Generic constants.
     */
    public static final String APPLICATION_ID_PROPERTY = "app_id";
    public static final String CONFIGURATION_HASH_PROPERTY = "cf_hash";
    public static final String USER_CONFIGURATION_HASH_PROPERTY = "ucf_hash";
    public static final String ACCESS_TOKEN_PROPERTY  = "access_token";
    public static final String NOTIFICATION_ID_PROPERTY = "nf_id";
    public static final String ENDPOINT_KEY_HASH_PROPERTY = "ep_key_hash";
    public static final String USER_ID_PROPERTY = "user_id";
    public static final String NOTIFICATION_TYPE_PROPERTY = "nf_type";
    public static final String SEQ_NUM_PROPERTY = "seq_num";
    public static final String BODY_PROPERTY = "body";
    public static final String EXPIRED_AT_PROPERTY = "expired_at";
    public static final String LAST_MOD_TIME_PROPERTY = "last_mod_time";
    public static final String SCHEMA_ID_PROPERTY = "schema_id";
    public static final String TOPIC_ID_PROPERTY = "topic_id";
    public static final String SDK_TOKEN_PROPERTY = "sdk_token";
    public static final String KEY_DELIMITER= "::";

    /**
     * Cassandra Endpoint Notification constants.
     */
    public static final String ET_NF_COLUMN_FAMILY_NAME = "ep_nfs";
    public static final String ET_NF_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;
    public static final String ET_NF_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String ET_NF_ID_PROPERTY = NOTIFICATION_ID_PROPERTY;
    public static final String ET_NF_NOTIFICATION_TYPE_PROPERTY = NOTIFICATION_TYPE_PROPERTY;
    public static final String ET_NF_SEQ_NUM_PROPERTY = SEQ_NUM_PROPERTY;
    public static final String ET_NF_BODY_PROPERTY = BODY_PROPERTY;
    public static final String ET_NF_EXPIRED_AT_PROPERTY = EXPIRED_AT_PROPERTY;
    public static final String ET_NF_VERSION_PROPERTY = "ep_nf_version";
    public static final String ET_NF_LAST_MOD_TIME_PROPERTY = LAST_MOD_TIME_PROPERTY;
    public static final String ET_NF_SCHEMA_ID_PROPERTY = SCHEMA_ID_PROPERTY;
    public static final String ET_NF_TOPIC_ID_PROPERTY = TOPIC_ID_PROPERTY;

    /**
     * Cassandra notification constants.
     */
    public static final String NF_COLUMN_FAMILY_NAME = "notification";
    public static final String NF_TOPIC_ID_PROPERTY = "topic_id";
    public static final String NF_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String NF_NOTIFICATION_ID_PROPERTY = NOTIFICATION_ID_PROPERTY;
    public static final String NF_SCHEMA_ID_PROPERTY = SCHEMA_ID_PROPERTY;
    public static final String NF_VERSION_PROPERTY = "nf_version";
    public static final String NF_LAST_MOD_TIME_PROPERTY = LAST_MOD_TIME_PROPERTY;
    public static final String NF_NOTIFICATION_TYPE_PROPERTY = NOTIFICATION_TYPE_PROPERTY;
    public static final String NF_BODY_PROPERTY = BODY_PROPERTY;
    public static final String NF_EXPIRED_AT_PROPERTY = EXPIRED_AT_PROPERTY;
    public static final String NF_SEQ_NUM_PROPERTY = SEQ_NUM_PROPERTY;

    /**
     * Cassandra endpoint configuration constants.
     */
    public static final String ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME = "ep_conf";
    public static final String ENDPOINT_CONFIGURATION_CONF_HASH_PROPERTY = CONFIGURATION_HASH_PROPERTY;
    public static final String ENDPOINT_CONFIGURATION_CONF_PROPERTY = "cf";
    public static final String ENDPOINT_CONFIGURATION_CONF_ID_PROPERTY = "cf_id";

    /**
     * Cassandra endpoint profile constants.
     */
    public static final String EP_COLUMN_FAMILY_NAME = "ep_profile";
    public static final String EP_APP_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String EP_CONFIG_HASH_PROPERTY = CONFIGURATION_HASH_PROPERTY;
    public static final String EP_USER_CONFIG_HASH_PROPERTY = USER_CONFIGURATION_HASH_PROPERTY;
    public static final String EP_ACCESS_TOKEN_PROPERTY = ACCESS_TOKEN_PROPERTY;
    public static final String EP_ENDPOINT_ID_PROPERTY = "ep_id";
    public static final String EP_EP_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;
    public static final String EP_EP_KEY_PROPERTY = "ep_key";
    public static final String EP_USER_ID_PROPERTY = USER_ID_PROPERTY;
    public static final String EP_GROUP_STATE_PROPERTY = "group_state";
    public static final String EP_SEQUENCE_NUMBER_PROPERTY = "seq_num";
    public static final String EP_PROFILE_PROPERTY = "pf";
    public static final String EP_PROFILE_HASH_PROPERTY = "pf_hash";
    public static final String EP_PROFILE_VERSION_PROPERTY = "pf_ver";
    public static final String EP_SERVER_PROFILE_VERSION_PROPERTY = "srv_pf_ver";
    public static final String EP_CONFIGURATION_VERSION_PROPERTY = "cf_ver";
    public static final String EP_NOTIFICATION_VERSION_PROPERTY = "nf_ver";
    public static final String EP_TOPIC_HASH_PROPERTY = "topic_hash";
    public static final String EP_SIMPLE_TOPIC_HASH_PROPERTY = "simple_topic_hash";
    public static final String EP_SUBSCRIPTIONS_PROPERTY = "subscs";
    public static final String EP_SYSTEM_NOTIFICATION_VERSION_PROPERTY = "sys_nf_ver";
    public static final String EP_USER_NOTIFICATION_VERSION_PROPERTY = "user_nf_ver";
    public static final String EP_LOG_SCHEMA_VERSION_PROPERTY = "log_schema_ver";
    public static final String EP_ECF_VERSION_STATE_PROPERTY = "ecf_ver_state";
    public static final String EP_SERVER_HASH_PROPERTY = "server_hash";
    public static final String EP_SDK_TOKEN_PROPERTY = SDK_TOKEN_PROPERTY;
    public static final String EP_USE_RAW_SCHEMA = "use_raw_schema";
    public static final String EP_SERVER_PROFILE_PROPERTY = "srv_pf";

    /**
     * Cassandra endpoint user constants.
     */
    public static final String EP_USER_COLUMN_FAMILY_NAME = "ep_user";
    public static final String EP_USER_ACCESS_TOKEN_PROPERTY = ACCESS_TOKEN_PROPERTY;
    public static final String EP_USER_USER_ID_PROPERTY = USER_ID_PROPERTY;
    public static final String EP_USER_USERNAME_PROPERTY = "username";
    public static final String EP_USER_EXTERNAL_ID_PROPERTY = "ext_id";
    public static final String EP_USER_TENANT_ID_PROPERTY = "tenant_id";
    public static final String EP_USER_ENDPOINT_IDS_PROPERTY = "ep_ids";

    /**
     * CassandraEPByAccessToken constants.
     */
    public static final String EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME = "access_token_eps";
    public static final String EP_BY_ACCESS_TOKEN_ACCESS_TOKEN_PROPERTY = ACCESS_TOKEN_PROPERTY;
    public static final String EP_BY_ACCESS_TOKEN_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

    /**
     * CassandraEPByAppId constants
     */
    public static final String EP_BY_APP_ID_COLUMN_FAMILY_NAME = "app_eps";
    public static final String EP_BY_APP_ID_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String EP_BY_APP_ID_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

    /**
     * CassandraEPBySdkToken constants.
     */
    public static final String EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME = "sdk_token_eps";
    public static final String EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY = SDK_TOKEN_PROPERTY;
    public static final String EP_BY_SDK_TOKEN_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

    /**
     * CassandraEPByEndpointGroupId constants
     */
    public static final String EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME = "endpoint_group_id_eps";
    public static final String EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY = "ep_group_id";
    public static final String EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

    /**
     * CassandraTopicLastSecNum constants
     */
    public static final String TOPIC_ID_SEQ_NUMBER_COLUMN_FAMILY_NAME = "topic_seq";
    public static final String TOPIC_ID_SEQ_NUMBER_TOPIC_ID_PROPERTY = TOPIC_ID_PROPERTY;
    public static final String TOPIC_ID_SEQ_NUMBER_SEQUENCE_NUMBER_PROPERTY = "seq_num";

    /**
     * CassandraTopicLastSecNum constants
     */
    public static final String TOPIC_IDS_COLUMN_FAMILY_NAME = "topics";
    public static final String TOPIC_IDS_TOPIC_ID_PROPERTY = TOPIC_ID_PROPERTY;
    public static final String TOPIC_IDS_VER_TYPE_PROPERTY = "schema_ver";

    /**
     * CassandraEndpointUserConfiguration constants
     */
    public static final String EP_USER_CONF_COLUMN_FAMILY_NAME = "user_conf";
    public static final String EP_USER_CONF_USER_ID_PROPERTY = "user_id";
    public static final String EP_USER_CONF_APP_TOKEN_PROPERTY = "app_token";
    public static final String EP_USER_CONF_VERSION_PROPERTY = "schema_version";
    public static final String EP_USER_CONF_BODY_PROPERTY = BODY_PROPERTY;

    /**
     * Cassandra EndpointGroupStateUserType constants.
     */
    public static final String ENDPOINT_GROUP_STATE_USER_TYPE_NAME = "ep_group_state";
    public static final String ENDPOINT_GROUP_STATE_ENDPOINT_GROUP_ID_PROPERTY = "ep_group_id";
    public static final String ENDPOINT_GROUP_STATE_PROFILE_FILTER_ID_PROPERTY = "pf_filter_id";
    public static final String ENDPOINT_GROUP_STATE_CONFIGURATION_ID_PROPERTY = "cf_id";

    /**
     * Cassandra EventClassFamilyVersionStateUserType constants.
     */
    public static final String EVENT_CLASS_FAMILY_VERSION_STATE_USER_TYPE_NAME = "ecf_ver_state";
    public static final String EVENT_CLASS_FAMILY_VERSION_STATE_ECF_ID_PROPERTY = "ecf_id";
    public static final String EVENT_CLASS_FAMILY_VERSION_STATE_ECF_VERSION_PROPERTY = "ecf_ver";

    /**
     * Cassandra TopicListEntry constants.
     */
    public static final String TOPIC_LIST_ENTRY_COLUMN_FAMILY_NAME = "tl_entry";
    public static final String TOPIC_LIST_ENTRY_HASH_PROPERTY = "tl_entry_hash";
    public static final String TOPIC_LIST_ENTRY_SIMPLE_HASH_PROPERTY = "tl_entry_simple_hash";
    public static final String TOPIC_LIST_ENTRY_TOPIC_IDS_PROPERTY = "tl_entry_topic_ids";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointRegistration}
     */
    public static final String EP_REGISTRATION_COLUMN_FAMILY_NAME = "ep_registration";
    public static final String EP_REGISTRATION_ID_PROPERTY = "id";
    public static final String EP_REGISTRATION_APPLICATION_ID_PROPERTY = EP_APP_ID_PROPERTY;
    public static final String EP_REGISTRATION_ENDPOINT_ID_PROPERTY = "ep_id";
    public static final String EP_REGISTRATION_CREDENTIALS_ID_PROPERTY = "creds_id";
    public static final String EP_REGISTRATION_SERVER_PROFILE_VERSION_PROPERTY = EP_SERVER_PROFILE_VERSION_PROPERTY;
    public static final String EP_REGISTRATION_SERVER_PROFILE_BODY_PROPERTY = EP_SERVER_PROFILE_PROPERTY;

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPRegistrationByEndpointID}
     */
    public static final String EP_REGISTRATIONS_BY_ENDPOINT_ID_COLUMN_FAMILY_NAME = "creds_id_ep_registration";
    public static final String EP_REGISTRATION_BY_ENDPOINT_ID_CREDENTIALS_ID_PROPERTY = EP_REGISTRATION_CREDENTIALS_ID_PROPERTY;
    public static final String EP_REGISTRATION_BY_ENDPOINT_ID_ENDPOINT_ID_PROPERTY = EP_REGISTRATION_ENDPOINT_ID_PROPERTY;

    /**
     * Cassandra Credentials constants
     */
    public static final String CREDENTIALS_COLUMN_FAMILY_NAME = "credentials";
    public static final String CREDENTIALS_APPLICATION_ID_PROPERTY = "creds_application_id";
    public static final String CREDENTIALS_ID_PROPERTY = "creds_id";
    public static final String CREDENTIALS_BODY_PROPERTY = "creds_body";
    public static final String CREDENTIALS_STATUS_PROPERTY = "creds_status";
}
