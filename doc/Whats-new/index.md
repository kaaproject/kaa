---
layout: page
title: What's new
permalink: /:path/
sort_idx: 60
---
{% include variables.md %}

* TOC
{:toc}

The Kaa 0.10.0 Avocado Archipelago is now available for [download](http://www.kaaproject.org/download-kaa)!

The Kaa 0.10.0 release implements a large number of new features.
Here we describe the major ones, while the full list of stories and fixed bugs can be found at [Kaa 0.10.0 release notes](https://github.com/kaaproject/kaa/releases/tag/v0.10.0).

## Key features

* CTL in Kaa extensions ([KAA-1142](http://jira.kaaproject.org/browse/KAA-1142)) - Common Type Library was implemented across all major Kaa modules (configuration, data collection, notifications, and events). 
This will allow developers to use and manage Kaa data schemas in a consistent, unified way across all types of Kaa applications. 

* Documentation on gh-pages ([KAA-784](http://jira.kaaproject.org/browse/KAA-784)) - from now on, the Kaa documentation will be hosted on gh-pages. 
The universal principle of pull requests used on github applies to documentation updates as well, so the [contribution procedure]({{root_url}}How-to-contribute/) is now very straightforward, exactly like with source code contributions. 
The documentation now also has a much better design, navigation, and versioning.

* Auto-generated REST API documentation ([KAA-1108](http://jira.kaaproject.org/browse/KAA-1108)) - [REST API documentation]({{root_url}}Programming-guide/Server-REST-APIs/) is now auto-generated on gh-pages.

* Docker deployment improvements ([KAA-1237](http://jira.kaaproject.org/browse/KAA-1237)) - improved capability to deploy Kaa in Docker, which is a feature contributed by a Kaa Community member. 
The [corresponding documentation]({{root_url}}Administration-guide/System-installation/Docker-deployment/) is also available.

* Retrieving endpoint-specific and user-specific configuration ([KAA-1336](http://jira.kaaproject.org/browse/KAA-1336) and [KAA-1337](http://jira.kaaproject.org/browse/KAA-1337)) - now it is possible to retrieve configuration of a specific endpoint by an endpoint ID as well configuration of a specific user by its external ID. 
For this purpose, new REST API and new functions in Admin UI are now available. 

* Binding on an unlimited number of public ports ([KAA-1377](http://jira.kaaproject.org/browse/KAA-1377)) - users are now allowed to configure a number of public ports on a Kaa node to listen to incoming connections, which provides a much greater degree of flexibility.

* QNX 6.5.0 support by C++ SDK ([KAA-1392](http://jira.kaaproject.org/browse/KAA-1392)) - the latest version of QNX platform is now supported by the Kaa C++ SDK.

## Other highlights

* Improved user management ([KAA-1256](http://jira.kaaproject.org/browse/KAA-1256), [KAA-1153](http://jira.kaaproject.org/browse/KAA-1153), [KAA-581](http://jira.kaaproject.org/browse/KAA-581)) - tenants and tenant users (tenant users are tenant admins and tenant developers) are now independent entities, which allows having multiple tenant users under one tenant. 
REST API for user management was significantly reworked and improved, including an ability for a tenant admin to remove tenant users.

* Secure KaaTCP channel for the C SDK ([KAA-635](http://jira.kaaproject.org/browse/KAA-635)) - the KaaTCP transport channel now supports encryption for the C SDK.

* Ability to delete endpoints ([KAA-581](http://jira.kaaproject.org/browse/KAA-581)) - specific endpoints can be located and deleted.

* Switching between strategies to persist and retrieve endpoint authentication keys in Kaa SDKs ([KAA-1190](http://jira.kaaproject.org/browse/KAA-1190)) - a developer can now choose between two strategies for persisting and retrieving endpoint authentication keys in Kaa SDKs, that is between the pre-shared keys strategy and the runtime generated keys strategy.

## Important information

To upgrade your Kaa instance from version 0.9 to 0.10, refer to [0.9.x to 0.10.x guide]({{root_url}}Administration-guide/Upgrading-your-instance/0.9.x-to-0.10.x/).

---
