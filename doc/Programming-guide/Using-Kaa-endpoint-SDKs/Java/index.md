---
layout: page
title: Java
permalink: /:path/
sort_idx: 40

---
**Table of Contents**

- [Minimum Requirements](#minimum-requirements)
- [Using endpoint SDK in your appication](#using-endpoint-sdk-in-your-appication)

# What you’ll need

- A favorite text editor or IDE
- JDK 1.7 or later

# Using endpoint SDK in your appication

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

Also you need add following dependencies to enable logging:


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

First, you have to create <code>KaaClient</code>. SDK provide you class factory  <code>Kaa</code> that responsible for creating new instance of client.

So, hear example code how to do this :

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

Static method <code>newClient()</code> received two mandatory arguments &ndash; implementation of platform context and state listener.

First argument is <code>DesktopKaaPlatformContext</code> for desktop or <code>AndroidKaaPlatformContext</code> for mobile applications.

Second argument is out of the box implementation of <code>KaaClientStateListener</code> &ndash; <code>SimpleKaaClientStateListener</code> that just logged current state of Kaa client. 
After getting the client, call method <code>start()</code> to begin communication with server. Now, you can use features provided by Kaa platform like data collection, notifications and etc.
In the end, when you no longer need client, call <code>stop()</code> to close communication with server. 
 
## State of client
In order to connect to server Kaa client generate public and private key and save them in appropriate files &ndash; _key.private_ and _key.public_.
Also client create file _state.properties_ that contains important information about communication with server.

***
**Note**
    As default all those files are created in folder where application running, but you can specify folder explicitly using <code>KaaClientProperties</code>.
    Just set path to your folder using <code>setWorkingDirectory()</code> and then pass client properties object as argument to <code>DesktopKaaPlatformContext</code> constructor.
    
***


## Comparing platforms
The main difference between android and desktop client lies in KaaPlatformContext implementation. Only this entity distinguishes these platforms.
Below the table describing two implementation of this interface:

|Method/Platform|Desktop|Android| Description |
|---|---|---|
|createHttpClient| the same | the same  | HttpClient used to make response to server in HTTP KaaDataChannel|
|createPersistentStorage|  Use file storage | Use android internal storage | For persisting state of client|
|getBase64|  Use Apache Base64 |  Use Android Base64 | Need to encode/decod some date, i. e. endpoint key hash|
|createConnectivityChecker|  the same  |  the same | Just check connection to network|
|getExecutorContext| the same  | the same  | Responsible for creation of <code>ExecutorService</code> instances for SDK internal usage |
|getProperties| the same  | the same  | Return KaaClientProperties that holds important information of client SDK | 
|needToCheckClientState| Return true  | Return false | Off/on checking of feasibility of the transition between lifecycle states |

## Architecture overview 

### Lifecycle of client
Diagram below describe lifecycle of Kaa client &ndash; allowed transitions between states.

<center>
<img src="img/lifecycle.png"/>
</center>

### High level overview of Kaa client structure
All calls of Kaa client APIs go through several layers those can be presented by the next scheme:

<br>
 <center>
 <img src="img/layers.png"/>
 </center>
<br>

When we call some method on client for example <code>start()</code> Kaa client delegate responsibility for processing call to appropriate _manager_,
in our case &ndash; <code>BootstrapManager</code>. The manager calls corresponding method of _BootstrapTransport_ in turn. Next, the transport address to 
<code>KaaChannelManager</code> that creates <code>SyncTask</code> and put it in queue from which <code>SyncWorker</code> will take **task** and call <code>sync()</code>
on <code>KaaDataChannel</code> (<code>DefaultBootstrapChannell</code>). And finally, channel makes request to server.

<br>
 <center>
 <img src="img/sequence.png"/>
 </center>
<br>

***
**Note**
  <code>SyncWorker</code> &ndash; class that extends <code>Thread</code> and responsible for serving ongoing tasks from client to channels. 
  ChannelManager creates for each channel new instance of this class.

***

