---
layout: page
title: Java
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to use Kaa [Java SDK]({{root_url}}Glossary/#endpoint-sdk) for your IoT applications.
It describes the environment setup, basic API, and provides comparison between desktop and Android versions of the Kaa Java SDK.
The Kaa Java SDK should be preferred if you want to run your application on different platforms, or if you plan on developing an Android application.

## Prerequisites

To follow this guide, you will need:

- A text editor or IDE
- JDK version 1.7 or newer

## Environment setup

To set up your Java SDK environment:

1. [Generate endpoint SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk) for your Java platform.

2. Add the generated .jar file to the classpath by using appropriate build tools.
For example, you can create the a **lib** folder and put the generated .jar file in it.

	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#maven-sdk">Maven</a></li>
		<li><a data-toggle="tab" href="#gradle-sdk">Gradle</a></li>
	</ul>
<ul>
<div class="tab-content">

<div id="maven-sdk" class="tab-pane fade in active" markdown="1">
```xml
<repositories>
	<repository>
		<id>local-maven-repo</id>
		<url>file:///${project.basedir}/lib</url>
	</repository>
</repositories>

...

<dependency>
	<groupId>org.kaaproject.kaa.client</groupId>
	<artifactId>client-java-desktop</artifactId>
	<version>${build-version}</version>
	<scope>system</scope>
	<systemPath>${project.basedir}/lib/kaa-java-ep-sdk-LVcpSA4q5BiErcm10Zg86TSGL9s.jar</systemPath>
</dependency>
```
</div>
<div id="gradle-sdk" class="tab-pane fade" markdown="1">
```groovy
dependencies {
   compile  files('./lib/kaa-java-ep-sdk-LVcpSA4q5BiErcm10Zg86TSGL9s.jar')
}
```
</div>
</div>
</ul>

<ol>
<li value="3">
Add the following dependencies to enable logging.
</li>
</ol>

<ul>
<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#Maven">Maven</a></li>
	<li><a data-toggle="tab" href="#Gradle">Gradle</a></li>
</ul>
</ul>

<ul>
<div class="tab-content">

<div id="Maven" class="tab-pane fade in active" markdown="1">
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.7</version>
</dependency>

<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-core</artifactId>
    <version>1.1.2</version>
</dependency>

<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.1.2</version>
</dependency>
```
</div>
<div id="Gradle" class="tab-pane fade" markdown="1">

```groovy
compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.7'

compile group: 'ch.qos.logback', name: 'logback-core', version: '1.1.2'

compile group: 'ch.qos.logback', name: 'logback-classic', version: '1.1.2'
```
</div>
</div>
</ul>

When you finish setting up the configurations, you can start writing your application.


## Basic API overview

First, you need to create an instance of `KaaClient`.
Kaa SDK provides you with the `Kaa` class factory to be used for creating new instances of [Kaa client]({{root_url}}Glossary/#kaa-client).
See the code example below.

<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#java">Java</a></li>
	<li><a data-toggle="tab" href="#android">Android</a></li>
</ul>


<div class="tab-content">
<div id="java" class="tab-pane fade in active" markdown="1">

```java
KaaClient client = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener());
```
</div>

<div id="android" class="tab-pane fade" markdown="1">

```java
KaaClient client = Kaa.newClient(new AndroidKaaPlatformContext(), new SimpleKaaClientStateListener());
```
</div>
</div>

The `newClient()` static method receives two mandatory arguments -- the platform-specific context and the implementation of the client state listener interface.

The first argument can be either `DesktopKaaPlatformContext` for Java desktop or `AndroidKaaPlatformContext` for Android applications.

In this example, the second argument is the default implementation of `KaaClientStateListener` -- `SimpleKaaClientStateListener`, that is solely used to log the client state changes.
Whenever a new instance of Kaa client is created, the `start()` method is called to start the client operation and communication with the server.
Starting from this point, you can use any features provided by the [Kaa platform]({{root_url}}Glossary/#kaa-platform), such as [data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/), [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), etc., in your application code.
When you no longer need the client, you can call the `stop()` method to release resources and stop the client communication with the server.


## Client state

When Kaa client starts for the first time, it generates a private/public key pair and saves those keys in the key.private and key.public files accordingly.
These keys are used to maintain secure communication with the server.
Kaa client also creates the state.properties file that is used to persist the parameters that handle the client state during its communication with the server.

In case with Java desktop application, all these files are created by default in the working directory.
However, you can specify a different folder using `KaaClientProperties`.
To do this, set the path to the new folder using the `setWorkingDirectory()` method and then pass the client properties instance as an argument of the `DesktopKaaPlatformContext` constructor.


## Platform comparison

The only difference between the Android client and the Java desktop client is the implementation of the `KaaPlatformContext` class.
See the table below for comparison between the desktop and Android clients.

|Method|Desktop|Android| Description |
|---|---|---|
|`createHttpClient`| Same | Same  | Creates an HTTP client that is used in HTTP Kaa data channel to send responses to the server.|
|`createPersistentStorage`|  Uses file storage | Uses Android internal storage | Persists the client state.|
|`getBase64`|  Uses Apache Base64 |  Uses Android Base64 | Encodes/decodes data to be sent over network without losses, e.g. an endpoint key hash.|
|`createConnectivityChecker`|  Same  |  Same | Checks network connection.|
|`getExecutorContext`| Same  | Same  | Creates instances of the `ExecutorService` class for SDK internal usage.|
|`getProperties`| Same  | Same  | Returns `KaaClientProperties` that contains important information about the client SDK.|
|`needToCheckClientState`| Returns **true**  | Returns **false** | Set off/on to check feasibility of the transition between the life sycle states.|


## ExecutorContext configuration
Both `AndroidKaaPlatformContext` and `DesktopKaaPlatformContext` allow you to specify `ExecutorContext`.
The `ExecutorContext` class provides implementation and configuration of the `ExecutorService` class that is used to run internal `KaaClient` tasks.
Below is the list of `ExecutorService` implementations.

|ExecutorService|Responsibilities|
|---|---|
|LifeCycleExecutor|Handles `KaaClient` lifecycle events (Start/Stop/Pause/Resume), processes callbacks from `KaaClientStateListener`.|
|ApiExecutor|Processes client's calls to server. For example, persistence of log records into internal log storage and enqueuing for further forwarding to server.|
|CallbackExecutor|Handles events from server and callbacks from listeners: <br />`AttachEndpointToUserCallback`<br />`ConfigurationListener`<br />`DetachEndpointFromUserCallback`<br />`FindEventListenersCallback`<br />`LogDeliveryListener`<br />`NotificationListener`<br />`NotificationTopicListListener`<br />`OnAttachEndpointOperationCallback`<br />`OnDetachEndpointOperationCallback`<br />`UserAttachCallback`|
|ScheduledExecutor|Handles log forwarding to server, runs failover and bootstrap tasks.|

### Available ExecutorService implementations

**SimpleExecutorContext** --- this implementation provides one `java.util.concurrent.ScheduledExecutorService` for `ScheduledExecutor` and three separate instances of `java.util.concurrent.ExecutorService` for `LifeCycleExecutor`, `ApiExecutor`, and `CallbackExecutor`.
The `ExecutorService` instances contain fixed number of threads in the pool.
A `SimpleExecutorContext` with one thread for each  `ExecutorService` is a default `ExecutorContext` implementation used by `KaaClient`.
To configure `SimpleExecutorContext`, follow the instruction below.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#java-ec">Java</a></li>
  <li><a data-toggle="tab" href="#android-ec">Android</a></li>
</ul>


<div class="tab-content">
<div id="java-ec" class="tab-pane fade in active" markdown="1">

```java
int threadsCount = 2;
ExecutorContext executorContext = new SimpleExecutorContext(threadsCount, threadsCount, threadsCount, threadsCount);
DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(new KaaClientProperties(), executorContext);
KaaClient kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener(), true);
```
</div>

<div id="android-ec" class="tab-pane fade" markdown="1">

```java
android.content.Context context = this; //to be specified by implementor
int threadsCount = 2;
ExecutorContext executorContext = new SimpleExecutorContext(threadsCount, threadsCount, threadsCount, threadsCount);
AndroidKaaPlatformContext androidKaaPlatformContext = new AndroidKaaPlatformContext(context, new KaaClientProperties(), executorContext);
KaaClient kaaClient = Kaa.newClient(androidKaaPlatformContext, new SimpleKaaClientStateListener(), true);
```
</div>
</div>

**SingleThreadExecutorContext** --- this implementation shares single `java.util.concurrent.ScheduledExecutorService` with one thread in pool for `LifeCycleExecutor`, `ApiExecutor`, `CallbackExecutor`, and `ScheduledExecutor`.

>**NOTE:** Avoid running any blocking or long running tasks within listeners passed to `KaaClient`.
>Make sure that `ExecutorContext` is properly configured.
{:.note}