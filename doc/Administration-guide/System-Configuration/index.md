---
layout: page
title: System configuration
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

The Kaa cluster node is comprised of the [Control]({{root_url}}Glossary/#control-service), [Operations]({{root_url}}Glossary/#operations-service), and [Bootstrap]({{root_url}}Glossary/#bootstrap-service) services.
Kaa administrator can enable or disable each of this services individually.
In order to do this administrator need to edit corresponding properties in the /usr/lib/kaa-node/conf/kaa-node.properties file.


``` bash 
# Specifies if Control Service is enabled.
control_service_enabled=true

# Specifies if Bootstrap Service is enabled.
bootstrap_service_enabled=true

# Specifies if Operations Service is enabled.
operations_service_enabled=true
```


---
