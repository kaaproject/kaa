---
layout: page
title: Key platform features
permalink: /:path/
sort_idx: 30
---
{% include variables.md %}


This guide describes major features of Kaa and how to effectively use them in IoT applications. 
If you decided to go with a full-scale Kaa and not Kaa Sandbox, make sure you have properly set up and configured your Kaa instance as described in [Administration guide]({{root_url}}Administration-guide/) before getting into programming. 

| Feature | Description |
|-------|----------------|
| **[Active load balancing]({{root_url}}Architecture-overview/#active-load-balancing)** | Kaa implements a number of load balancing strategies and automatically uses them at run time to achieve more or less equal load for each node in the Kaa cluster. Also, this feature ensures that endpoints get instantly redirected to other nodes in case their current node goes down.
| **[Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library/)** | A repository of data type schemas used for all Kaa modules. As more schema types and versions are created, they are recorded in the CTL for future use.
| **[Configuration management]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/)** | Allows you to create and distribute configuration data to endpoints.
| **[Data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/)** | Allows you to collect data records (logs) from endpoints, store them in specific data processing systems, or submit to immediate stream analysis. 
| **[Endpoint provisioning and registration]({{root_url}}Programming-guide/Key-platform-features/Devices-provisioning-and-registration/)** | Enables secure authentication and registration of endpoints within a Kaa cluster.
| **[Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/)** | Allows you to aggregate endpoints into groups and then apply other Kaa features to entire endpoint groups.
| **[Endpoint ownership]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/)** | Allows you to associate owners with specific endpoints.
| **[Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/)** | Allows you to provide endpoints with specific attributes, which can be then used for filtering endpoints and aggregating them into endpoint groups. 
| **[Events]({{root_url}}Programming-guide/Key-platform-features/Events/)** | Allows you to create custom events and make endpoints send them to each other. 
| **[Notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/)** | Allows you to deliver messages and calls for action to endpoints.

---
