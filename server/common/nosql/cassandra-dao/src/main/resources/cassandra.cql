DROP KEYSPACE IF EXISTS kaa;
CREATE KEYSPACE kaa WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TYPE IF NOT EXISTS kaa.ecf_ver_state (
    ecf_id text,
    ecf_ver int
);

CREATE TYPE IF NOT EXISTS kaa.ep_group_state (
    ep_group_id text,
    pf_filter_id text,
    cf_id text
);

CREATE TABLE IF NOT EXISTS kaa.ep_nfs (
    ep_key_hash blob,
    seq_num int,
    nf_id text,
    nf_type text,
    app_id text,
    schema_id text,
    ep_nf_version int,
    last_mod_time timestamp,
    body blob,
    expired_at timestamp,
    topic_id text,
    opt_lock bigint,
    PRIMARY KEY (ep_key_hash, last_mod_time)
) WITH CLUSTERING ORDER BY (last_mod_time DESC);

CREATE TABLE IF NOT EXISTS kaa.notification (
    topic_id text,
    nf_type text,
    nf_id text,
    app_id text,
    schema_id text,
    last_mod_time timestamp,
    nf_version int,
    body blob,
    expired_at timestamp,
    seq_num int,
    opt_lock bigint,
    PRIMARY KEY((topic_id, nf_type), nf_version, seq_num)
) WITH CLUSTERING ORDER BY (nf_version DESC, seq_num DESC);

CREATE TABLE IF NOT EXISTS kaa.ep_conf (
    cf_hash blob PRIMARY KEY,
    cf blob,
    cf_id text,
    opt_lock bigint
);

CREATE TABLE IF NOT EXISTS kaa.ep_user (
    user_id text,
    username text,
    ext_id text,
    tenant_id text,
    access_token text,
    ep_ids list <text>,
    opt_lock bigint,
    PRIMARY KEY (ext_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS kaa.ep_profile (
    ep_key_hash blob PRIMARY KEY,
    ep_id text,
    app_id text,
    ep_key blob,
    user_id text,
    access_token text,
    group_state list < frozen < ep_group_state > >,
    seq_num int,
    pf text,
    pf_hash blob,
    pf_ver int,
    srv_pf_ver int,
    ucf_hash blob,
    cf_hash blob,
    cf_ver int,
    topic_hash blob,
    simple_topic_hash int,
    nf_ver int,
    subscs list <text>,
    sys_nf_ver int,
    user_nf_ver int,
    log_schema_ver int,
    ecf_ver_state list < frozen < ecf_ver_state > >,
    server_hash text,
    sdk_token text,
    srv_pf text,
    opt_lock bigint
);

CREATE TABLE IF NOT EXISTS kaa.app_eps (
    app_id text,
    ep_key_hash blob,
    PRIMARY KEY (app_id, ep_key_hash)
);

CREATE TABLE IF NOT EXISTS kaa.access_token_eps (
    access_token text PRIMARY KEY ,
    ep_key_hash blob
);

CREATE TABLE IF NOT EXISTS kaa.endpoint_group_id_eps (
    ep_group_id text ,
    ep_key_hash blob ,
    PRIMARY KEY (ep_group_id, ep_key_hash)
);

CREATE TABLE IF NOT EXISTS kaa.sdk_token_eps (
    sdk_token text PRIMARY KEY,
    ep_key_hash blob,
    opt_lock bigint
);

CREATE TABLE IF NOT EXISTS kaa.user_conf (
    user_id text,
    app_token text,
    schema_version int,
    body text,
    opt_lock bigint,
    PRIMARY KEY (user_id, app_token, schema_version)
);

CREATE TABLE IF NOT EXISTS kaa.tl_entry (
    tl_entry_hash blob PRIMARY KEY,
    tl_entry_simple_hash int,
    tl_entry_topic_ids list<text>,
    opt_lock bigint
);

CREATE TABLE IF NOT EXISTS kaa.ep_registration (
    id text,
    app_id text,
    ep_id text,
    creds_id text PRIMARY KEY,
    srv_pf_ver int,
    srv_pf text
);

CREATE TABLE IF NOT EXISTS kaa.creds_id_ep_registration (
	ep_id text PRIMARY KEY,
    creds_id text
);

CREATE TABLE IF NOT EXISTS kaa.credentials (
    creds_application_id text,
    creds_id text,
    creds_body blob,
    creds_status text,
    PRIMARY KEY (creds_application_id, creds_id)
);