---
layout: page
title: Java
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to start using the Kaa [Java SDK]({{root_url}}Glossary/#endpoint-sdk) for your IoT applications.
It describes the environment setup, basic API, and provides comparison between desktop and Android versions of the Kaa Java SDK.
The Kaa Java SDK should be preferred if you want to run your application on a variety of comparatively powerful platforms, or if you plan on developing an Android application.

You can find auto-generated docs for Core Kaa Java SDK [here]({{site.baseurl}}/autogen-docs/client-java-core/{{version}}/) with desktop flavor covered [here]({{site.baseurl}}/autogen-docs/client-java-android/{{version}}/), and Android [here]({{site.baseurl}}/autogen-docs/client-java-android/{{version}}/).

## Prerequisites

- Your favorite text editor or IDE
- JDK 1.7 or higher

## Environment setup

To set up your Java SDK environment:

1. [Generate endpoint SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk) for target platform 'Java'.

2. Add the generated `.jar` file to the classpath by using appropriate build tools.
For example, you can create the `lib` folder and put there the generated `.jar` file.

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

After completing the configurations, you can start writing your application.

## Basic API overview

First, you have to create an instance of `KaaClient`.
The Kaa SDK provides you with the `Kaa` class factory, which should be used for creating new instances of the [Kaa client]({{root_url}}Glossary/#kaa-client).
See the following code example.


<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#java">Java</a></li>
	<li><a data-toggle="tab" href="#android">Android</a></li>
</ul>


<div class="tab-content">
<div id="java" class="tab-pane fade in active" markdown="1">

```java
KaaClient client = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener(), true);
```
</div>

<div id="android" class="tab-pane fade" markdown="1">

```java
KaaClient client = Kaa.newClient(new AndroidKaaPlatformContext(), new SimpleKaaClientStateListener(), true);
```
</div>
</div>

The `newClient()` static method receives three mandatory arguments -- the platform-specific context, the implementation of the client state listener interface and the boolean argument that specifies whether to generate a public/private key pair automatically.

The first argument can be either `DesktopKaaPlatformContext` for Java desktop or `AndroidKaaPlatformContext` for Android applications.
In this example, the second argument is the default implementation of `KaaClientStateListener` -- `SimpleKaaClientStateListener`, which is solely used to log the client state changes.
As to the third argument: when it is set to **false**, an existing pre-generated public/private key pair is used; when **true**, a new public/private key pair is auto-generated if there is none.


You can configure properties and `ExecutorContext` of `DesktopKaaPlatformContext` with help of its constructors. 
`DesktopKaaPlatformContext` uses `FlexibleExecutorContext` implementation of `ExecutorContext` by default. 
You can change it by calling corresponding the constructor with a specific `ExecutorContext` implementation.

`FlexibleExecutorContext` has four thread groups, responsible for life cycle, callbacks, API, and scheduling. 
Each of these groups is implemented as separate thread pool (using `ThreadPoolExecutor` or its subclass). 
Each of them has minimum and maximum threads amount and maximum thread idle time. 
By default minimum threads amount for all these groups is zero, maximum threads amount is almost not limited (it set to `Integer.MAX_VALUE`) and maximum idle time is 100 milliseconds. 
You can call the corresponding constructor to limit maximum threads amount or idle time (minimum threads amount is always zero). 
For scheduled thread groups, you can change only its minimum threads amount. 
It is recommended to use the builder to construct the `FlexibleExecutorContext` instance with custom parameters.

The other `ExecutorContext` implementation which goes with Kaa is `SimpleExecutorContext`. 
Its minimum threads amount is 1 for each group. 
And its maximum threads amount is also limited to 1 by default but can be changed with a proper constructor (see example below).

Here are some examples for using a non-empty constructor and using builder.

<ul class="nav nav-tabs">
<li class="active"><a data-toggle="tab" href="#not-empty-constructor">DesktopKaaPlatformContext</a></li>
<li><a data-toggle="tab" href="#builder">FlexibleExecutorContext builder</a></li>
<li><a data-toggle="tab" href="#simpleExecutorContext">SimpleExecutorContext</a></li>
</ul>


<div class="tab-content">
<div id="not-empty-constructor" class="tab-pane fade in active" markdown="1">

```java
KaaClientProperties properties = new KaaClientProperties();
properties.put(CUSTOM_PROPERTY_NAME, CUSTOM_PROPERTY_VALUE);
DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(properties,
	 CUSTOM_MAX_LIFE_CYCLE_THREADS, CUSTOM_MAX_API_THREADS,
	 CUSTOM_MAX_CALLBACK_THREADS, CUSTOM_MIN_SCHEDULED_THREADS
 ); // If you have no properties to set, just pass null instead of first constructor's argument
 
KaaClient client = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener(), true);
```
</div>

<div id="simpleExecutorContext" class="tab-pane fade" markdown="1">

```java
ExecutorContext executor = new SimpleExecutorContext(
	 CUSTOM_MAX_LIFE_CYCLE_THREADS, CUSTOM_MAX_API_THREADS,
	 CUSTOM_MAX_CALLBACK_THREADS, CUSTOM_MIN_SCHEDULED_THREADS
 );

DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(null, executor);
KaaClient client = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener(), true);
```
</div>

<div id="builder" class="tab-pane fade" markdown="1">

```java
ExecutorContext executorContext = new FlexibleExecutorContext.FlexibleExecutorContextBuilder()
	 .setMaxLifeCycleThreads(CUSTOM_MAX_LIFE_CYCLE_THREADS) 
	 .setMaxLifeCycleThreadsIdleMilliseconds(CUSTOM_MAX_LIFECYCLE_THREADS_IDLE_MILLISECONDS)
	 .setMaxApiThreads(CUSTOM_MAX_API_THREADS) 
	 .setMaxApiThreadsIdleMilliseconds(CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS) 
	 .setMaxCallbackThreads(CUSTOM_MAX_CALLBACK_THREADS)
	 .setMaxCallbackThreadsIdleMilliseconds(CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS)
	 .setMinScheduledThreads(CUSTOM_MIN_SCHEDULED_THREADS) 
	 .build();
	 
DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(null, executorContext);
KaaClient client = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener(), true);
```
</div>

</div>

Whenever a new instance of the Kaa client is created, the `start()` method is invoked to start the client operation and communication with the server.
Starting from this point, you can use any features provided by the [Kaa platform]({{root_url}}Glossary/#kaa-platform), such as [data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/), [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), etc., in your application code.
When you no longer need the client, you can call the `stop()` method to release resources and stop the client communication with the server.


## Client state

When the Kaa client starts for the first time, it generates a private/public key pair and saves those keys in the `key.private` and `key.public` files accordingly.
These keys are used to maintain secure communication with the server.
The Kaa client also creates the `state.properties` file that is used to persist the parameters that handle the client state during its communication with the server.

In the case of a Java desktop application, all these files are created by default in the working directory.
However, you can specify a different folder by using `KaaClientProperties`.
For that purpose, set the path to the new folder by using the `setWorkingDirectory()` method and then pass the client properties instance as an argument for the `DesktopKaaPlatformContext` constructor.


## Platform comparison

The only difference between the Android client and the Java desktop client is the implementation of the `KaaPlatformContext` class.
The following table illustrates key differences between them.

|Method|Desktop|Android| Description |
|---|---|---|
|`createHttpClient`| Same | Same  | Creates an HTTP client that is used in HTTP Kaa data channel to send responses to the server.|
|`createPersistentStorage`|  Uses file storage | Uses Android internal storage | Persists the client state.|
|`getBase64`|  Uses Apache Base64 |  Uses Android Base64 | Encodes/decodes data to be sent over network without losses, e.g. an endpoint key hash.|
|`createConnectivityChecker`|  Same  |  Same | Checks network connection.|
|`getExecutorContext`| Same  | Same  | Creates instances of the `ExecutorService` class for SDK internal usage.|
|`getProperties`| Same  | Same  | Returns `KaaClientProperties` that contains important information about the client SDK.|
|`needToCheckClientState`| Returns **true**  | Returns **false** | Set off/on to check feasibility of the transition between the life cycle states.|


## ExecutorContext configuration
Both `AndroidKaaPlatformContext` and `DesktopKaaPlatformContext` allow you to specify `ExecutorContext`.
The `ExecutorContext` class provides implementation and configuration of the `ExecutorService` class, which is used to run internal `KaaClient` tasks.
The following table provides `ExecutorService` implementations.

|ExecutorService|Responsibilities|
|---|---|
|LifeCycleExecutor|Handles `KaaClient` lifecycle events (Start/Stop/Pause/Resume), processes callbacks from `KaaClientStateListener`.|
|ApiExecutor|Processes the client's calls to the server. One example is persisting log records into an internal log storage and enqueuing them for further forwarding to the server.|
|CallbackExecutor|Handles events from the server and callbacks from listeners: <br />`AttachEndpointToUserCallback`<br />`ConfigurationListener`<br />`DetachEndpointFromUserCallback`<br />`FindEventListenersCallback`<br />`LogDeliveryListener`<br />`NotificationListener`<br />`NotificationTopicListListener`<br />`OnAttachEndpointOperationCallback`<br />`OnDetachEndpointOperationCallback`<br />`UserAttachCallback`|
|ScheduledExecutor|Handles log forwarding to the server, runs failover and bootstrap tasks.|

### Available ExecutorService implementations

**SimpleExecutorContext** --- this implementation provides one `java.util.concurrent.ScheduledExecutorService` for `ScheduledExecutor` and three separate instances of `java.util.concurrent.ExecutorService` for `LifeCycleExecutor`, `ApiExecutor`, and `CallbackExecutor`.
The `ExecutorService` instances contain a fixed number of threads in the pool.
`SimpleExecutorContext` with one thread for each  `ExecutorService` is a default `ExecutorContext` implementation used by `KaaClient`.
To configure `SimpleExecutorContext`, use the following instruction.

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

**SingleThreadExecutorContext** --- this implementation shares single `java.util.concurrent.ScheduledExecutorService` with one thread in the pool for `LifeCycleExecutor`, `ApiExecutor`, `CallbackExecutor`, and `ScheduledExecutor`.

>**NOTE:** Avoid running any blocking or long running tasks within listeners passed to `KaaClient`.
>Make sure that `ExecutorContext` is properly configured.
{:.note}
