---
layout: page
title: General configuration
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC:
{:toc}

# Server configuration

## Kaa node configuration

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
<br> Recalculate period in seconds for Operations service [load balancer]({{root_url}}Administration-guide/System-components-overview/#load-balancing-lb) process.
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
<br> Date pattern for the [file log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/).
* *layout_pattern*
<br> Default: _%m%n_
<br> Layout pattern for the [file log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/).
* *load_stats_update_frequency*
<br> Default: _10000_
<br> Frequency of load status check in milliseconds for the [load balancing]({{root_url}}Administration-guide/System-components-overview/#load-balancing-lb) feature.
* *additional_plugins_scan_package*
<br> Default: _empty_
<br> Specify additional package to scan kaa plugins configuration. For details look at
[Log appender provisioning]({{root_url}}Customization-guide/Customizable-system-components/Log-appenders#log-appender-provisioning) or
[Owner verifier provisioning]({{root_url}}Customization-guide/Customizable-system-components/Owner-verifiers/#owner-verifier-provisioning).
* *default_ttl*
<br> Default: 7 days
<br> Defines the live time of the notification

## Public host/ports configuration

Kaa server is going to be deployed in cloud service such as AWS, Google Cloud etc.
After deployment it will have private and public IP address.
Public IP will be available from the internet on particular ports.
Kaa allows to specify several public ports or port ranges. E.g: *"publicPorts":"8000-8080, 9090"*.
It is useful in case when clients have a limited list of ports to which they can connect.
Private IP will be available within virtual network in cloud service.

Kaa allows you to specify what IP and host/ports are used for public and private interface/port for communication with Bootstrap and Operations servers.
This is useful because once public and private IPs retrieved you have ability to report public IP/ports to the client in form of embedded configuration in generated SDK and specify private IP/port where netty binds and serves requests.

There are few configuration files for this purpose:

* kaa-node.properties
* operations-http-transport.config
* operations-tcp-transport.config
* bootstrap-http-transport.config
* bootstrap-tcp-transport.config

Below is default state of file kaa-node.properties:

```bash
# Interface that will be used by all transports
transport_bind_interface=0.0.0.0

# Interface that will be reported by all transports
transport_public_interface=localhost
```

Where *transport_bind_interface* reflects private IP for Bootstrap and Operations servers, and *transport_public_interface* - public accordingly.

>**NOTE:**
> If *transport_public_interface* is set to *localhost* - SDK will send request to localhost, so only applications that are launched on the same host will work.
>

Below are given default config files, which consume above host values from file kaa-node.properties.

File operations-http-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9999,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9999",
"maxBodySize":524288
}
```

File operations-tcp-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9997,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9997"
}
```

File bootstrap-http-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9889,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9889",
"maxBodySize":524288
}
```

File bootstrap-tcp-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9888,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9888"
}
```

### Typical usecase

Given two clients, which should communicate with Kaa server.
They have restrictions, for example the first client can connect to 80 and 8000 port and the second client can connect to 80 and 8001 port.
In this case clients unable to communicate with Kaa server, because default configuration pointed to listen on different ports.
The solution is, to change config file operations-tcp-transport.config, set *"publicPorts":"80"* for communication with operations server via 80 port and in file bootstrap-tcp-transport.config, set *"publicPorts":"8000-8001"* for communication with bootstrap server via 8000 and 8001 port.
If you set up ports forwarding, in this case 80 to 9997, 8000 to 9888 and 8001 to 9888, then clients would be able to reach Kaa server properly.

