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

## Public host/port configuration

Kaa allows you to choose what host/port to use for internal and external communication with bootstrap and operations servers.
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

Where *transport_bind_interface* reflects internal host for bootstrap and operations servers, and *transport_public_interface* - external accordingly.
Below are given default config files, which consume above host values from file kaa-node.properties.

File operations-http-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9999,
"publicInterface":"${transport_public_interface}",
"publicPort":9999,
"maxBodySize":524288
}
```

File operations-tcp-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9997,
"publicInterface":"${transport_public_interface}",
"publicPort":9997
}
```

File bootstrap-http-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9889,
"publicInterface":"${transport_public_interface}",
"publicPort":9889,
"maxBodySize":524288
}
```

File bootstrap-tcp-transport.config :

```bash
{
"bindInterface":"${transport_bind_interface}",
"bindPort":9888,
"publicInterface":"${transport_public_interface}",
"publicPort":9888
}
```

### Typical usecases

Given a client, which should communicate with Kaa server. Client has restrictions, for example 80 and 8000 ports are open only.
In this case client unable to commuticate with Kaa server, because default configuration pointed to listen on different ports.
The solution is, to change config file operations-tcp-transport.config , set *"publicPort":80* for communication with operations server via 80 port and in file bootstrap-tcp-transport.config , set *"publicPort":8000* for communication with bootstrap server via 8000 port.
After these changes are applied client would be able to reach Kaa server properly.