---
layout: page
title: System configuration
permalink: /:path/
sort_idx: 30
---

* [Introduction](#introduction)

## Introduction

The Kaa cluster node is comprised of the [Control](Configuration-Control-service), [Operations](Configuration-Operations-service), and [Bootstrap](Configuration-Bootstrap-service) services. Kaa administrator can enable or disable each of this services individually. In order to do this administrator need to edit corresponding properties in ```/usr/lib/kaa-node/conf/kaa-node.properties``` file.


``` bash 
# Specifies if Control Server is enabled.
control_server_enabled=true

# Specifies if Bootstrap Server is enabled.
bootstrap_server_enabled=true

# Specifies if Operations Server is enabled.
operations_server_enabled=true
```


---
