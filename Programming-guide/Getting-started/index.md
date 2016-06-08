---
layout: page
title: Getting started
permalink: /:path/
nav: /:path/Programming-guide/Getting-started/
sort_idx: 10
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

- [Kaa sandbox](#kaa-sandbox)
  - [Video tutorial](#video-tutorial)
  - [Installation](#installation)
    - [System requirements](#system-requirements)
    - [Installation steps](#installation-steps)
    - [Know issues](#know-issues)
    - [Troubleshooting](#troubleshooting)
  - [Configuration](#configuration)
    - [Outgoing mail settings](#outgoing-mail-settings)
    - [Networking](#networking)
  - [Kaa Sandbox web UI](#kaa-sandbox-web-ui)
    - [Demo projects](#demo-projects)
    - [Admin UI](#admin-ui)
    - [Avro UI](#avro-ui)
- [Your first Kaa application](#your-first-kaa-application)
  - [Adding application](#adding-application)
- [Next Steps](#next-steps)
- [Further reading](#further-reading)


This section provides guidance on how to create your first Kaa application that will work with the Kaa platform. In this guide we will show you how to create a simple desktop java application that will receive notifications from the Kaa server and display them on the console. We will define our own notification schema and use the generated java classes within our application.

# Kaa Sandbox

Kaa Sandbox is a private Kaa environment which includes demo client applications. Sandbox includes all necessary Kaa components in a convenient virtual environment that can be set up in just 5 minutes!
With the use of Kaa Sandbox, anyone can learn Kaa, build a proof of concept and test their own applications locally.

## Video tutorial

<p align="center">
  <iframe width="800" height="500" src="https://www.youtube.com/embed/ynbxcRdgXFU">
  </iframe>
</p>

## Installation Sandbox

Kaa Sandbox is presented as a stand-alone virtual machine.

### System requirements

To use Kaa Sandbox, your system must meet the following minimum system requirements.

- 64-bit OS
- 4GB RAM
- Virtualization enabled in BIOS

### Installation steps

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#VirtualBox">VirtualBox</a></li>
  <li><a data-toggle="tab" href="#Amazon">Amazon</a></li>
</ul>

<div class="tab-content">

<div id="VirtualBox" class="tab-pane fade in active" markdown="1">

<br>

To install Kaa Sandbox, perform the following steps:

1. Install the virtualization environment.
The current version of Kaa Sandbox supports [Oracle VirtualBox 4.2+](https://www.virtualbox.org/wiki/Downloads) which is available as a free download.

2. Download the Sandbox image from [Kaa download page](http://www.kaaproject.org/download-kaa/).

3. Import the Sandbox image using this [guide](https://www.virtualbox.org/manual/ch01.html#ovf).

<br>

</div><div id="Amazon" class="tab-pane fade" markdown="1">

<br>

To launch the Kaa sanbox on Amazon Elastic Compute Cloud (Amazon EC2), go through the following steps.

1. Launch the AMI using the links in the following table:

   Amazon EC2 offers a number of [geographic regions](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html) for launching the AMI. Factors for choosing a region include: reduce latency, cost, or regulatory requirements.

   To launch an AMI for a specific region, please use the [download](http://www.kaaproject.org/download-kaa/) page.

2. On **Choose an Instance Type** step, choose the appropriate instance type. For optimal performance we recommended that you use at least _m3.large_ instance type, or more powerful.

3. On **Configure Instance Details** step, change values in the fields as appropriate or leave default values.

4. On **Add Storage** step, add additional volumes as appropriate.

   The number of instance store devices available on the machine depends on the instance type. EBS volumes are not recommended for the database storage.

5. On **Tag Instance** step, give a name to your instance, for example, kaa-sandbox-0.10.0

6. On **Configure Security Group** step, select one of the following options.

   * Create a new security group with the inbound open ports:

      | Protocol | Port | RangeSource|
      |----------|------|------------|
      | TCP      | 22   | 0.0.0.0/0  |
      | TCP      | 8080 | 0.0.0.0/0  |
      | TCP      | 9999 | 0.0.0.0/0  |
      | TCP      | 9998 | 0.0.0.0/0  |
      | TCP      | 9997 | 0.0.0.0/0  |
      | TCP      | 9889 | 0.0.0.0/0  |
      | TCP      | 9888 | 0.0.0.0/0  |
      | TCP      | 9887 | 0.0.0.0/0  |
      | TCP      | 9080 | 0.0.0.0/0  |

   * Select the created security group.

7. On **Review Instance Launch** step, make any changes as appropriate.

8. Click **Launch** and then in the **Select an existing key pair** or **Create a new key pair** dialog, do one of the following:

  * Select an existing key pair from the **Select a key pair** drop list.

  * If you need to create a new key pair, click **Create a new key pair**. Then create the new key pair as described in [Creating a key pair](http://docs.aws.amazon.com/gettingstarted/latest/wah/getting-started-prereq.html).

9. Click **Launch Instances**. The **Launch Status** page will be displayed.

10. Click **View Instances**.

11. After launching Kaa Sandbox instance, go to **<your\_instance\_public\_dns\>:9080/sandbox** or **<your\_instance\_public\_ip\>:9080/sandbox** URL. Public DNS or IP of your instance are available from your instance description.

<br>

</div></div>

### Known issues

Please take into account the following known issues and limitations of Kaa Sandbox.

* Without the SMTP server configured, you will not be able to create new users. See the [Outgoing mail settings section](#outgoing-mail-settings) for more details.


### Troubleshooting

Common issues covered in this [guide]({{root_url}}Administration-guide/Troubleshooting/).

## Configuration

### Outgoing mail settings

Outgoing mail settings are used to send emails to newly created users with the information about their passwords, as well as other notifications.
By default, outgoing mail settings are not configured for Admin UI. To target Admin UI to your SMTP server refer to the [Admin UI guide***]({{root_url}}Administration-guide/Tenants-and-applications-management).

### Networking

By default, Kaa Sandbox components are accessible from a host machine only. But if you want to share Kaa Sandbox in the local network you need to reconfigure the network interface for this virtual machine in [Bridge mode](https://www.virtualbox.org/manual/ch06.html#network_bridged). Once the virtual box is available to devices on your local/test network, you need to change Sandbox host/IP on [web UI](#kaa-sandbox-web-ui) or execute the script on Sandbox.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console">Console</a></li>
</ul>

<div class="tab-content">

<div id="Sandbox-web-ui" class="tab-pane fade in active" markdown="1">

<br>

Go to Kaa Sandbox web UI and in a upper right corner select "Management" menu item. Input new host/IP and click the "Update" button in the "Kaa host/IP" section.

<br>

<p align="center">
  <img src="attach/managment_tab.png" width="800" height="500">
</p>

</div><div id="Console" class="tab-pane fade" markdown="1">

<br>

```sh
sudo /usr/lib/kaa-sandbox/bin/change_kaa_host.sh <new host/ip>
```

<br>

</div></div>

## Kaa Sandbox web UI

Kaa Sandbox web UI provides you with access to Kaa demo projects and some basic Kaa Sandbox configuration capabilities. Once Kaa Sandbox is installed and opened, the web UI is available at the following URL (by default): [http://127.0.0.1:9080/sandbox](http://127.0.0.1:9080/sandbox).

### Demo projects

You can download both source and binary distributions for each demo project. A downloaded binary already contains Kaa SDK that targets current Kaa Sandbox. Thus, if you successfully configure the [networking](#networking) for your SDK, the downloaded application will be able to access it and will work correctly.

<br>

<p align="center">
  <img src="attach/Sandbox.png" width="800" height="400">
</p>

## Admin UI

You can access Admin UI by clicking **Administrative console** at the top of the window.
Refer to the [Admin UI guide]({{root_url}}Administration-guide/Tenants-and-applications-management) for working instructions.
**NOTE**
Kaa Sandbox provides default credentials for all three types of Kaa users, as follows:
* Kaa admin - kaa/kaa123
* Tenant admin - admin/admin123
* Tenant developer - devuser/devuser123

## Avro UI

You can access Avro UI by clicking **Avro UI sandbox console** at the top of the window.
Refer to the [Avro UI guide]({{root_url}}Administration-guide/Tenants-and-applications-management)  for working instructions.

# Next steps
To create a real-world IoT solution, you will most likely need to implement more features into your application. Kaa provides you with practically everything you might need. The following overview will help you grasp the scope of Kaa capabilities as well as get familiar with the essential documentation, such as [Programming guide]({{root_url}}Programming-guide) and [Administration UI]({{root_url}}Administration-guide/Tenants-and-applications-management) guide.

**Profiling and grouping**
During a new endpoint registration, Kaa creates an associated _endpoint profile_ for the endpoint. An endpoint profile is basically some meaningful information about the endpoint which may be useful for specific applications. Profiles may contain things like an OS version, amount of RAM, average battery life, type of network connection, device operation mode – virtually anything. An endpoint profile structure in Kaa is configured using a client-side endpoint profile schema. Based on the defined profile schema, Kaa generates an object model to operate against the client side and handles data marshaling all the way to the database. Whenever the client updates its profile information, the endpoint SDK automatically sends these updates to the server as soon as the connection becomes available.

For programming practice, see [collecting endpoint profiles]({{root_url}}Programming-guide/Key-system-features/Endpoint-profiling/).

The information collected in an endpoint’s profile can be used to group endpoints into independently managed entities called _endpoint groups_. On the back end, Kaa provides a [profile filtering language***]() for defining the criteria for group membership. An endpoint can belong to any number of groups. Grouping endpoints can be used, for example, to send targeted notifications or adjust software behavior by applying group-specific configuration overrides.

For programming practice, see [using endpoint groups]({{root_url}}Programming-guide/Key-system-features/Endpoint-groups-management/).

**Events**
Kaa allows for delivery of _events_, which are structured messages, across endpoints. When endpoints register with the Kaa server, they communicate which event types they are able to generate and receive. Kaa allows endpoints to send events either to virtual “chat rooms” or to individual endpoints. Events can even be delivered across applications registered with Kaa – making it possible to quickly integrate and enable interoperability between endpoints running different applications. Some examples are: a mobile application that controls house lighting, a car’s GPS that communicates with the home security system, a set of integrated audio systems from different vendors that deliver a smooth playback experience as you walk from one room to another. Kaa events are implemented in a generic, abstract way, using non-proprietary schema definitions that ensure identical message structures. The schema provides independence from any specific functionality implementation details.
For programming practice, see [messaging across endpoints]().

**Collecting data**
Kaa provides rich capabilities for collecting and storing structured data from endpoints. A typical use-case is collecting various types of logs: performance, user behavior, exceptional conditions, etc.

Using a set of pre-packaged server-side _log appenders_, the Kaa server is able to store records to a filesystem, a variety of big data platforms (Hadoop, MongoDB, Cassandra, Oracle NoSQL etc.), or submit them directly to a streaming analytics system. It is also possible to [create a custom log appender***]().

The structure of the collected data is flexible and defined by the [log schema***](). Based on the log schema defined for the Kaa application, Kaa generates an object model for the records and the corresponding API calls in the client SDK. Kaa also takes care of data marshalling, managing temporary data storage on the endpoint, and uploading data to the Kaa server.

For programming practice, see [collecting data from endpoints***]().

**Using notifications**
Kaa uses _notifications_ to distribute structured messages, posted within _notification topics_, from the server to endpoints. A notification structure is defined by a corresponding [notification schema***]().

Endpoint are subscribed to notification topics, which can be either mandatory or optional. Access to notification topics is automatically granted according to the endpoint’s group membership. Notifications can be sent either to every endpoint subscribed to a topic or to an individual endpoint.

Notifications can be assigned expiration timestamps to prevent their delivery after a certain period of time.

For programming practice, see [using notifications***]().

**Distributing operational data**
Kaa allows you to perform operational data updates, such as configuration data updates, from the Kaa server to endpoints. This feature can be used for centralized configuration management, content distribution, etc. Since Kaa works with structured data and constraint types, it guarantees data integrity.

The Kaa server monitors the database for changes and distributes updates to endpoints in the incremental form, thus ensuring efficient bandwidth use. The endpoint SDK performs data merging and persistence, as well as notifies the client code about the specific changes made to the data. As a result, the client application knows exactly where in the data structure the changes occurred and can be programmed to react accordingly.

Based on the endpoint’s group membership, it is possible to control what data is available to the endpoint. This is achieved by applying group-specific data overrides, which make it possible to adjust the behavior of the client application based on operational conditions or usage patterns, fine-tune the algorithms according to feedback, implement gradual feature roll-out, A/B testing, etc.
For programming practice, see [distributing data to endpoints***]().

# Further reading

Use the following guides and references to make the most of Kaa.

| Guide                                                          | What it is for                                                                                                                                                                                           |
|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Key system features]({{root_url}}Programming-guide/Key-system-features/)                           | Use this reference to learn about features and capabilities of Kaa \([Endpoint profiling]({{root_url}}Programming-guide/Key-system-features/Endpoint-profiling/), [Events\*\*\*](#), [Notifications\*\*\*](#), [Logging\*\*\*](#), and other features\). |
| [Installation guide]({{root_url}}Administration-guide/System-installation)                       | Use this guide to install and configure Kaa either on a single Linux node or in a cluster environment.                                                                                                    |
| [Contribute To Kaa]({{root_url}}Customization-guide/How-to-contribute/)                       | Use this guide to learn how to contribute to Kaa project and which code/documentation style conventions we adhere to.                                                                                                   |
