# ![KAA](https://github.com/kaaproject/kaa/blob/master/gh-pages-stub/public/kaa-avatar.png?raw=true) Open-source IoT platform

[![Build Status](https://jenkins.kaaproject.org:52001/buildStatus/icon?job=kaa_develop_daily)](https://jenkins.kaaproject.org:52001/job/kaa_develop_daily)

# About

Kaa is a production-ready, multi-purpose middleware platform for building complete end-to-end IoT solutions, connected applications, and smart products. The Kaa platform provides an open, feature-rich toolkit for the IoT product development and thus dramatically reduces associated cost, risks, and time-to-market. For a quick start, Kaa offers a set of out-of-the-box enterprise-grade IoT features that can be easily plugged in and used to implement a large majority of the IoT use cases.

# Architecture

The Kaa IoT platform consists of the Kaa server, Kaa extensions and endpoint SDKs.

* The Kaa server implements the back-end part of the platform, performs tenants, applications, users, and devices management, exposes integration interfaces, and offers administrative capabilities.
* Kaa extensions are independent software modules that improve the platform functionality. (In this version of the documentation you will notice some extensions actually managed within the core platform code base. Those are planned to be fully decoupled in future Kaa releases.)
* Endpoint SDK is a library which provides client-side APIs for the various Kaa platform features and handles communication, data marshalling, persistence, etc. Kaa SDKs are designed to facilitate the creation of client applications to be run on various connected devices - however, client applications that do not use Kaa endpoint SDK are also possible. Several implementations of the Endpoint SDK are available in different programming languages.
Kaa cluster consists of Kaa server nodes that use Apache ZooKeeper for services coordination. Kaa cluster also requires NoSQL and SQL database instances to store endpoint data and metadata, accordingly.

[Read more...](http://kaaproject.github.io/kaa/docs/v0.10.0/Architecture-overview/)

# Getting started

[Kaa Sandbox](http://www.kaaproject.org/download-kaa/) is a pre-configured virtual environment specifically designed for speeding up the setup of your private Kaa platform instance for educational, development, and proof-of-concept purposes. The Sandbox also includes a selection of demo applications that illustrate various aspects of the platform functionality.

You are also welcome to follow the below video tutorial that will walk you through the Kaa Sandbox set up.

[![Getting started](http://img.youtube.com/vi/ynbxcRdgXFU/0.jpg)](https://youtu.be/ynbxcRdgXFU)

[Read more...](http://kaaproject.github.io/kaa/docs/v0.10.0/Getting-started/)

# Installation guide

Kaa platform provides you several options for kaa-node server installation, for more detail on how to install your Kaa server please refer to [next guide](http://kaaproject.github.io/kaa/docs/v0.10.0/Administration-guide/System-installation/).

# Need help ?

First of all, don't panic! 
There is a nice [F.A.Q](http://docs.kaaproject.org/display/KAA/Frequently+asked+questions) that covers a lot of questions. And check out the [Kaa forum](http://www.kaaproject.org/forum/) - there is a good chance somebody already have dealt with your issue. Still stuck ? You can submit a new question and help everybody else.

# Documentation

Kaa documentation is a part of Kaa source code and is located in the [`doc/`](https://github.com/kaaproject/kaa/tree/master/doc) folder. You can find the web version [here](http://kaaproject.github.io/kaa).

# How to contribute

We welcome you to join our rapidly growing community!

As an open-source project, we thrive from contributions by people like you to create the best possible platform for developing IoT solutions. We would love to see you mastering Kaa’s source code, however, writing code is not the only way to contribute. There are many other options, such as providing ideas, suggestions and comments in Kaa forum discussions, testing features and new releases, and reviewing and improving the documentation.

[Read more...](http://kaaproject.github.io/kaa/docs/v0.10.0/Customization-guide/How-to-contribute/)

# Report issue

You can report your findings by creating a bug in [Jira®](http://jira.kaaproject.org/browse/KAA/). Please provide as much detail as possible so that any identified issues can be effectively resolved.

# License

Kaa is distributed under the terms of the Apache License (Version 2.0).

See [`LICENSE`](https://github.com/kaaproject/kaa/blob/master/LICENSE) and [`copyright.txt`](https://github.com/kaaproject/kaa/blob/master/copyright.txt) for details.