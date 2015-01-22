package org.kaaproject.kaa.server.common.dao.cassandra.model;

public class CassandraModelConstants {

    /**
     * Generic constants.
     */
    public static final String APPLICATION_ID_PROPERTY = "app_id";
    public static final String CONFIGURATION_HASH_PROPERTY = "cf_hash";
    public static final String ACCESS_TOKEN_PROPERTY  = "access_token";
    public static final String NOTIFICATION_ID_PROPERTY = "nf_id";
    public static final String ENDPOINT_KEY_HASH_PROPERTY = "ep_key_hash";
    public static final String USER_ID_PROPERTY = "user_id";
    public static final String NOTIFICATION_TYPE_PROPERTY = "nf_type";
    public static final String SEQ_NUM_PROPERTY = "seq_num";
    public static final String BODY_PROPERTY = "body";
    public static final String EXPIRED_AT_PROPERTY = "expired_at";
    public static final String VERSION_PROPERTY = "version";
    public static final String LAST_MOD_TIME_PROPERTY = "last_mod_time";
    public static final String SCHEMA_ID_PROPERTY = "schema_id";

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
    public static final String ET_NF_VERSION_PROPERTY = VERSION_PROPERTY;
    public static final String ET_NF_LAST_MOD_TIME_PROPERTY = LAST_MOD_TIME_PROPERTY;
    public static final String ET_NF_SCHEMA_ID_PROPERTY = SCHEMA_ID_PROPERTY;
    public static final String ET_NF_TOPIC_ID_PROPERTY = "topic_id";

    /**
     * Cassandra notification constants.
     */
    public static final String NF_COLUMN_FAMILY_NAME = "notification";
    public static final String NF_TOPIC_ID_PROPERTY = "topic_id";
    public static final String NF_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String NF_NOTIFICATION_ID_PROPERTY = NOTIFICATION_ID_PROPERTY;
    public static final String NF_SCHEMA_ID_PROPERTY = SCHEMA_ID_PROPERTY;
    public static final String NF_VERSION_PROPERTY = VERSION_PROPERTY;
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
    public static final String EP_ACCESS_TOKEN_PROPERTY = ACCESS_TOKEN_PROPERTY;
    public static final String EP_ENDPOINT_ID_PROPERTY = "ep_id";
    public static final String EP_EP_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;
    public static final String EP_EP_KEY_PROPERTY = "ep_key";
    public static final String EP_USER_ID_PROPERTY = USER_ID_PROPERTY;
    public static final String EP_PROFILE_SCHEMA_ID_PROPERTY = "pf_schema_id";
    public static final String EP_CONFIG_GROUP_STATE_PROPERTY = "cf_group_state";
    public static final String EP_NOTIFICATION_GROUP_STATE_PROPERTY = "nf_group_state";
    public static final String EP_CONFIGURATION_SEQUENCE_NUMBER_PROPERTY = "cf_seq_num";
    public static final String EP_NOTIFICATION_SEQUENCE_NUMBER_PROPERTY = "nf_seq_num";
    public static final String EP_PROFILE_PROPERTY = "pf";
    public static final String EP_PROFILE_HASH_PROPERTY = "pf_hash";
    public static final String EP_PROFILE_VERSION_PROPERTY = "pf_ver";
    public static final String EP_CONFIGURATION_VERSION_PROPERTY = "cf_ver";
    public static final String EP_NOTIFICATION_VERSION_PROPERTY = "nf_ver";
    public static final String EP_NOTIFICATION_HASH_PROPERTY = "nf_hash";
    public static final String EP_SUBSCRIPTIONS_PROPERTY = "subscs";
    public static final String EP_SYSTEM_NOTIFICATION_VERSION_PROPERTY = "sys_nf_ver";
    public static final String EP_USER_NOTIFICATION_VERSION_PROPERTY = "user_nf_ver";
    public static final String EP_LOG_SCHEMA_VERSION_PROPERTY = "log_schema_ver";
    public static final String EP_ECF_VERSION_STATE_PROPERTY = "ecf_ver_state";
    public static final String EP_SERVER_HASH_PROPERTY = "server_hash";

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
     * Cassandra notifications by application constants.
     */
    public static final String NF_BY_APP_COLUMN_FAMILY_NAME = "app_nfs";
    public static final String NF_BY_APP_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String NF_BY_APP_NOTIFICATION_ID_PROPERTY = NOTIFICATION_ID_PROPERTY;


    /**
     * Cassandra CassandraUnicastNfsByAppId constants.
     */
    public static final String EP_KEY_HASH_BY_APP_COLUMN_FAMILY_NAME = "app_key_hash";
    public static final String EP_KEY_HASH_BY_APP_APPLICATION_ID_PROPERTY = APPLICATION_ID_PROPERTY;
    public static final String EP_KEY_HASH_BY_APP_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

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
     * CassandraEPByUserId constants
     */
    public static final String EP_BY_USER_ID_COLUMN_FAMILY_NAME = "user_eps";
    public static final String EP_BY_USER_ID_USER_ID_PROPERTY = USER_ID_PROPERTY;
    public static final String EP_BY_USER_ID_ENDPOINT_KEY_HASH_PROPERTY = ENDPOINT_KEY_HASH_PROPERTY;

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
}
