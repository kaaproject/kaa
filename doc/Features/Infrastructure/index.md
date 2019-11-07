---
layout: page
title: Infrastructure
permalink: /:path/
sort_idx: 80
---

{% include variables.md %}
{% include_relative links.md %}

Kaa is a cloud-native IoT platform that leverages multiple 3-rd party infrastructure components to enable and facilitate the platform operation.
This includes [NATS message broker][nats], [InfluxDB][influxdb] and [MongoDB][mongo] databases, [Kubernetes][k8s] container orchestration system, [Helm][helm], [NGINX][nginx], [Prometheus][prometheus], [Fluentd][fluentd], [Grafana][grafana], and more.

In addition to these amazing 3-rd party components, Kaa includes its own infrastructure components which simplify the operation of Kaa-based clusters and provide Kaa-specific administration functionality.

| Service          | Version            |
| ---------------- | ------------------ |
| [Tekton][TEKTON] | {{tekton_version}} |
<!-- TODO: add installer once the doc is ready -->
<!-- | [Kaa installer][TODO] | {{installer_version}} | -->
