---
layout: page
title: What's new
permalink: /:path/
sort_idx: 60
---

October 28, 2016: Kaa 0.10 Avocado Archipelago is released.

The Kaa 0.10 release implements a large number of new features. Here we describe the major ones, while the full list of stories and fixed bugs can be found at [Kaa 0.10 release notes].

**Key features:**
* CTL in Kaa extensions ([KAA-1142](http://jira.kaaproject.org/browse/KAA-1142)) - Common Type Library was implemented across all major Kaa modules (configuration, data collection, notifications, and events). This will allow developers to use and manage Kaa data schemas in a consistent, unified way across all types of Kaa applications. 
* Documentation on gh-pages ([KAA-784](http://jira.kaaproject.org/browse/KAA-784)) - from now on, the Kaa documentation will be hosted on gh-pages. The same principle of pull requests applies to documentation updates, so the contribution procedure is now very straightforward, exactly like with source code contributions.
* Auto-generated REST API documentation ([KAA-1108](http://jira.kaaproject.org/browse/KAA-1108)) - [REST API documentation](http://kaaproject.github.io/kaa/docs/v0.10.0/Programming-guide/Server-REST-APIs/) is now auto-generated on gh-pages.
* Docker deployment improvements ([KAA-1237](http://jira.kaaproject.org/browse/KAA-1237)) - improved capability to deploy Kaa in Docker, which is a feature contributed by a Kaa Community member. The [corresponding documentation](http://kaaproject.github.io/kaa/docs/v0.10.0/Administration-guide/System-installation/Docker-deployment/) is also available.
* Retrieving endpoint-specific and user-specific configuration ([KAA-1336](http://jira.kaaproject.org/browse/KAA-1336) and [KAA-1337](http://jira.kaaproject.org/browse/KAA-1337)) - now it is possible to retrieve configuration of a specific endpoint by an endpoint ID as well configuration of a specific user by its external ID. For this purpose, new REST API and new functions in Admin UI are now available. Also, 
* Binding on an unlimited number of public ports ([KAA-1377](http://jira.kaaproject.org/browse/KAA-1377)) - users are now allowed to configure a number of public ports on a Kaa node to listen to incoming connections, which provides a much greater degree of flexibility.
* QNX 6.5.0 support by C++ SDK ([KAA-1392](http://jira.kaaproject.org/browse/KAA-1392)) - the latest version of QNX platform is now supported by the Kaa C++ SDK.

**Other highlights:**
* Separate tenant and user management ([KAA-1256](http://jira.kaaproject.org/browse/KAA-1256), [KAA-1153](http://jira.kaaproject.org/browse/KAA-1153), [KAA-581](http://jira.kaaproject.org/browse/KAA-581)) - tenants and tenant users (tenant admins and tenant developers) are now separate entities, which allows having multiple tenant users under one tenant. The corresponding REST API was updated, including an ability for a tenant admin to remove tenant users.
* Secure KaaTCP channel for the C SDK ([KAA-635](http://jira.kaaproject.org/browse/KAA-635)) - the KaaTCP transport channel now supports encryption for the C SDK.
* Ability to delete endpoints ([KAA-581](http://jira.kaaproject.org/browse/KAA-581)) - specific endpoints can be located by their profiles and deleted.
* Switching between strategies to persist and retrieve endpoint authentication keys in Kaa SDK ([KAA-1190](http://jira.kaaproject.org/browse/KAA-1190)) - a developer can now choose between two strategies for persisting and retrieving endpoint authentication keys in Kaa SDK, that is between the pre-shared keys strategy and the runtime generated keys strategy.

**Important information:**

To upgrade your Kaa instance from version 0.9 to 0.10, refer to [0.9.x to 0.10.x guide](http://kaaproject.github.io/kaa/docs/v0.10.0/Administration-guide/Upgrading-your-instance/0.9.x-to-0.10.x/).




