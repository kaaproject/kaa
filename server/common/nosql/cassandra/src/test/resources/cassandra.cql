CREATE TABLE kaa . ep_nfs (
    ep_key_hash blob,
    nf_id varchar,
    PRIMARY KEY (ep_key_hash, nf_id)
);

CREATE TABLE kaa . notification (
    nf_id text PRIMARY KEY,
    app_id text,
    schema_id varchar,
    topic_id varchar,
    last_mod_time timestamp,
    version int,
    nf_type text,
    body blob,
    expired_at timestamp,
    seq_num int
);

CREATE TABLE kaa . ep_conf (
    cf_hash blob PRIMARY KEY,
    cf blob
);

CREATE TABLE kaa . topic_nfs (
    topic_id varchar,
    seq_num bigint,
    ntf_id uuid,
    PRIMARY KEY (topic_id, seq_num)
);

CREATE TABLE kaa . ep_user (
    user_id uuid PRIMARY KEY,
    username text,
    ext_id text,
    tenant_id varchar,
    access_token text
);

CREATE TABLE kaa . user_eps (
    user_id varchar,
    ep_ids text,
    PRIMARY KEY (user_id, ep_ids)
);

CREATE TYPE kaa . ecf_ver_state (
    ecf_id text,
    ecf_ver int
);

CREATE TYPE kaa . ep_group_state (
    ep_group_id varchar,
    pf_filter_id varchar,
    cf_id varchar
);

CREATE TABLE kaa . ep_profile (
    ep_key_hash blob PRIMARY KEY,
    ep_id varchar,
    app_id varchar,
    ep_key blob,
    ep_user_id uuid,
    access_token text,
    pf_schema_id varchar,
    cf_group_state list < frozen < ep_group_state > >,
    nf_group_state list < frozen < ep_group_state > >,
    cf_seq_num int,
    nf_seq_num int,
    pf text,
    pf_hash blob,
    pf_ver int,
    cf_hash blob,
    cf_ver int,
    nt_hash blob,
    nt_ver int,
    subscs list < text >,
    sys_nf_ver int,
    user_nf_ver int,
    log_schema_ver int,
    ecf_ver_state list < frozen < ecf_ver_state > >,
    server_hash text
);

CREATE TABLE kaa . app_eps (
    app_id varchar,
    ep_key_hash blob,
    PRIMARY KEY (app_id, ep_key_hash)
);

CREATE TABLE app_nfs (
    app_id varchar,
    nf_id varchar,
    PRIMARY KEY (app_id, nf_id)
);