CREATE TABLE IF NOT EXISTS kaa.ep_nfs (
    ep_key_hash blob,
    seq_num int,
    nf_id text,
    nf_type text,
    app_id text,
    schema_id text,
    version int,
    last_mod_time timestamp,
    body blob,
    expired_at timestamp,
    PRIMARY KEY (ep_key_hash, seq_num)
) WITH CLUSTERING ORDER BY (seq_num DESC);

CREATE TABLE IF NOT EXISTS kaa.notification (
    topic_id text,
    nf_type text,
    nf_id text,
    app_id text,
    schema_id text,
    last_mod_time timestamp,
    version int,
    body blob,
    expired_at timestamp,
    seq_num int,
    PRIMARY KEY((topic_id, nf_type), version, seq_num)
) WITH CLUSTERING ORDER BY (version DESC, seq_num DESC);

CREATE TABLE IF NOT EXISTS kaa.topic_nf (
    nf_id text PRIMARY KEY,
    app_id text,
    schema_id text,
    topic_id text,
    last_mod_time timestamp,
    version int,
    nf_type text,
    body blob,
    expired_at timestamp,
    seq_num int
);

CREATE TABLE IF NOT EXISTS kaa.ep_conf (
    cf_hash blob PRIMARY KEY,
    cf blob,
    cf_id text
);

CREATE TABLE IF NOT EXISTS kaa.topic_nfs (
    topic_id text,
    seq_num bigint,
    ntf_id uuid,
    PRIMARY KEY (topic_id, seq_num)
);

CREATE TABLE IF NOT EXISTS kaa.ep_user (
    user_id text,
    username text,
    ext_id text,
    tenant_id text,
    access_token text,
    ep_ids list <text>,
    PRIMARY KEY (ext_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS kaa.user_eps (
    user_id text,
    ep_ids text,
    PRIMARY KEY (user_id, ep_ids)
);

CREATE TYPE IF NOT EXISTS kaa.ecf_ver_state (
    ecf_id text,
    ecf_ver int
);

CREATE TYPE IF NOT EXISTS kaa.ep_group_state (
    ep_group_id text,
    pf_filter_id text,
    cf_id text
);

CREATE TABLE IF NOT EXISTS kaa.ep_profile (
    ep_key_hash blob PRIMARY KEY,
    ep_id text,
    app_id text,
    ep_key blob,
    user_id uuid,
    access_token text,
    pf_schema_id text,
    cf_group_state list < frozen < ep_group_state > >,
    nf_group_state list < frozen < ep_group_state > >,
    cf_seq_num int,
    nf_seq_num int,
    pf text,
    pf_hash blob,
    pf_ver int,
    cf_hash blob,
    cf_ver int,
    nf_hash blob,
    nf_ver int,
    subscs list <text>,
    sys_nf_ver int,
    user_nf_ver int,
    log_schema_ver int,
    ecf_ver_state list < frozen < ecf_ver_state > >,
    server_hash text
);

CREATE TABLE IF NOT EXISTS kaa.app_eps (
    app_id text,
    ep_key_hash blob,
    PRIMARY KEY (app_id, ep_key_hash)
);

CREATE TABLE IF NOT EXISTS kaa.app_nfs (
    app_id text,
    nf_id text,
    PRIMARY KEY (app_id, nf_id)
);

CREATE TABLE IF NOT EXISTS kaa.app_key_hash (
    app_id text,
    ep_key_hash blob,
    PRIMARY KEY (app_id, ep_key_hash)
);

CREATE TABLE IF NOT EXISTS kaa.access_token_eps (
    access_token text,
    ep_key_hash blob,
    PRIMARY KEY (access_token, ep_key_hash)
);
