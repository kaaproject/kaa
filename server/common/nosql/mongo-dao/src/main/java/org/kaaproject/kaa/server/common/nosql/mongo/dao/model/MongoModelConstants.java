package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

public class MongoModelConstants {

    /**
     * Generic constants.
     */
    public static final String VERSION = "version";
    public static final String BODY = "body";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ENDPOINT_KEY_HASH = "endpoint_key_hash";
    public static final String APPLICATION_ID = "application_id";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.EndpointGroupState} constants.
     */
    public static final String ENDPOINT_GROUP_ID = "endpoint_group_id";
    public static final String PROFILE_FILTER_ID = "profile_filter_id";
    public static final String CONFIGURATION_ID = "configuration_id";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.EventClassFamilyVersionState} constants
     */
    public static final String ECF_ID = "ecf_id";
    public static final String EVENT_CLASS_FAMILY_VERSION = VERSION;

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointConfiguration} constants
     */
    public static final String ENDPOINT_CONFIGURATION = "endpoint_configuration";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointNotification} constants
     */
    public static final String ENDPOINT_NOTIFICATION = "endpoint_notification";
    public static final String EP_NF_ENDPOINT_KEY_HASH = ENDPOINT_KEY_HASH;

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointNotification} constants
     */
    public static final String NOTIFICATION = "notification";
    public static final String NF_APPLICATION_ID = APPLICATION_ID;
    public static final String NF_SCHEMA_ID = "notification_schema_id";
    public static final String NF_TOPIC_ID = "topic_id";
    public static final String NF_LAST_MODIFY_TIME = "last_modify_time";
    public static final String NF_TYPE = "notification_type";
    public static final String NF_EXPIRED_AT = "expired_at";
    public static final String NF_SEQ_NUM = "seq_num";
    public static final String NF_VERSION = VERSION;
    public static final String NF_BODY = BODY;

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUser} constants
     */
    public static final String ENDPOINT_USER = "endpoint_user";
    public static final String EP_USER_USERNAME = "username";
    public static final String EP_USER_EXTERNAL_ID = "external_id";
    public static final String EP_USER_TENANT_ID = "tenant_id";
    public static final String EP_USER_ACCESS_TOKEN = ACCESS_TOKEN;
    public static final String EP_USER_ENDPOINT_IDS = "endpoint_ids";

    /**
     * {@link org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile} constants
     */
    public static final String ENDPOINT_PROFILE = "endpoint_profile";
    public static final String EP_APPLICATION_ID = APPLICATION_ID;
    public static final String EP_ENDPOINT_KEY = "endpoint_key";
    public static final String EP_ENDPOINT_KEY_HASH = ENDPOINT_KEY_HASH;
    public static final String EP_USER_ID = "endpoint_user_id";
    public static final String EP_ACCESS_TOKEN = ACCESS_TOKEN;
    public static final String EP_PROFILE_SCHEMA_ID = "profile_schema_id";
    public static final String EP_CF_GROUP_STATE = "cf_group_state";
    public static final String EP_NF_GROUP_STATE = "nf_group_state";
    public static final String EP_CF_SEQ_NUM = "cf_seq_num";
    public static final String EP_NF_SEQ_NUM = "nf_seq_num";
    public static final String EP_CHANGED_FLAG = "changed_flag";
    public static final String EP_PROFILE_HASH = "profile_hash";
    public static final String EP_PROFILE_VERSION = "profile_version";
    public static final String EP_CONFIGURATION_HASH = "configuration_hash";
    public static final String EP_CONFIGURATION_VERSION = "configuration_version";
    public static final String EP_NOTIFICATION_VERSION = VERSION;
    public static final String EP_NT_HASH = "nt_hash";
    public static final String EP_SYSTEM_NF_VERSION = "system_nf_version";
    public static final String EP_USER_NF_VERSION = "user_nf_version";
    public static final String EP_LOG_SCHEMA_VERSION = "log_schema_version";
    public static final String EP_ECF_VERSION_STATE = "ecf_version_state";
    public static final String EP_SERVER_HASH = "server_hash";

}
