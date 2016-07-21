---
layout: page
title: UDOO
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

[UDOO](http://www.udoo.org/) is a family of Open Source Arduino-powered Mini PCs, compatible with Android and Linux that you can exploit both as embedded systems for DIY-electronics projects and as low power consumption, fanless computers for everyday use.

This guide explains how to build applications based on the Kaa C endpoint SDKs for the UDOO board.

# Starting with UDOO board

If this is the first time you use the UDOO technology, you have to start with configuring your board. For this purpose, refer to [the UDOO official documentation](http://www.udoo.org/docs/Introduction/Introduction.html).

The UDOO platform allows building the source code directly on the board. The only thing you need to do before that is export your code onto the board. For this purpose you can use, for example, the `scp` utility:

1. Find the IP address of the UDOO board.

        ifconfig

1. Copy the source code of C SDK to the UDOO board from the host machine.

        scp /path/to/downloaded/sdk/c-sdk-archive-name.tar.gz root@<put ip address here>:c-sdk-archive-name.tar.gz

# Creating an application

Since UDOO board is powerful enough to run Linux and build applications directly on it, you can follow [the C SDK Linux guide]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) to create Kaa client application for this target.
