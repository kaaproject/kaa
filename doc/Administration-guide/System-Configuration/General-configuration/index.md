---
layout: page
title: General configuration
permalink: /:path/
sort_idx: 10
---

After Kaa installation on Ubuntu/Debian OS (deb packages), configuration files for each Kaa component will be extracted into the
/usr/lib/kaa-node/conf or /etc/kaa-node/conf directories.

The kaa-node.properties file is responsible for Kaa server configuration.

>**NOTE:**
> After changing properties in the kaa-node.properties file, you must restart the node for changes to take effect, by executing following command:
>
```bash
$ sudo service kaa-node restart
```

The kaa-node.properties consist of the following parameters:

#### control_service_enabled
Default: _true_

Specifies if Control Service is enabled.

#### bootstrap_service_enabled
Default: _true_

Specifies if Bootstrap Service is enabled.

#### operations_service_enabled
Default: _true_

Specifies if Operations Service is enabled.

#### thrift_host
Default: _localhost_

Thrift service host address.

#### thrift_port
Default: _9090_

Thrift service port address.

#### admin_port
Default: _8080_

Kaa Admin Web service port.

#### zk_enabled
Default: _true_

Specifies if need to use zookeeper service. This is property have to be always _true_. It is possible to change it for development or debug process.

#### zk_host_port_list
Default: _localhost:2181_

Comma-separated url list of Zookeeper nodes: hostname:port.

#### zk_max_retry_time
Default: _3000_

The max retry time in milliseconds.

#### zk_sleep_time
Default: _1000_

Time to sleep in milliseconds between searches for work.

#### zk_ignore_errors
Default: _true_

Specifies if need to throw runtime exception during registration control zookeeper node.

#### loadmgmt_min_diff
Default: _10000_

Minimum difference between amount of endpoints that need to be present in order to trigger rebalancing.

#### loadmgmt_max_init_redirect_probability
Default: _0.75_

Maximum redirect probability for new sessions.

#### loadmgmt_max_session_redirect_probability
Default: _0.0_

Maximum redirect probability for existing sessions.


#### recalculation_period
Default: _10_

Recalculate period in seconds for Operations service load process.

#### user_hash_partitions
Default: _10_

Specify hash partitions count for each server node.

#### max_number_neighbor_connections
Default: _3_

Specify the max number of neighbor connections.

#### ops_server_history_ttl
Default: _3600_

Default time to live in seconds for historical information about Operations service load.

#### worker_thread_pool
Default: _8_

Handler thread pool executor size.

#### bootstrap_keys_private_key_location
Default: _keys/bootstrap/private.key_

Path to Bootstrap service private key.

#### bootstrap_keys_public_key_location
Default: _keys/bootstrap/public.key_

Path to Bootstrap service public key.

#### operations_keys_private_key_location
Default: _keys/operations/private.key_

Path to Operations service private key.

#### operations_keys_public_key_location
Default: _keys/operations/public.key_

Path to Operations service public key.

#### support_unencrypted_connection
Default: _true_

Specify if support unencrypted connection.

#### transport_bind_interface
Default: _0.0.0.0_

Interface that will be used by all transports.

#### transport_public_interface
Default: _localhost_

Interface that will be reported by all transports.

#### metrics_enabled
Default: _true_

Metrics collect enabled.

#### logs_root_dir
Default: _/kaa_log_uploads_

Path to logs root directory.

#### date_pattern
Default: _'.'yyyy-MM-dd-HH-mm_

Date pattern for file log appender.

#### layout_pattern
Default: _%m%n_

Layout pattern for file log appender.

#### tmp_keys
Default: _/home/kaa/tmp_keys_

Path to tmp keys directory.

#### load_stats_update_frequency
Default: _10000_

Frequency of load status check in milliseconds.

#### additional_plugins_scan_package
Default: _empty_

Specify additional package to scan kaa plugins configuration.
