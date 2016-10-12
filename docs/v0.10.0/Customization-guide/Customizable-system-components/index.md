---
layout: page
title: Customizable system components
permalink: /:path/
sort_idx: 10
---

* [Introduction](#introduction)
* [Networking](#networking)
* [User verifiers](#user-verifiers)
* [Protocol adapters](#protocol-adapters)

## Introduction

This guide explains how to create your own versions of such Kaa entities as transports, user verifiers, and protocol adapters.

Kaa platform provides next customization capabilities:

* [Transports](#transports)
* [User verifiers](#user-verifiers)
* [Protocol adapters](#protocol-adapters)

## Transports

Kaa platform is designed to support virtually any data transport protocols. It provides default transport channel implementations for all its services, however, developers can create custom implementations of transport channels for any of the Kaa services and thus override the default data channels. To learn how to implement your own custom transport follow [Creating custom transport](Built-in-networking-stack#creating-custom-transport) guide.

## User verifiers

User verifiers are specific server components that handles user verification. There are several default user verifier implementations like Facebook or Twitter verifiers that are available out of the box for each Kaa installation. It is also possible to plug in custom verifier implementations. For more information about integration with external authentication system refer to [Custom user verifier](Owner-verifiers) creation guide.

## Protocol adapters

Protocol adapters are suitable in use cases when your endpoint device uses some sophisticated communication protocol and you can not install Kaa SDK on it. To learn how to implement custom Protocol adapter refer to [Creating custom protocol adapter](Protocol-adapters) guide.

---
