---
layout: page
title: Multi-tenancy
permalink: /:path/
sort_idx: 80
---

{% include variables.md %}
{% include_relative links.md %}

The Kaa platform supports multi-tenancy where single Kaa instance installation serves multiple tenants. 
Each tenant has a fully isolated space and can manage own users, their permissions, applications, devices, dashboards, etc.

By default, the platform installation has one **system tenant** called **"kaa"** that has privileges to create, update, or delete other tenants.

To see how multi-tenancy works under the hood in the Kaa platform check the [Tenant Manager][Tenant Manager] documentation.

| Service                                          | Version                    |
| ------------------------------------------------ | -------------------------- |
| [Tenant Manager (TM)][Tenant Manager]            | {{tenant-manager_version}} |
