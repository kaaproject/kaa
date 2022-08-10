---
layout: page
title: Automation
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


## Prerequisites

- You understand the Kaa platform [microservice-based architecture][architecture overview].


## Basic concept

Kaa automation services power the Kaa platform to run various automation types on in-platform events.
Examples of such automation could be sending an email or mobile push notification, ticket creation in [Redmine][redmine], sending a command to a device, and so on once a specific event was fired.  


## Components

The table below summarizes the list of Kaa platform components that contribute to this feature:

| Service                                               | Version          |
|-------------------------------------------------------|------------------|
| [Action Automation (AA)][AA]                          | {{aa_version}}   |
| [Action Automation Connector (AAC)][AAC]              | {{aac_version}}  |
