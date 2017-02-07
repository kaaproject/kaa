---
layout: page
title: UDOO
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

This section explains how to build Kaa C SDK for the [UDOO](http://www.udoo.org/) board and install [Kaa applications]({{root_url}}Glossary/#kaa-application) on it.

UDOO is a family of Open Source Arduino-powered Mini PCs compatible with Android and Linux that you can exploit both as embedded systems for DIY-electronics projects, and as low power consumption fanless computers for everyday use.

## Connecting UDOO board

If this is the first time you use the UDOO technology, refer to [the UDOO official documentation](http://www.udoo.org/docs/Introduction/Introduction.html).

The UDOO platform allows building the source code directly on the board.
The only thing you need to do before that is export your code onto the board.
To do this, you can use the `scp` utility:

1. Find the IP address of the UDOO board.

   ```bash
   ifconfig
   ```

2. Copy the Kaa C SDK source code from the host machine to the UDOO board.

   ```bash
   scp /path/to/downloaded/sdk/c-sdk-archive-name.tar.gz root@<put ip address here>:c-sdk-archive-name.tar.gz
   ```

## Build Kaa application

Since UDOO runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#build-c-sdk) to build and run your application.
