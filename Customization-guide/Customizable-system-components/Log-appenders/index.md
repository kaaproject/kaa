---
layout: page
title: Log appenders
permalink: /:path/Customization-guide/Customizable-system-components/Log-appenders
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

To implement a custom log appender, you need to complete the following steps:

1. Design and compile a configuration schema.
2. Implement the log appender based on
[AbstractLogAppender](https://github.com/kaaproject/kaa/blob/master/server/common/log-shared/src/main/java/org/kaaproject/kaa/server/common/log/shared/appender/AbstractLogAppender.java).
3. Develop the log appender descriptor.
4. Provision the log appender.

We recommend that you use one of the existing [log appender implementations](https://github.com/kaaproject/kaa/tree/master/server/appenders) as a reference.

# Example Maven Project structure

Your can create Maven project with the next structure:

```
custom-log-appender
|-- pom.xml
`-- src
    |-- main
    |   |`-- avro
    |   |    `-- CustomAppenderConfiguration.avsc
    |   `-- java
    |       `-- org
    |           `-- domain
    |               `-- sample
    |                   `-- appenders
    |                       ` -- custom
    |                           |-- appender
    |                           |    `-- CustomLogAppender.java
    |                           `-- config
    |                               |-- gen
    |                               |   `-- CustomAppenderConfiguration.java
    |                               `-- CustomAppenderDescriptor.java
```

Below is an example of Maven project POM file:

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.domain.sample.appenders</groupId>
    <artifactId>custom-appender</artifactId>
    <version>0.10.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Custom Log Appender</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <main.dir>${basedir}</main.dir>
        <kaa.version>0.10.0</kaa.version>
        <avro.version>1.7.5</avro.version>
        <slf4j.version>1.7.7</slf4j.version>
        <logback.version>1.1.2</logback.version>
        <maven-jar-plugin.version>2.4</maven-jar-plugin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.kaaproject.kaa.server.common</groupId>
            <artifactId>log-shared</artifactId>
            <version>${kaa.version}</version>
        </dependency>
        <dependency>
            <groupId>org.kaaproject.kaa.server.common</groupId>
            <artifactId>utils</artifactId>
            <version>${kaa.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>log4j-over-slf4j</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>${logback.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>${avro.version}</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                        </goals>
                        <configuration>
                            <stringType>String</stringType>
                            <sourceDirectory>${basedir}/src/main/avro/</sourceDirectory>
                            <outputDirectory>${basedir}/src/main/java/</outputDirectory>
                            <fieldVisibility>PRIVATE</fieldVisibility>
                            <includes>
                                <include>*.avsc</include>
                            </includes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>${maven-jar-plugin.version}</version>
                <configuration>
                    <excludes>
                        <exclude>**/logback.xml</exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

# Configuration schema

A log appender configuration schema is an Avro compatible schema that defines configuration structure of the log appender. This schema may contain additional
properties related to the UI form visualization which are described
[here]({{root_url}}Administration-guide/Tenants-and-applications-management/#avro-schema-parameters).

```json
{
    "namespace":"org.domain.sample.appenders.custom.config.gen",
    "type":"record",
    "name":"CustomAppenderConfiguration",
    "fields":[
        {
            "name":"servers",
            "displayName":"Your server list",
            "minRowCount":1,
            "type":{
                "type":"array",
                "items":{
                    "namespace":"org.domain.sample.appenders",
                    "type":"record",
                    "name":"Server",
                    "fields":[
                        {
                            "name":"host",
                            "displayName":"Host",
                            "weight":0.75,
                            "default":"localhost",
                            "type":"string"
                        },
                        {
                            "name":"port",
                            "displayName":"Port",
                            "weight":0.25,
                            "default":80,
                            "type":"int"
                        }
                    ]
                }
            }
        },
        {
            "name":"StringParameter",
            "displayName":"String parameter name",
            "type":"string"
        },
        {
            "name":"IntegerParameter",
            "displayName":"Integer parameter name",
            "default":1,
            "optional":true,
            "type":[
                "int",
                "null"
            ]
        }
    ]
}
```

In case when you use Maven project like in example described above, this schema will be compiled automatically during the Maven build process. Otherwise if
you wish to compile it manually, you should perform the following command:

```bash
java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>
```

Please refer to [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema) for more information.

# Log appender implementation

All Kaa log appenders extend generic abstract class org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender<T>. The following
code example illustrates the implementation of a custom log appender:

```
package org.domain.sample.appenders.custom.appender;

import org.domain.sample.appenders.custom.config.gen.CustomAppenderConfiguration;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

/**
 *
 * Sample appender implementation that uses {@link CustomAppenderConfiguration} as configuration.
 *
 */
public class CustomLogAppender extends AbstractLogAppender<CustomAppenderConfiguration> {

    public CustomLogAppender(Class<CustomAppenderConfiguration> configurationClass) {
        super(configurationClass);
    }

    /**
     * Inits the appender from configuration.
     *
     * @param appender the metadata object that contains useful info like application token, tenant id, etc.
     * @param configuration the configuration object that you have specified during appender provisioning.
     */
    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CustomAppenderConfiguration configuration) {
        //Do some initialization here.
    }

    /**
     * Consumes and delivers logs.
     *
     * @param logEventPack container for log events with some metadata like log event schema.
     * @param recordHeader additional data about log event source (endpoint key hash, application token, header version, timestamp).
     * @param logDeliveryCallback report status of log delivery.
     */
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader recordHeader, LogDeliveryCallback logDeliveryCallback) {
        //Append logs to your system here.
    }

    /**
     * Closes this appender and releases any resources associated with it.
     *
     */
    @Override
    public void close() {
        //Free allocated resources here.
    }
}
```

# Log appender descriptor

A log appender descriptior provides Kaa with the information on how to locate and configure your custom log appender. To implement a
log appender descriptor, you need to implement the PluginConfig interface at first.

It is also important to provide your class with the ```@KaaPluginConfig``` annotation. This annotation is used by Kaa Admin UI service in order to scan
avaliable log appenders in the class path.

The following code example illustrates the implementation of a log appender descriptor:

```
package org.domain.sample.appenders.custom.config;

