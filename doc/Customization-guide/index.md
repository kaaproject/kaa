---
layout: page
title: Customization guide
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

This guide describes the basics of customizing and extending the [Kaa platform]({{root_url}}Glossary/#kaa-platform).
It is intended for users, already intimately familiar with the Kaa platform, it's [features]({{root_url}}Programming-guide/Key-platform-features/) and [architecture]({{root_url}}Architecture-overview/), and who have extensive programming experience.

## Sections overview

| Section | Description |
|---------|-------------|
| **[Log appenders]({{root_url}}Customization-guide/Log-appenders/)** | Kaa comes with a variety of [readily available log appenders]({{root_url}}Programming-guide/Key-platform-features/Data-collection/#existing-log-appender-implementations) that handle data collected from endpoints, and pass it to an integrated system. You can create custom log appenders for you projects. |
| **[Owner verifiers]({{root_url}}Customization-guide/Owner-verifiers/)** | [Owner verifiers]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/#owner-verifiers) are server components that handle verification of the owners associated with endpoints. There are several out-of-the-box user verifier implementations, plus you can plug in custom owner verifiers. |
| **[Nix package manager]({{root_url}}Customization-guide/Nix-guide/)** | Learn how to use Nix package manager to improve development environment for Kaa [C]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/)/[C++]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/) SDKs, and to manage third-party dependencies. |

---
