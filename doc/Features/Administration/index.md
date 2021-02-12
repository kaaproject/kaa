---
layout: page
title: Administration
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


## Basic concept

### Disaster Recovery Plan

The Kaa platform has a disaster recovery plan (DRP) by implementing backup and restore procedures.

By default, the platform is deployed with the enabled backup feature.
Kaa automatically backup itself on a daily basis and upload snapshots to the AWS S3 bucket related to the particular deployment.
Using the snapshots it is possible to restore the platform to a specific state.

You can configure the backup frequency using the cron expression.
So it is possible to set up the platform to backup itself every half a day, every day, every week, etc.


## Components

The table below summarizes the list of Kaa platform components that contribute to this feature:

| Service                                  | Version                     |
| ---------------------------------------- | --------------------------- |
| [Platform Backup][Platform Backup]       | {{platform-backup_version}} |
