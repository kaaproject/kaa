---
layout: page
title: Java
permalink: /:path/
sort_idx: 40
---

**Table of Contents**

* TOC
{:toc}

# Prerequisites 

- Your favorite text editor or IDE
- JDK 1.7 or later

# Using endpoint SDK in your application

This guide describes about how to configure environment to start using Kaa SDK in your application, base API overview of java client and comparison desktop and android SDKs. 
Java client SDK should be preferred if you want to run your application on different platforms or you are going to develop android application.

## Preparing environment 

You need to [generate Endpoint SDK](Getting-started#generate-sdk) for the target platform - Java. Endpoint SDK is a jar file that developer should add to classpath using build tools.

For example you can create _lib_ folder and put there created jar file:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#maven-sdk">Maven</a></li>
  <li><a data-toggle="tab" href="#gradle-sdk">Gradle</a></li>
</ul>

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

Also you need add the following dependencies to enable logging:

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Maven">Maven</a></li>
  <li><a data-toggle="tab" href="#Gradle">Gradle</a></li>
</ul>

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

After configuring all this stuff you can start to write your client.


## Base API overview

First, you have to create an instance of `KaaClient`. SDK provides you with class factory  `Kaa` which is responsible for creating new instance of client.

So, here is an example code showing how it should be done:

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

Static method `newClient()` receives two mandatory arguments -- platform specific context and 
implementation of the client state listener interface.

The first argument can be either `DesktopKaaPlatformContext` for java desktop or `AndroidKaaPlatformContext` for android applications.

In this example the second argument is default implementation of `KaaClientStateListener` -- `SimpleKaaClientStateListener` which performs only logging of client state changes. 
When the new instance of the client is created the method `start()` should be invoked in order to start client operation and communication with server. Starting from this point you can use 
features API provided by Kaa platform such as data collection, notifications and etc. In the end, when the client is no longer needed, call `stop()` in order to release resources and stop 
communication with the server.
 
## State of client
When the client is started for the first time it generates private/public key pair and saves them in appropriate files -- _key.private_ and _key.public_.
These keys are used afterwards to maintain secure communication with the server.
Also client creates _state.properties_ file used for persistence of the parameters which reflect client state during operation with the server.

>**NOTE:** In case of Java desktop application by default all these files are created in the working directory, but you are able to specify different folder using `KaaClientProperties`. 
Set path to the new folder using `setWorkingDirectory()` method and then pass client properties instance as argument of `DesktopKaaPlatformContext` constructor.
    

## Comparing platforms
The main difference between android and desktop client is reflected in KaaPlatformContext implementation. Only this entity distinguishes these endpoint SDKs.
The comparison table showing key differences between two implementations of this interface is presented below:

|Method/Platform|Desktop|Android| Description |
|---|---|---|
|createHttpClient| the same | the same  | HttpClient is used to make response to server in HTTP KaaDataChannel|
|createPersistentStorage|  Use file storage | Use android internal storage | For persisting state of client|
|getBase64|  Use Apache Base64 |  Use Android Base64 | Need to encode/decode some date, i. e. endpoint key hash|
|createConnectivityChecker|  the same  |  the same | Just check connection to network|
|getExecutorContext| the same  | the same  | Responsible for creation of `ExecutorService` instances for SDK internal usage |
|getProperties| the same  | the same  | Return KaaClientProperties that holds important information of client SDK | 
|needToCheckClientState| Return true  | Return false | Off/on checking of feasibility of the transition between lifecycle states |




