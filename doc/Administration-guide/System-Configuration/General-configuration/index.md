---
layout: page
title: General configuration
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

After [Kaa installation]({{root_url}}Administration-guide/System-installation/), configuration files for each Kaa component will be extracted into the
/etc/kaa-node/conf directory.

The kaa-node.properties file is responsible for Kaa server configuration.

>**NOTE:**
> After changing properties in the kaa-node.properties file, you must restart the node for changes to take effect, by executing following command:
>
```bash
$ sudo service kaa-node restart
```

The kaa-node.properties consist of the following parameters:

* *control_service_enabled*
<br> Default: _true_
<br>Specifies if [Control Service]({{root_url}}Architecture-overview/#control-service) is enabled.
* *bootstrap_service_enabled*
<br> Default: _true_
<br> Specifies if [Bootstrap Service]({{root_url}}Architecture-overview/#bootstrap-service) is enabled.
* *operations_service_enabled*
<br> Default: _true_
<br> Specifies if [Operations Service]({{root_url}}Architecture-overview/#operations-service) is enabled.
* *thrift_host*
<br> Default: _localhost_
<br> Thrift service host address. This information is used for Thrift remote procedure calls between nodes in
[Kaa cluster]({{root_url}}Administration-guide/System-installation/Cluster-setup/).
* *thrift_port*
<br> Default: _9090_
<br> Thrift service port address. This information is used for Thrift remote procedure calls between nodes in
[Kaa cluster]({{root_url}}Administration-guide/System-installation/Cluster-setup/).
* *admin_port*
<br> Default: _8080_
<br> Kaa Administration Web UI port.
* *zk_enabled*
<br> Default: _true_
<br> Specifies if need to use [Zookeeper service]({{root_url}}Architecture-overview/#zookeeper). This property has to be always _true_. It is possible
to change it for development or debug process.
* *zk_host_port_list*
<br> Default: _localhost:2181_
<br> Comma-separated url list of Zookeeper nodes: hostname1:port1,hostname2:port2.
* *zk_max_retry_time*
<br> Default: _3000_
<br> The max retry time in milliseconds that retries to start Zookeeper service.
* *zk_sleep_time*
<br> Default: _1000_
<br> Time to sleep in milliseconds between searches for work.
* *zk_ignore_errors*
<br> Default: _true_
<br> Specifies if need to throw runtime exception during registration control Zookeeper node.
* *loadmgmt_min_diff*
<br> Default: _10000_
<br> Minimum difference between amount of endpoints that need to be present in order to trigger rebalancing.
* *loadmgmt_max_init_redirect_probability*
<br> Default: _0.75_
<br> Maximum redirect probability for new sessions.
* *loadmgmt_max_session_redirect_probability*
<br> Default: _0.0_
<br> Maximum redirect probability for existing sessions.
* *recalculation_period*
<br> Default: _10_
<br> Recalculate period in seconds for Operations service load balancer process.
* *user_hash_partitions*
<br> Default: _10_
<br> Specify consistent-hash partitions count for each server node.
* *max_number_neighbor_connections*
<br> Default: _3_
<br> Specify the max number of neighbor connections.
* *ops_server_history_ttl*
<br> Default: _3600_
<br> Time to live in seconds for historical information about Operations service load.
* *worker_thread_pool*
<br> Default: _8_
<br> Message Handler thread pool executor size.
* *bootstrap_keys_private_key_location*
<br> Default: _keys/bootstrap/private.key_
<br> Path to Bootstrap service private key.
* *bootstrap_keys_public_key_location*
<br> Default: _keys/bootstrap/public.key_
<br> Path to Bootstrap service public key.
* *operations_keys_private_key_location*
<br> Default: _keys/operations/private.key_
<br> Path to Operations service private key.
* *operations_keys_public_key_location*
<br> Default: _keys/operations/public.key_
<br> Path to Operations service public key.
* *support_unencrypted_connection*
<br> Default: _true_
<br> Specify if support unencrypted connection from client to Kaa server.
* *transport_bind_interface*
<br> Default: _0.0.0.0_
<br> Interface that will be used by all transports.
* *transport_public_interface*
<br> Default: _localhost_
<br> Interface that will be reported by all transports.
* *metrics_enabled*
<br> Default: _true_
<br> Specify if metrics collections are enabled.
* *logs_root_dir*
<br> Default: _/kaa_log_uploads_
<br> Path to logs root directory.
* *date_pattern*
<br> Default: _'.'yyyy-MM-dd-HH-mm_
<br> Date pattern for the [file system log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/).
* *layout_pattern*
<br> Default: _%m%n_
<br> Layout pattern for the [file system log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/).
* *load_stats_update_frequency*
<br> Default: _10000_
<br> Frequency of load status check in milliseconds for the load balancing feature.
* *additional_plugins_scan_package*
<br> Default: _empty_
<br> Specify additional package to scan kaa plugins configuration. For details look at
[Log appender provisioning]({{root_url}}Customization-guide/Customizable-system-components/Log-appenders#log-appender-provisioning) or
[Owner verifier provisioning]({{root_url}}Customization-guide/Customizable-system-components/Owner-verifiers/#owner-verifier-provisioning).
