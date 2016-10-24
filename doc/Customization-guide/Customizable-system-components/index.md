---
layout: page
title: Customizable system components
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to create your own versions of such Kaa entities as transports, user verifiers, and protocol adapters.

## Transports

Kaa platform is designed to support virtually any data transport protocols.
It provides default transport channel implementations for all its services, however, developers can create custom implementations of transport channels for any of the Kaa services and thus override the default data channels.
To learn how to implement your own custom transport see [Transport configuration]({{root_url}}Administration-guide/System-Configuration/Transport-configuration/).

## Owner verifiers

Owner verifiers are specific server components that handles user verification.
There are several default user verifier implementations like Facebook or Twitter verifiers that are available out of the box for each Kaa installation.
It is also possible to plug in custom verifier implementations.
For more information about integration with external authentication system, see [Owner verifiers]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/#owner-verifiers).

## Protocol adapters

Protocol adapters are suitable in use cases when your endpoint device uses some sophisticated communication protocol and you can not install Kaa SDK on it.
See also [Creating custom protocol adapter]({{root_url}}Getting-started/#sandbox-installation).

---
