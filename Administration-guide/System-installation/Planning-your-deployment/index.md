---
layout: page
title: Planning your deployment
permalink: /:path/
nav: /:path/Administration-guide/System-installation/Planning-your-deployment
sort_idx: 10
---

* [Introduction](#introduction)
* [Fetching source code](#fetching-source-code)
* [Build artifacts](#build-artifacts)
* [Installing Kaa](#installing-kaa)
* [AWS deployment preparation](#aws)
* [Performance monitoring](#performance-monitoring)
  * [Accessing performance information](#accessing-performance-information)
  * [Performance metrics description](#performance-metrics-description)
    * [OS information](#os-information)
    * [Memory usage](#memory-usage)
    * [Thread system](#thread-system)
    * [Remote connections](#remote-connections)
  * [Third-party component metrics](#third-party-component-metrics)
* [Installing Kaa flume agents](#installing-kaa-flume-agents)
  * [Third party  components](#third-party-components)
  * [Install Kaa flume agents](#install-kaa-flume-agents)
  * [Set up Kaa flume source agent](#setup-kaa-flume-source-agent)
  * [Set up Kaa flume sink agent](#set-up-kaa-flume-sink-agent)
  * [Start Kaa flume agents](#start-kaa-flume-agents)
  * [Validate/troubleshoot Kaa flume agents](#validate/troubleshoot-kaa-flume-agents)

## Introduction

This guide describes different ways of installation and deployment of kaa platform

This page describes how to build the Kaa server from the source code available on [GitHub](https://github.com/kaaproject/kaa).

Before building the Kaa server from source, ensure that Oracle JDK 8 and Apache Maven are installed on your machine.

## Fetching source code

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

1. Single node installation
2. Node claster setup
3. AWS deployment.


## AWS

To launch the Kaa sanbox on Amazon Elastic Compute Cloud (Amazon EC2), go through the following steps.

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

The JMX metrics for the third-party components used by Kaa, such as the Java MongoDB driver or Ehcache, can be observed in their respective domains.<br/>

## Installing Kaa flume agents

### Third party  components

Kaa flume agents require the following third party components to be installed and configured.

* [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Kaa flume agents have been tested with JDK 7.
* [Apache flume NG 1.5](https://flume.apache.org/download.html) and higher. Kaa flume agents have been tested with [Cloudera Flume NG v.1.6.0](http://www.cloudera.com/content/www/en-us/documentation/cdh/5-1-x/CDH5-Installation-Guide/cdh5ig_flume_package_install.html).

### Install Kaa flume agents

Depending on the target operating system that you are using, perform the following installation steps.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Debian1">Ubuntu/Debian</a></li>
  <li><a data-toggle="tab" href="#RPM1">Red Hat/Red Hat 6/CentOS/Oracle 5</a></li>
</ul>
<div class="tab-content">
<div id="Debian1" class="tab-pane fade in active" markdown="1" ><br/>

1. Download the latest debian packages from the [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Install the Kaa flume package by executing the following command.

   ```bash
    sudo dpkg -i kaa-flume-x.y.z.deb
   ```

</div><div id="RPM1" class="tab-pane fade" markdown="1" ><br/>

1. Download the latest rpm packages from [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Install the Kaa flume package by executing the following command.

   ```bash
    sudo rpm -i kaa-flume-x.y.z.rpm
   ```

</div></div><br/>

After installing the Kaa flume package, you have to install and configure the Kaa flume source agent and Kaa flume sink agent. 

### Set up Kaa flume source agent

To install and configure the Kaa flume source agent, perform the following steps.

1. Enter the following command in the shell:

   ```bash
    sudo kaa-flume install source
   ```

2. Follow the installation instructions. You will be prompted to enter basic information. Enter the values shown in the following example after the colon (":") character.

   ```bash
    $ [INPUT] Please specify Kaa Flume Source Instance Name [default: 'default']: instance1
    $ [INPUT] Please specify Kaa Flume Source Host Name [default: 'localhost']: 10.2.3.93
    $ [INPUT] Please specify Kaa Flume Source Port: 7060
    $ [INPUT] Please specify Target Kaa Flume Sinks Count: 1
    $ [INPUT] Please specify Target Kaa Flume Sink 1 Host Name: 10.2.3.93
    $ [INPUT] Please specify Target Kaa Flume Sink 1 Port: 7070
   ```

### Set up Kaa flume sink agent

To install and configure the Kaa flume sink agent, perform the following steps.

1. Enter the following command in the shell.

   ```bash
    sudo kaa-flume install sink
   ```

2. Follow the installation instructions. You will be prompted to enter basic information. Enter the values shown in the following example after the colon (":") character.

   ```bash
    $ [INPUT] Please specify Kaa Sink Host Name: 10.2.3.93
    $ [INPUT] Please specify Kaa Sink Port: 7070
    $ [INPUT] Please specify Name node [host:port] or nameservice name: 10.2.3.93:8020
    $ [INPUT] Please specify HDFS root path [default: 'logs']: {press enter to use default location}
    $ [INPUT] Please specify Avro schema source type [rest|local] [default: 'rest']: {press enter to use Kaa REST API as Avro schema source for log events}
   ```

3. Enter information necessary to locate the schema source.   
This information depends on the Avro schema source type that you have specified in the previous step.

   * If you have specified the "rest" schema source type, enter the following:

   ```bash
    $ [INPUT] Please specify Kaa Admin Rest API host: 10.2.3.93
    $ [INPUT] Please specify Kaa Admin Rest API port [default: '8080']: 8080
    $ [INPUT] Please specify Kaa Admin User: devuser {user should have 'Tenant developer' or 'Tenant user' authority}
    $ [INPUT] Please specify Kaa Admin Password: devuser123
   ```

   * If you have specified the "local" schema source type, enter the following:

   ```bash
    $ [INPUT] Please specify Absolute path to local schema files: {specify absolute path to directory with schema files}
   ```

Please see the following additional information.

> Schema files should be stored using the following pattern:<br/>
> _{path to local schema files}_/_{application token}_/schema\_v_{log schema version}_<br/><br/>
> where:<br/><br/>
> _{application token}_ - the application token for logs collection. It can be obtained from Kaa admin web UI.<br/>
> _{log schema version}_ - the version of the log schema within the application. It can be obtained from Kaa admin web UI.<br/>
> File _schema\_v{log schema version}_ should contain an avro log schema for the specified version in the text format.

Now that you have installed and configured the Kaa flume agents, you are ready to start them.

### Start Kaa flume agents

* To start the Kaa flume source, enter the following command in the shell.

```bash
 sudo service kaa-flume-source-{source instance name} start
```

* To start the Kaa flume sink, enter the following command in the shell.

```bash
 sudo service kaa-flume-sink start
```

### Validate/troubleshoot Kaa flume agents

To validate the installation of Kaa flume agents, see the logs located at ```/var/log/flume-ng/```. If the agents were installed and configured correctly, the logs should not contain any exceptions or errors.