import org.apache.avro.Schema;
import org.domain.sample.appenders.custom.config.gen.CustomAppenderConfiguration;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;

/**
 *
 * Sample descriptor for {@link org.domain.sample.appenders.custom.appender.CustomLogAppender} appender.
 *
 */
@KaaPluginConfig(pluginType = PluginType.LOG_APPENDER)
public class CustomAppenderDescriptor implements PluginConfig {

    public CustomAppenderDescriptor() {
    }

    /**
     * Name of the appender will be used in Admin UI
     */
    @Override
    public String getPluginTypeName() {
        return "Custom appender";
    }

    /**
     * Returns name of the appender class.
     */
    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.sample.appender.CustomLogAppender";
    }

    /**
     * Returns avro schema of the appender configuration.
     */
    @Override
    public Schema getPluginConfigSchema() {
        return CustomAppenderConfiguration.getClassSchema();
    }
}
```

# Log appender provisioning

To provision your log appender, do the following:

1. Build your log appender using next command: <br/>
```$ mvn clean install```
2. Place the log appender ```*.jar``` from ```/target``` folder into the ```/usr/lib/kaa-node/lib``` folder.
3. If you using different package than ```org.kaaproject.kaa.*``` you need to edit ```kaa-node.properties``` file in ```/usr/lib/kaa-node/conf``` folder.
Specify additional package to scan kaa plugins configuration in parameter ```additional_plugins_scan_package```. For example provided in this article:
```additional_plugins_scan_package=org.domain.sample.appenders.custom```.
4. Restart kaa-node service: <br/>
```$ sudo service kaa-node restart```
5. Use Admin UI or REST API to create/update/delete your log appender instances as described in one of the
[Default log appenders]({{root_url}}Programming-guide/Key-platform-features/Data-collection#default-log-appenders).
