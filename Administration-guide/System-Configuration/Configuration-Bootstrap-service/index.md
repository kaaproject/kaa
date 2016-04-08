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

The value of built-in host ip/name of Bootstrap server depends on ```transport_public_interface``` in ```/usr/lib/kaa-node/conf/kaa-node.properties``` file.

```bash
transport_public_interface=<ip_of_current_machine>
```

---
