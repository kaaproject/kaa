---
layout: page
title: System installation
permalink: /:path/
nav: /:path/Administration-guide/System-installation/
sort_idx: 20
---
* [Introduction](#introduction)
* [System requirements](#system-requirements)
* [Supported OS](#supported-os)
* [Third party components](#third-party-components)
* [Installing Kaa](#installing-kaa)
  * [Single node installation](#single-node-installation)
  * [Node claster setup](#node-claster-setup)
  * [AWS deployment](#aws-deployment)
  * [Planning your deployment](#planning-your-deployment)
* [Throubleshooting](#throubleshooting)

## Introduction

This guide overview over kaa platform instalation.

## System requirements

To use Kaa, your system must meet the following minimum system requirements.

   * 64-bit OS
   * 4 Gb RAM

## Supported OS

Kaa supports the following operating system families and provides installation packages for each of them.

   * Ubuntu and Debian systems
   * Red Hat/CentOS/Oracle 5 or Red Hat 6 systems

Please note that the instructions from this guide were tested on Ubuntu 14.04 and Centos 6.7. Instructions for other OS may have minor differences.

## Third party components

Kaa requires the following third party components to be installed and configured.

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Kaa has been tested on JDK 8
* [PostgreSQL 9.4](http://www.postgresql.org/download/). Kaa has been tested on the latest production release of PostgreSQL.
* [MariaDB 5.5](https://mariadb.org/download/). Kaa has been tested on the latest production release of MariaDB.
* [Zookeeper 3.4.5](http://zookeeper.apache.org/doc/r3.4.5/). Kaa requires ZooKeeper for coordination of server components.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 2.2.5](http://cassandra.apache.org/download/) as a NoSQL database. The installation steps for third-party components are provided in the following section.

## Installing Kaa

Kaa platform provides you several options for kaa-node server instalation, for more detail on how to install your kaa server please refer to next sections.

### Single node installation

If you need to install and configure Kaa components on a single Linux node refer to [Single node installation](Single-node-installation) documentation page.

### Node claster setup

To lern how to create kaa-node claster refer to [Node claster setup](Cluster-setup) documentation page.

### AWS deployment.

You can either install Kaa server as described on this page or run it on [Amazon EC2](Planning-your-deployment/#aws-deployment-preparation).

### Planning your deployment

For more information about environment setup please refet to [Planning your deployment](Planning-your-deployment/) documentation page.

## Throubleshooting

Common issues covered in this [guide](../Troubleshooting).