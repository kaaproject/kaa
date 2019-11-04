---
layout: page
title: Miscellaneous
permalink: /:path/
sort_idx: 90
---

{% include variables.md %}
{% include_relative links.md %}

The Kaa platform ships with additional microservices that implement miscellaneous functionality.
One such microservice is the [Time Series Extension (TSX)][TSX], which allows connected endpoints to request a current timestamp from the Kaa server to approximately synchronize their internal clock (or just use for the data reporting in case of constrained devices that may not have an internal clock).

| Service                                          | Version         |
| ------------------------------------------------ | --------------- |
| [Timestamp Synchronization Extension (TSX)][TSX] | {{tsx_version}} |
