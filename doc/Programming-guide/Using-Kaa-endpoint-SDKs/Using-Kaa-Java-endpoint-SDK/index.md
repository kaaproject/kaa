---
layout: page
title: Java
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs/Using-Kaa-Java-endpoint-SDK
sort_idx: 40

---
**Table of Contents**

- [Minimum Requirements](#minimum-requirements)
- [Using endpoint SDK in your appication](#using-endpoint-sdk-in-your-appication)

# What you’ll need

- Аt least 80 Mb RAM
- A favorite text editor or IDE
- JDK 1.7 or later

# Using endpoint SDK in your appication

## Preparing environment 

You need to [generate Endpoint SDK](Getting-started#generate-sdk) for the target platform - Java. Endpoint SDK is a jar file that developer should add to classpath using build tools.

For example you can create _lib_ folder and put there created jar file:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#maven-sdk">Maven</a></li>
  <li><a data-toggle="tab" href="#gradel-sdk">Gradel</a></li>
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
<div id="gradel-sdk" class="tab-pane fade" markdown="1">
```groovy
dependencies {
   compile  files('./lib/kaa-java-ep-sdk-LVcpSA4q5BiErcm10Zg86TSGL9s.jar')
}
```
</div>
</div>

Also you need add following dependencies for logging:


<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Maven">Maven</a></li>
  <li><a data-toggle="tab" href="#Gradel">Gradel</a></li>
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
<div id="Gradel" class="tab-pane fade" markdown="1">

```groovy
compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.7'

compile group: 'ch.qos.logback', name: 'logback-core', version: '1.1.2'

compile group: 'ch.qos.logback', name: 'logback-classic', version: '1.1.2'
```
</div>
</div>

After configuring all this stuff you can start to write your client.

## Base API overview

First, you have to create **KaaClient**. SDK provide you class factory **Kaa** that responsible for creating new instance of client.

So, hear example code how to do this :
 
```java
KaaClient client = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener());
```

Static method <code>newClient()</code> received two mandatory arguments &mdash; implementation of platform context and state listener.

In this example we have <code>DesktopKaaPlatformContext</code> but there is another implementation of <code>KaaPlatformContext</code> exists &mdash; <code>AndroidKaaPlatformContext</code> for mobile applications.

Second argument is out of the box implementation of <code>KaaClientStateListener</code> &mdash; <code>SimpleKaaClientStateListener</code> that just logged current state of Kaa client. 
After getting the client, call method <code>start()</code> to begin communication with server. Now, you can use features provided by Kaa platform like data collection, notifications and etc.
In the end, when you no longer need client, call <code>stop()</code> to close communication with server. 
 
## State of client
In order to connect to server Kaa client generate public and private key and save them in appropriate files - _key.private_ and _key.public_.
Also client create file _state.properties_ that contains important information about communication with server.

***
**Note**
    As default all those files are created in folder where application running, but you can specify folder explicitly using <code>KaaClientProperties</code>.
    Just set path to your folder using <code>setWorkingDirectory()</code> and then pass client properties object as argument to <code>DesktopKaaPlatformContext</code> constructor.
    
***