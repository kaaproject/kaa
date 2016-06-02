---
layout: page
title: Operations service
permalink: /:path/
nav: /:path/Administration-guide/System-Configuration/Configuration-Operations-service
sort_idx: 30
---

* [Introduction](#introduction)

## Introduction

A Kaa Operations service is a “worker” service that is responsible for concurrently handling multiple requests from multiple clients. Most common Operations service tasks include endpoint registration, processing endpoint profile updates, configuration updates distribution, and notifications delivery.
Multiple nodes with Operations service enabled may be set up in a Kaa cluster for the purpose of horizontal scaling. In this case, all the Operations service will function concurrently. In case an Operations service outage happens, the corresponding endpoints switch to the other available Operations services automatically. A Kaa cluster provides instruments for the workload re-balancing at run time, thus effectively routing endpoints to the less loaded nodes in the cluster.

---
