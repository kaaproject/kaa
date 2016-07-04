---
layout: page
title: Planning your deployment
permalink: /:path/
sort_idx: 10
---

* [Introduction](#introduction)
* [Building Kaa from source](#building-kaa-from-source)
  * [Fetching source code](#fetching-source-code)
  * [Build artifacts](#build-artifacts)
* [Installing Kaa](#installing-kaa)
* [AWS deployment preparation](#aws-deployment-preparation)
* [Performance monitoring](#performance-monitoring)
  * [Accessing performance information](#accessing-performance-information)
  * [Performance metrics description](#performance-metrics-description)
    * [OS information](#os-information)
    * [Memory usage](#memory-usage)
    * [Thread system](#thread-system)
    * [Remote connections](#remote-connections)
  * [Third-party component metrics](#third-party-component-metrics)
* [Storing custom schemas](#storing-custom-schemas)
* [Troubleshooting](#troubleshooting)

## Introduction

This page describes common ways to setup and deploy your kaa instance and its environment and also some best practices.

## Building Kaa from source

This paragraph describes how to build the Kaa server from the source code available on [GitHub](https://github.com/kaaproject/kaa).
Before building the Kaa server from source, ensure that [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Apache Maven](https://maven.apache.org/) are installed on your machine.

### Fetching source code

It is allowed to use any Git client to fetch the Kaa source code from the repository.

[Set up your Git configuration](https://git-scm.com/book/tr/v2/Customizing-Git-Git-Configuration) (at least the username and email) and download Kaa repository as follows:

```bash
 $ git clone https://github.com/kaaproject/kaa.git
```

To build Kaa node Debian/RPM packages, execute the following command.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Debian">Ubuntu</a></li>
  <li><a data-toggle="tab" href="#RPM">CentOS</a></li>
</ul>
<div class="tab-content">
<div id="Debian" class="tab-pane fade in active" markdown="1" ><br/>  
          
```bash
 $ mvn -P compile-gwt,mongo-dao clean install verify
```

</div><div id="RPM" class="tab-pane fade" markdown="1" ><br/>
      
```bash
 $ mvn -P compile-gwt,mongo-dao,build-rpm clean install verify
```

</div></div><br/>

**NOTE**

The Debian build will work correctly on both Linux and Windows operation systems, while the RPM build will work only on Linux operated machines with the RPM tool installed.

**NOTE**

For the mvn command, the build number and git commit variables are set to emulate Jenkins build variables that are substituted automatically on the build machine.

**NOTE**

Please add "-DskipTests" suffix to the mvn command in order to skip execution of tests and speed up build process.

## Build artifacts

The following command can be used to browse the Kaa node build artifacts in case of successful build.

    $ ls server/node/target/kaa-node.deb

    $ ls server/node/target/rpm/kaa-node/RPMS/noarch/kaa-node*.rpm

## Installing Kaa

You can deploy Kaa server as a single node or as multi node cluster, for more details refer to corresponding pages:

* [Single node installation](../Single-node-installation)
* [Node cluster setup](../Cluster-setup)

## AWS deployment preparation

To launch the Kaa sandbox on Amazon Elastic Compute Cloud (Amazon EC2), go through the following steps.

1. Launch the AMI using the links in the following table:

   Amazon EC2 offers a number of [geographic regions](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html) for launching the AMI. Factors for choosing a region include: reduce latency, cost, or regulatory requirements.

   To launch an AMI for a specific region, please use the [download](http://www.kaaproject.org/download-kaa/) page.

2. On**Choose an Instance Type** step, choose the appropriate instance type. For optimal performance we recommended that you use at least _m3.large_ instance type, or more powerful.

3. On **Configure Instance Details** step, change values in the fields as appropriate or leave default values.

4. On **Add Storage** step, add additional volumes as appropriate. The number of instance store devices available on the machine depends on the instance type. EBS volumes are not recommended for the database storage.

5. On **Tag Instance** step, give a name to your instance, for example, kaa-sandbox-0.8.0

6. On **Configure Security Group** step, select one of the following options.

   1. Create a new security group with the inbound open ports:

       | Protocol | Port | RangeSource |
       | -------- | ---- | ----------- |
       | TCP      | 22   | 0.0.0.0/0   |
       | TCP      | 8080 | 0.0.0.0/0   |
       | TCP      | 9999 | 0.0.0.0/0   |
       | TCP      | 9998 | 0.0.0.0/0   |
       | TCP      | 9997 | 0.0.0.0/0   |
       | TCP      | 9889 | 0.0.0.0/0   |
       | TCP      | 9888 | 0.0.0.0/0   |
       | TCP      | 9887 | 0.0.0.0/0   |
       | TCP      | 9080 | 0.0.0.0/0   |

   2. Select the created security group.

7. On **Review Instance Launch** step, make any changes as appropriate.

8. Click **Launch** and then in the **Select an existing key pair** or **Create a new key pair** dialog, do one of the following:

   * Select an existing key pair from the **Select a key pair** drop list.
   * If you need to create a new key pair, click **Create a new key pair**. Then create the new key pair as described in [Creating a key pair](http://docs.aws.amazon.com/gettingstarted/latest/wah/getting-started-prereq.html).

9. Click **Launch Instances**. The **Launch Status** page will be displayed.

10. Click **View Instances**.

11. After launching Kaa Sandbox instance, go to <**your\_instance\_public\_dns\>:9080/sandbox** or **<your\_instance\_public\_ip\>:9080/sandbox** URL. Public DNS or IP of your instance are available from your instance description.
  
12. For more information about Kaa Sandbox please visit this [page](https://docs.kaaproject.org/display/KAA/Sandbox).

## Performance monitoring

### Accessing performance information

Each Kaa node exposes a number of system performance metrics through the [JMX](https://docs.oracle.com/javase/tutorial/jmx/overview/index.html) technology. The easiest way to access the metrics information is by using [JConsole](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html), a JMX-compliant tool that comes as a part of the JDK.

> The default JMX port for Kaa is 7091.

### Performance metrics description

The metrics described in this section are available in the org.kaaproject.kaa.metrics domain.

![alt text](jmx.png)

 
#### OS information

| Metric name         | Description                                                                                    |
| ------------------- | ---------------------------------------------------------------------------------------------- |
| system-load-average | The [system load](https://en.wikipedia.org/wiki/Load_(computing)) average for the last minute. |

 
#### Memory usage

| Metric name              | Description                                                                        |
| ------------------------ | ---------------------------------------------------------------------------------- |
|heap-memory-usage.mb      | The current memory usage of the heap memory that used for object allocation, in MB.|
| non-heap-memory-usage.mb | The current memory usage of non-heap memory, in MB.                                |

 
#### Thread system

| Metric name  | Description                                                                  |
| ------------ | ---------------------------------------------------------------------------- |
| thread-count | The current number of live threads (including both daemons and non-daemons). |

 
#### Remote connections

| Metric name          | Description                                                       |
| -------------------- | ----------------------------------------------------------------- |
| sessionInitMeter     | The current number of session initialization requests processed.  |
| sessionRequestMeter  | The current number of session messages processed.                 |
| sessionResponseMeter | The current number of session message responses.                  |
| redirectMeter        | The current number of session messages redirected to other nodes. |
| errorMeter           | The current number of errors during message processing.           |

> Each thread that handles a remote connection has a set of metrics sharing the names above, followed by the thread name. For example, a thread named foo manages a set of metrics called sessionRequestMeter.foo, sessionResponseMeter.foo, and so on.

  
### Third-party component metrics

The JMX metrics for the third-party components used by Kaa, such as the Java MongoDB driver or Ehcache, can be observed in their respective domains.

## Storing custom schemas

Our best practices is to provide all schemas next to source code, covered by some version control system (VCS).
Also if you need to compile some of your schemas or you need to serialize data by some schema you can refer to [Apache Avro](https://avro.apache.org/docs/1.7.7/index.html) documentation.

## Troubleshooting

Common issues covered in this [guide](../Troubleshooting).

