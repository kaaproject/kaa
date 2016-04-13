---
layout: page
title: Bootstrap service
permalink: /:path/
nav: /:path/Administration-guide/System-Configuration/Configuration-Bootstrap-service
sort_idx: 20
---

* [Introduction](#introduction)
* [Configurations](#configurations)

## Introduction

A Kaa Bootstrap service is responsible for directing endpoints to Operations services. On their part, Kaa endpoints have a built-in list of Bootstrap services set up in the given Kaa deployment. The endpoints use this list to query the Bootstrap services and retrieve a list of currently available Operations services from them, as well as security credentials. Bootstrap services maintain their lists of available Operations service nodes by coordinating with the ZooKeeper servers.

## Configurations

A Control service manages data stored in a database (independently for each tenant) and notifies every Operations server on most data updates via a Thrift-based protocol. The value of ```transport_public_interface``` property  in ```/usr/lib/kaa-node/conf/kaa-node.properties``` file will be built-in as a host ip/name of Bootstrap server.

```bash
transport_public_interface=<ip_of_current_machine>
```

---
