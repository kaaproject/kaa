---
layout: page
title: Server configuration
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC:
{:toc}

This section explains how to configure parameters of your [Kaa server]({{root_url}}Glossary/#kaa-server) and ensure proper communication with the [Kaa clients]({{root_url}}Glossary/#kaa-client).

## Configure Kaa node

When you complete [Kaa installation]({{root_url}}Administration-guide/System-installation/), the configuration files for each Kaa component will be extracted into the `/usr/lib/kaa-node/conf` directory.

Use the `kaa-node.properties` file to configure your Kaa server nodes.
See table below.

|Parameter name|Default value|Description|
|--------------|-------------|-----------|
|`control_service_enabled`|true|Specifies if [Control service]({{root_url}}Glossary/#control-service) is enabled.|
|`bootstrap_service_enabled`|true|Specifies if [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) is enabled.|
|`operations_service_enabled`|true|Specifies if [Operations service]({{root_url}}Glossary/#operations-service) is enabled.|
|`thrift_host`|localhost|Thrift service host address used for Thrift remote procedure calls between nodes in [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).|
|`thrift_port`|9090|Thrift service port address used for Thrift remote procedure calls between nodes in Kaa cluster.|
|`admin_port`|8080|[Administration UI]({{root_url}}Glossary/#administration-ui) port.|
|`zk_enabled`|true|Specifies if Zookeeper service is used. This property must always equal **true** and can only be changed for development or debugging purposes.|
|`zk_host_port_list`|localhost:2181|Comma-separated URL list of Zookeeper nodes, e.g: hostname1:port1,hostname2:port2.|
|`zk_max_retry_time`|3000|Maximum retry interval in milliseconds for Zookeeper service start.|
|`zk_sleep_time`|1000|Time to sleep in milliseconds between searches for work.|
|`zk_ignore_errors`|true|Specifies if runtime exception is thrown during registration of Zookeeper node.|
|`loadmgmt_min_diff`|10000|Minimum difference between the number of [endpoints]({{root_url}}Glossary/#endpoint-ep) that triggers the rebalancing process.|
|`loadmgmt_max_init_redirect_probability`|0.75|Maximum redirect probability for new sessions.|
|`loadmgmt_max_session_redirect_probability`|0.0|Maximum redirect probability for existing sessions.|
|`recalculation_period`|10|Recalculates period in seconds for the Operations service [load balancer]({{root_url}}Architecture-overview/#active-load-balancing) process.|
|`user_hash_partitions`|10|Specifies consistent-hash partitions count for each server node.|
|`max_number_neighbor_connections`|3|Specifies the maximum number of neighbor connections.|
|`ops_server_history_ttl`|3600|Time-to-live in seconds for load history of the Operations service.|
|`worker_thread_pool`|8|Thread pool size of the message handler.|
|`bootstrap_keys_private_key_location`|keys/bootstrap/private.key|Path to location of the Bootstrap service private key.|
|`bootstrap_keys_public_key_location`|keys/bootstrap/public.key|Path to location of the Bootstrap service public key.|
|`operations_keys_private_key_location`|keys/operations/private.key|Path to location of the Operations service private key.|
|`operations_keys_public_key_location`|keys/operations/public.key|Path to location of the Operations service public key.|
|`support_unencrypted_connection`|true|Enables support of unencrypted connection from Kaa client to Kaa server.|
|`transport_bind_interface`|0.0.0.0|Interface used by all transports.|
|`transport_public_interface`|localhost|Interface reported by all transports.|
|`metrics_enabled`|true|Specifies if metrics collection is enabled.|
|`logs_root_dir`|/kaa_log_uploads|Path to location of root directory for logs.|
|`date_pattern`|'.'yyyy-MM-dd-HH-mm|Date pattern for the [file system log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/).|
|`layout_pattern`|%m%n|Layout pattern for the file system log appender.|
|`load_stats_update_frequency`|10000|Frequency of load status check in milliseconds for the load balancing feature.|
|`additional_plugins_scan_package`|empty|Specifies additional package to scan for Kaa plugins configuration. See [Log appender provisioning]({{root_url}}Customization-guide/Log-appenders#log-appender-provisioning) and [Owner verifier provisioning]({{root_url}}Customization-guide/Owner-verifiers/#owner-verifier-provisioning).|

After you changed the properties in the `kaa-node.properties` file, restart the node for the changes to take effect.

```bash
$ sudo service kaa-node restart
```

## Configure public host and ports

After Kaa server is deployed in a cloud service such as AWS, Google Cloud etc., it will obtain private and public IP addresses.
The public IP address will be accessible from the Internet through particular ports.
You can specify several public ports or port ranges, e.g: `"publicPorts":"8000-8080, 9090"`.
It is useful when Kaa clients have a limited list of ports for connection.
The private IP address will be accessible within virtual network of the chosen cloud service.

You can specify IP addresses and host/ports to be used for public and private interfaces for communication with Bootstrap and Operations services.
This is useful because once public and private IP addresses are retrieved, you can report public IP/ports as embedded configuration in the SDK generated for Kaa client and specify private IP/port for Netty to bind and handle requests.

Use the following configuration files for this purpose:

* `kaa-node.properties`
* `operations-http-transport.config`
* `operations-tcp-transport.config`
* `bootstrap-http-transport.config`
* `bootstrap-tcp-transport.config`

Below is the default state of the `kaa-node.properties` file.

```bash
# Interface that will be used by all transports
transport_bind_interface=0.0.0.0

# Interface that will be reported by all transports
transport_public_interface=localhost
```

The `transport_bind_interface` parameter here specifies the private IP for Bootstrap and Operations services, and the `transport_public_interface` parameter specifies the public IP.

>**NOTE:** If `transport_public_interface` is set to **localhost**, the SDK will send request to the localhost, so only applications that are launched on the same host will work.
{:.note}

Below is the default configuration for files that receive the above host values from the `kaa-node.properties` file.

<ul>
<li markdown="1">
`operations-http-transport.config`

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9999,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9999",
"maxBodySize":524288
}
```
</li>
<li markdown="1">
`operations-tcp-transport.config`

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9997,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9997"
}
```
</li>
<li markdown="1">
`bootstrap-http-transport.config`

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9889,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9889",
"maxBodySize":524288
}
```
</li>
<li markdown="1">
`bootstrap-tcp-transport.config`

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9888,
"publicInterface":"${transport_public_interface}",
"publicPorts":"9888"
}
```
</li>
</ul>

### Typical use case

Assume you have two Kaa clients that have restrictions on communication with the Kaa server.
For example, one client can connect to port 80 and 8000 and the other one can connect to port 80 and 8001.
In this case, the clients will be unable to communicate with Kaa server, because the clients have different ports specified in the default configuration.

To resolve this:

1. In the `operations-tcp-transport.config` configuration file, specify `"publicPorts":"80"` to enable communication with the Operations service through port 80.

2. In the `file bootstrap-tcp-transport.config` configuration file, specify `"publicPorts":"8000-8001"` to enable communication with the Bootstrap service trough port 8000 and 8001.

3. Set up port forwarding to enable the clients to reach the server:
	
	* 80 to 9997
	* 8000 to 9888
	* 8001 to 9888