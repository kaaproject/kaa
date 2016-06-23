---
layout: page
title: Compatibility and dependencies
permalink: /:path/
sort_idx: 30
---

*   [Kaa server, Apache 2.0 license](#kaa-server-apache-20-license)
*   [Kaa server, non-Apache 2.0 license](#kaa-server-non-apache-20-license)
*   [Kaa server, testing](#kaa-server-testing)
*   [Kaa endpoint](#kaa-endpoint)

The following third-party software components are used in Kaa.

# Kaa server, Apache 2.0 license

| Component                                 | Version         | Licence            | Language |
|-------------------------------------------|-----------------|--------------------|----------|
| Netty                                     | 4.0.34.Final    | Apache License 2.0 | Java     |
| Akka                                      | 2.4.1           | Apache License 2.0 | Scala    |
| Google GWT                                | 2.7.0           | Apache License 2.0 | Java     |
| Spring Framework                          | 4.0.2.RELEASE   | Apache License 2.0 | Java     |
| Apache Commons                            | misc            | Apache License 2.0 | Java     |
| Apache Http                               | 4.3.2           | Apache License 2.0 | Java     |
| Apache Avro                               | 1.7.5           | Apache License 2.0 | Java     |
| Apache Ant                                | 1.9.4           | Apache License 2.0 | Java     |
| Apache Thrift                             | 0.9.3           | Apache License 2.0 | Java     |
| Apache Curator                            | 2.9.0           | Apache License 2.0 | Java     |
| Apache Velocity                           | 1.7             | Apache License 2.0 | Java     |
| Apache ZooKeeper                          | 3.4.6           | Apache License 2.0 | Java     |
| Twitter Commons                           | 0.0.64          | Apache License 2.0 | Java     |
| Jackson                                   | 2.4.1           | Apache License 2.0 | Java     |
| MongoDB Driver*                           | 3.0.1           | Apache License 2.0 | Java     |
| Oracle NoSQL Driver*                      | 3.1.7           | Apache License 2.0 | Java     |
| CDAP Stream client*                       | 1.0.1           | Apache License 2.0 | Java     |
| Apache Flume NG*                          | 1.5.0.1         | Apache License 2.0 | Java     |
| Ehcache                                   | 2.8.1           | Apache License 2.0 | Java     |
| Log4j                                     | 1.2.17          | Apache License 2.0 | Java     |
| Guava GWT                                 | 18.0            | Apache License 2.0 | Java     |
| Joda time                                 | 2.2             | Apache License 2.0 | Java     |
| HikariCP                                  | 2.4.2           | Apache License 2.0 | Java     |
| DataStax Java Driver for Apache Cassandra | 3.0.0           | Apache License 2.0 | Java     |
| Javassist                                 | 3.18.1-GA       | Apache License 2.0 | Java     |
| Metrics Core                              | 3.1.0           | Apache License 2.0 | Java     |
| Signpost                                  | 1.2.1.2         | Apache License 2.0 | Java     |
| Jetty                                     | 9.2.2.v20140723 | Apache License 2.0 | Java     |


"\*" - Depends on configuration of nosql database provider and/or log appenders.

# Kaa server, non-Apache 2.0 license

| Component           | Version         | Licence                                                           | Language |
|---------------------|-----------------|-------------------------------------------------------------------|----------|
| Hibernate Framework | 4.3.11.Final    | [LGPL v2.1](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)                                                         | Java     |
| Json                | 20080701        | Json License [http://www.json.org/license.html](http://www.json.org/license.html)                     | Java     |
| Scala library       | 2.11.7          | BSD-like [http://www.scala-lang.org/downloads/license.html](http://www.scala-lang.org/downloads/license.html)         | Scala    |
| SLF4j               | 1.7.7           | MIT License                                                       | Java     |
| Jline               | 2.11            | BSD                                                               | Java     |
| PostgreSQL Driver   | 9.3-1101-jdbc41 | BSD                                                               | Java     |
| Javax Activation    | 1.1             | Common Development and Distribution License (CDDL) v1.0           | Java     |
| Javax Servlet-api   | 2.5             | Common Development and Distribution License (CDDL) v1.0           | Java     |
| Javax Mail          | 1.4             | Common Development and Distribution License (CDDL) v1.0           | Java     |
| Logback             | 1.1.2           | Eclipse Public License - v 1.0; GNU Lesser General Public License | Java     |
| AspectJ             | 1.7.4           | Eclipse Public License - v 1.0                                    | Java     |
| Base64              | 2.3.8           | Public domain                                                     | Java     |
| Janino2.6.1New      | BSD             | License                                                       | Java     |

# Kaa server, testing

| Component            | Version | Licence                           | Language |
|----------------------|---------|-----------------------------------|----------|
| JUnit                | 4.11    | Common Public License Version 1.0 | Java     |
| Mockito              | 1.9.5   | MIT License                       | Java     |
| Embedded Mongo DB    | 1.42    | Apache License 2.0                | Java     |
| net.java.dev.jna:jna | 4.0.0   | LGPL, version 2.1                 | Java     |
| H2 Database Engine   | 1.4.179 | MPL 2.0, and EPL 1.0              | Java     |
| Powermock            | 1.6.4   | Apache                            | Java     |

# Kaa endpoint

| Component                                  | Version | Licence                | Language    |
|--------------------------------------------|---------|------------------------|-------------|
| Apache Avro                                | 1.7.6   | Apache License 2.0     | Java/C++    |
| Boost                                      | 1.54    | Boost Software License | C++         |
| Botan                                      | 1.11    | BSD-2 license          | C++         |
| SQLite                                     | -       | -                      | C++         |
| SLF4J                                      | 1.7.6   | MIT License            | Java        |
| Apache HttpComponents                      | 4.3.2   | Apache License 2.0     | Java        |
| EqualsVerifier (for unit testing purposes) | 1.7.6   | Apache License 2.0     | Java        |
| JUnit (for unit testing purposes)          | 4.11    | Common Public License  | Java        |
| Mockito (for unit testing purposes)        | 1.9.5   | MIT License            | Java        |
| CocoaPods                                  | 0.39.0  | СocoaPods License      | Objective-C |
| XCode                                      | 7.2     | Apple Software License | Objective-C |
