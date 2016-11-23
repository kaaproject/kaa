---
layout: page
title: Customizable system components
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

As a 100% open-source platform, Kaa offers unmatched opportunities for customization and extension.
This guide is designed to explain how to create your own versions of customizable Kaa components.

More customization guides are planned to be added in future releases of the documentation.
You can help the project tremendously by [contributing to this section]({{root_url}}Customization-guide/How-to-contribute/Contribution-guide/).

## Log appenders

Kaa comes with a variety of [readily available log appenders]({{root_url}}Programming-guide/Key-platform-features/Data-collection/#existing-log-appender-implementations) that handle data collected from endpoints, and pass it to an integrated system.
In case your project requires creating a custom log appender, you can create and load one using [this guide]({{root_url}}Customization-guide/Customizable-system-components/Log-appenders/).

## Owner verifiers

Owner verifiers are server components that handle verification of the owners associated with endpoints.
There are several out of the box user verifier implementations like Facebook or Twitter verifiers.
It is also possible to plug in custom owner verifiers.
For more information about the integration with an external authentication system, see [Owner verifiers]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/#owner-verifiers).

---
