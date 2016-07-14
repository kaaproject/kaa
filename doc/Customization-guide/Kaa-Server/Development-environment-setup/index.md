---
layout: page
title: Development environment setup
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

- [Creating custom log appender](#creating-custom-log-appender)
  - [Configuration schema](#configuration-schema)
  - [Log appender implementation](#log-appender-implementation)
  - [Log appender descriptor](#log-appender-descriptor)
  - [Log appender provisioning](#log-appender-provisioning)
- [Creating custom transport](#creating-custom-transport)
  - [Introduction](#introduction)
  - [Transport configuration schema](#transport-configuration-schema)
  - [Transport descriptor implementation](#transport-descriptor-implementation)
  - [Transport implementation](#transport-implementation)
  - [Transport provisioning](#transport-provisioning)
- [Creating custom transport channel](#creating-custom-transport-channel)
  - [Implement the KaaDataChannel interface (Java client)](#implement-the-kaadatachannel-interface-java-client)
    - [Register your own channel using the Kaa Channel Manager](#register-your-own-channel-using-the-kaa-channel-manager)
    - [Step 1 - Get an instance of KaaDataMultiplexer ](#step-1---get-an-instance-ofkaadatamultiplexer)
    - [Step 2 - Prepare a request collecting information from the services](#step-2---prepare-a-requestcollecting-information-from-the-services)
    - [Step 3 - Send the prepared request to the server and receive a response](#step-3---send-the-prepared-request-to-the-server-and-receive-a-response)
    - [Step 4 - Get an instance of KaaDataDemultiplexer ](#step-4---get-an-instance-ofkaadatademultiplexer)
    - [Step 5 - Push a response to the Kaa SDK](#step-5---push-a-response-to-the-kaa-sdk)
- [Creating custom user verifier](#creating-custom-user-verifier)
  - [Configuration schema](#configuration-schema-1)
  - [User verifier implementation](#user-verifier-implementation)
  - [User verifier descriptor](#user-verifier-descriptor)
  - [User verifier provisioning](#user-verifier-provisioning)

This guide explains how to create your own versions of such Kaa entities as transports, log appenders, and user verifiers. Kaa supports various kinds of customization to provide you with maximum flexibility and freedom of choice for your particular project needs.

In order for the guide to be useful, make sure that you have a development environment set up, including a Kaa server instance. We recommend that you use Kaa Sandbox with this guide, but you can use a Kaa cluster instead, if you like.

# Creating custom log appender

To implement a custom log appender, you need to complete the following steps.

1.  Design and compile a configuration schema.
2.  Implement the log appender based on AbstractLogAppender.
3.  Develop the log appender descriptor.
4.  Provision the log appender.

We recommend that you use one of the existing [log appender implementations](https://github.com/kaaproject/kaa/tree/v0.9.0/server/appenders) as a reference.

## Configuration schema

A log appender configuration schema is an Avro compatible schema that defines configuration parameters of the log appender. The following parameters in the schema affect Kaa Admin UI layout.

*   minRowCount - specifies a minimum number of rows in a UI table (only for arrays in the schema)
*   displayName - displays the name of the field on UI
*   displayNames - displays the name of each enumeration symbol on UI (only for enumeration fields in the schema)
*   default - displays the default value of the field on UI
*   optional - defines whether the field on UI is optional or mandatory
*   weight - defines a relative width of the corresponding column on UI (only for arrays in the schema)

```json
    {
        "namespace":"org.kaaproject.kaa.schema.sample",
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
                        "namespace":"com.company.kaa.appender",
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


Once you have prepared your schema, you can compile it using the following command.  

```
java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>
```

Please refer to [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema) for more information. It is also possible to integrate the schema compilation with [avro-maven-plugin](http://avro.apache.org/docs/current/gettingstartedjava.html).

## Log appender implementation

All Kaa log appenders extend generic abstract class org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender<T>. The following code example illustrates the implementation of a custom log appender.

```Java
package org.kaaproject.kaa.sample.appender;

import org.kaaproject.kaa.schema.sample.CustomAppenderConfiguration;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
/**
 *
 * Sample appender implementation that uses {@link CustomAppenderConfiguration} as configuration.
 *
 */
public class CustomLogAppender extends AbstractLogAppender<CustomAppenderConfiguration> {

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
     * @param header additional data about log event source (endpoint key hash, application token, header version, timestamp).
     */
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
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

## Log appender descriptor

A log appender descriptior provides Kaa with the information on how to locate and configure your custom log appender. To implement a log appender descriptor, you need to implement the AppenderConfig interface at first.

It is also important to provide your class with the @KaaPluginConfig annotation. This annotation helps Kaa Admin UI to find all available log appenders in the class path.

**NOTE**  
A log appender descriptor is optional if you are going to configure your log appenders using only REST API.

The following code example illustrates the implementation of a log appender descriptor.

```Java
package org.kaaproject.kaa.sample.appender.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.schema.sample.CustomAppenderConfiguration;
import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;

/**
 *
 * Sample descriptor for org.kaaproject.kaa.sample.appender.CustomLogAppender appender.
 *
 */
@KaaPluginConfig(pluginType = PluginType.LOG_APPENDER)
public class CustomAppenderDescriptor implements AppenderConfig {


    public CustomAppenderDescriptor() {
    }
    /**
     * Name of the appender will be used in Admin UI
     */
    @Override
    public String getName() {
        return "Custom appender";
    }
    /**
     * Returns name of the appender class.
     */
    @Override
    public String getLogAppenderClass() {
        return "org.kaaproject.kaa.sample.appender.CustomLogAppender";
    }
    /**
     * Returns avro schema of the appender configuration.
     */
    @Override
    public Schema getConfigSchema() {
        return CustomAppenderConfiguration.getClassSchema();
    }
}
```

## Log appender provisioning

To provision your log appender, do the following:

1.  Build your log appender using next command:

```
    $ mvn clean install
```

2.  Place the log appender \*.jar from /target folder into the /usr/lib/kaa-node/lib folder
3.  If you using different package than org.kaaproject.kaa.\* you need to edit kaa-node.properties file in /usr/lib/kaa-node/conf folder. Specify additional package to scan kaa plugins configuration in parameter additional_plugins_scan_package.
4.  Restart kaa-node service:

```
    $ sudo service kaa-node restart
```  

5.  Use [Admin UI999](999) or [REST API99](999) to create/update/delete your log appender instances.

# Creating custom transport

## Introduction

To implement a custom transport, you need to complete the following steps.

1.  Design and compile a configuration schema.
2.  Implement the [TransportConfig](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/TransportConfig.java) interface.
3.  Implement [Transport](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/Transport.java).
4.  Provision the transport in a Bootstrap server and/or an Operations server.

We recommend that you use one of the existing [transport implementations](https://github.com/kaaproject/kaa/tree/v0.9.0/server/transports) as a reference and also review the [transports design reference999](999).

Please note that once a new transport is implemented, you will most likely need to [implement a corresponding transport channel](#creating-custom-transport-channel) for one or multiple endpoint SDK platforms.

## Transport configuration schema

A _transport configuration schema_ is an Avro compatible schema that defines configuration parameters for the transport. The following parameters in the schema affect Kaa Admin UI layout.

*   minRowCount - specifies a minimum number of rows in a UI table (If you are using arrays in your schema, you can specify a minimum number of elements in an array with this parameter)
*   displayName - displays the name of the field on UI
*   displayNames - displays the name of each enumeration symbol on UI (only for enumeration fields in the schema)
*   default - displays the default value of the field on UI
*   optional - defines whether the field on UI is optional or mandatory
*   weight - defines a relative width of the corresponding column on UI (only for arrays in the schema)

The following example illustrates a simple transport configuration schema with two fields (host and port).

```json
{
    "namespace":"org.kaaproject.kaa.server.transport.custom.config",
    "type":"record",
    "name":"CustomConfig",
    "fields":[
        {
            "name":"host",
            "type":"string"
        },
        {
            "name":"port",
            "type":"int"
        }
    ]
}
```

Once you have prepared your schema, you can compile it using the following command.  
`java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>`  
For more information, refer to [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema). It is also possible to integrate the schema compilation with [avro-maven-plugin](http://avro.apache.org/docs/current/gettingstartedjava.html).

## Transport descriptor implementation

Once you have defined and compiled a transport configuration schema, you can proceed to the implementation of a transport descriptor.

A transport descriptor should implement the [TransportConfig](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/TransportConfig.java) interface and be annotated with the [KaaTransportConfig](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/KaaTransportConfig.java) annotation for the provisioning purposes.

**Note:** all transport descriptors should be inside the `org.kaaproject.kaa.server.transport` package or its subpackages.

The following example illustrates a transport descriptor implementation based on the schema defined in the previous section.

```Java
package org.kaaproject.kaa.server.transport.custom.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.Transport;
import org.kaaproject.kaa.server.transport.TransportConfig;
import org.kaaproject.kaa.server.transport.TransportService;

/**
 * Configuration for the custom transport
 */
@KaaTransportConfig
public class CustomTransportConfig implements TransportConfig {

    private static final String CUSTOM_TRANSPORT_NAME = "org.kaaproject.kaa.server.transport.tcp";
    private static final int CUSTOM_TRANSPORT_ID = ServerNameUtil.crc32(CUSTOM_TRANSPORT_NAME);
    private static final String CUSTOM_TRANSPORT_CLASS = "org.kaaproject.kaa.server.transports.tcp.transport.TcpTransport";
    private static final String CUSTOM_TRANSPORT_CONFIG = "custom-transport.config";

    public CustomTransportConfig() {
        super();
    }

    /**
     * Returns the transport id. The transport id must be unique.
     *
     * @return the transport id
     */
    @Override
    public int getId() {
        return CUSTOM_TRANSPORT_ID;
    }

    /**
     * Returns the transport name. There is no strict rule for this
     * name to be unique.
     *
     * @return the transport name
     */
    @Override
    public String getName() {
        return CUSTOM_TRANSPORT_NAME;
    }

    /**
     * Returns the class name of the {@link Transport} implementation.
     *
     * @return the class name of the {@link Transport} implementation
     */
    @Override
    public String getTransportClass() {
        return CUSTOM_TRANSPORT_CLASS;
    }

    /**
     * Returns the avro schema of the {@link Transport} configuration.
     *
     * @return the avro schema of the {@link Transport} configuration
     */
    @Override
    public Schema getConfigSchema() {
        return CustomConfig.getClassSchema();
    }

    /**
     * Returns the configuration file name. This configuration file may
     * be used by {@link TransportService} to initialize and configure
     * the corresponding {@link Transport}.
     *
     * @return the configuration file name
     */
    @Override
    public String getConfigFileName() {
        return CUSTOM_TRANSPORT_CONFIG;
    }
}
```

## Transport implementation

All transport implementations should implement the [Transport](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/Transport.java) interface. We recommend extending [AbstractKaaTransport](https://github.com/kaaproject/kaa/tree/v0.9.0/server/common/transport-shared/src/main/java/org/kaaproject/kaa/server/transport/AbstractKaaTransport.java) for convenience.

The following example illustrates a transport implementation based on the defined transport configuration schema.

```Java
package org.kaaproject.kaa.server.transports.tcp.transport;
import java.nio.ByteBuffer;
import org.kaaproject.kaa.server.transport.AbstractKaaTransport;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.SpecificTransportContext;
import org.kaaproject.kaa.server.transport.Transport;
import org.kaaproject.kaa.server.transport.TransportLifecycleException;
import org.kaaproject.kaa.server.transport.custom.config.CustomConfig;

public class CustomTransport extends AbstractKaaTransport<CustomConfig>{

    /**
     * Initialize a transport instance with a particular configuration and
     * common transport properties that are accessible via the context. The configuration is an Avro
     * object. The serializaion/deserialization is done using the schema specified in
     * {@link KaaTransportConfig}.
     *
     * @param context
     *            the transport initialization context
     * @throws TransportLifecycleException
     */
    @Override
    protected void init(SpecificTransportContext<CustomConfig> context) throws TransportLifecycleException {
        // TODO Auto-generated method stub
    }

    /**
     * Retrieves the serialized connection data. This data will be used in an
     * endpoint sdk to set up a connection to this transport instance.
     * Used to provide implementation of {@link Transport#getConnectionInfo()}.
     *
     * @return the serialized connection data
     */
    @Override
    protected ByteBuffer getSerializedConnectionInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Starts a transport instance. This method should block its caller thread
     * until the transport is started. This method should not block its caller
     * thread after the startup sequence is successfully completed.
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    /**
     * Stops the transport instance. This method should block its current thread
     * until the transport is stopped. The transport may be started again after it is
     * stopped.
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    /**
     * Returns a min version of the transport protocol that is supported by this transport.
     * Useful when a single transport instance needs to support multiple versions of the client protocol implementations.
     */
    @Override
    protected int getMinSupportedVersion() {
        // TODO Auto-generated method stub
        return 1;
    }

    /**
     * Returns a max version of the transport protocol that is supported by this transport.
     * Useful when a single transport instance needs to support multiple versions of the client protocol implementations.
     */
    @Override
    protected int getMaxSupportedVersion() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public Class<CustomConfig> getConfigurationClass() {
        // TODO Auto-generated method stub
        return CustomConfig.class;
    }
}
```

## Transport provisioning

To provision the implemented transport, you need to put all the transport related classes into the classpath of the server. Also, you need to make sure that the transport configuration file is present in the classpath and its schema and name match appropriate parameters from your transport descriptor.

The following example illustrates the configuration file contents that match our transport. This file should be named `custom-transport.config.`

```json
{
  "host":"${transport.bindInterface}",
  "port":9997
}
```

**Note:** you can use transport configuration variables from the `operations-server.properties` or `bootstrap-server.properties` files; these variables must have the `transport` prefix.

# Creating custom transport channel

To create your own channel using the Kaa SDK you have to implement the KaaDataChannel interface and register the implementation using the Kaa Channel Manager

# Implement the KaaDataChannel interface (Java client)

The implementation of the KaaDataChannel interface will contain methods that you will use to transfer data between endpoints and Servers using the protocol of your choice.

For a definition of the KaaDataChannel interface please refer to the [javadoc](http://kaaproject.github.io/kaa/doc/client-java-core/latest/org/kaaproject/kaa/client/channel/KaaDataChannel.html).

You can find examples of the interface implementation in the following java classes for the default transport channels:

1.  DefaultOperationsChannel  - implementation of the Operation HTTP Long poll channel;
2.  DefaultOperationHttpChannel - implementation of the Operation HTTP channel;
3.  DefaultBootstrapChannel - implementation of the Bootstrap HTTP channel;
4.  DefaultOperationTcpChannel - implementation of the Operation [Kaatcp999](http://docs.kaaproject.org/display/KAA09x/KaaTCP+channel) channel.  

## Register your own channel using the Kaa Channel Manager

When the implementation of your channel is ready you should add the channel to Channel Manager by invoking the addChannel() method as follows:

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp">Cpp</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1" >

```Java
KaaClient kaaClient = ...;
KaaDataChannel myChannel;
kaaClient.getChannelManager().addChannel(myChannel);
```

</div><div id="Cpp" class="tab-pane fade" markdown="1" >

```Cpp
IDataChannelPtr myChannel;
Kaa::getKaaClient().getChannelManager().addChannel(myChannel);
```

</div><div id="C" class="tab-pane fade" markdown="1" >

```C
kaa_context_t kaa_context;
/* Assume Kaa SDK is already initialized */
kaa_error_t error_code = kaa_channel_manager_add_transport_channel(kaa_context.channel_manager
                                                                 , channel
                                                                 , NULL);
/* Check error code */
```

</div><div id="Objective-C" class="tab-pane fade" markdown="1" >

```Objective-C
id<KaaClient> kaaClient = ...;
id<KaaDataChannel> myChannel;
[[kaaClient getChannelManager] addChannel:myChannel];
```

</div></div>

Now Kaa SDK knows about your channel and will use it to send service data according to the channel settings.</div>

Notice that in java sdk, after adding new data channel multiplexer and demultiplexer will be setted regarding to channel server type.

To send a request to the server and get a response please follow the steps described below.

## Step 1 - Get an instance of KaaDataMultiplexer

To prepare a request to the server, you have to use a data multiplexer that combines and serializes requests from different Kaa services.

Kaa provides two data multiplexers. One should be used for communication with the Operations server and the other for communication with the Bootstrap server.

To get an instance of [KaaDataMultiplexer](http://kaaproject.github.io/kaa/doc/client-java-core/latest/org/kaaproject/kaa/client/channel/KaaDataMultiplexer.html) for communication with the Operation server, use the getBootstrapMultiplexer() method:

```Cpp
Kaa::getKaaClient().getOperationMultiplexer();
```

To get an instance of KaaDataMultiplexer for communication with the Bootstrap server, use the getBootstrapMultiplexer() method:

```Cpp
Kaa::getKaaClient().getBootstrapMultiplexer();
```

## Step 2 - Prepare a request collecting information from the services

In order to create a request to be sent to the server you have to collect data from Kaa services. Collecting data is performed using the KaaDataMultiplexer interface obtained in the previous step.

The KaaDataMultiplexer interface has only one method: compileRequest ()

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-1">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp-1">Cpp</a></li>
  <li><a data-toggle="tab" href="#Objective-C-1">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-1" class="tab-pane fade in active" markdown="1" >

```Java
byte [] compileRequest(Map<TransportType, ChannelDirection> types) throws Exception;
```

</div><div id="Cpp-1" class="tab-pane fade" markdown="1" >

```Cpp
virtual std::vector<boost::uint8_t> compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes) = 0;
```

</div><div id="Objective-C-1" class="tab-pane fade" markdown="1" >

```Objective-C
- (NSData *)compileRequestForTypes:(NSDictionary *)types;
```

</div></div>

where **types** is a map of Kaa services and their data exchange directions that are supported by your channel. 

For example, if you have implemented a channel with the following settings:

*   The channel is able to send events, but cannot receive events
*   The channel is able to receive notifications, but cannot send notification requests
*   The channel supports the Configuration service in both directions.

then your **types** map will look as follows:

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-2">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp-2">Cpp</a></li>
  <li><a data-toggle="tab" href="#Objective-C-2">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-2" class="tab-pane fade in active" markdown="1" >

```Java
Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
types.put(TransportType.NOTIFICATION, ChannelDirection.DOWN);
types.put(TransportType.EVENT, ChannelDirection.UP);
```

</div><div id="Cpp-2" class="tab-pane fade" markdown="1" >

```Cpp
std::map<TransportType, ChannelDirection> types =
{
    { TransportType::CONFIGURATION, ChannelDirection::BIDIRECTIONAL },
    { TransportType::NOTIFICATION, ChannelDirection::DOWN },
    { TransportType::EVENT, ChannelDirection::UP }
};
```

</div><div id="Objective-C-2" class="tab-pane fade" markdown="1" >

```Objective-C
NSDictionary *types = @{
    @(TRANSPORT_TYPE_CONFIGURATION) : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
    @(TRANSPORT_TYPE_NOTIFICATION)  : @(CHANNEL_DIRECTION_DOWN),
    @(TRANSPORT_TYPE_EVENT)         : @(CHANNEL_DIRECTION_UP)
};
```

</div></div>

The method scans the services and collects data from those that have prepared data to be sent to the server. The method uses the **types** map to filter requests from the services. (For example, if for a Transport Type the "DOWN" direction is indicated in the type map, the request data from the respective service will be filtered out and will not be sent to the server.)

The data collected from the services is combined into the SyncRequest and serialized. As a result, the method returns a byte array with serialized data.

## Step 3 - Send the prepared request to the server and receive a response

Insert the data returned  by compileRequest into your transfer protocol and send it to the server. This step is performed using the methods that are in the implemented KaaDataChannel interface.

The response  is received as a byte array, and it contains serialized responses for all the services from which requests were sent.

## Step 4 - Get an instance of KaaDataDemultiplexer

To deserialize the received response and provide a response to each service, you have to use a data demultiplexer.

Kaa provides two data demultiplexers. One should be used for communication with the Operations server and the other for communication with the Bootstrap server.

To get an instance of [KaaDataDemultiplexer](http://kaaproject.github.io/kaa/doc/client-java-core/latest/org/kaaproject/kaa/client/channel/KaaDataDemultiplexer.html) for communication with the Operation server, use the getOperationDemultiplexer() method:

```Cpp
Kaa::getKaaClient().getOperationDemultiplexer();
```

To get an instance of KaaDataDemultiplexer for communication with the Bootstrap server, use the getBootstrapDemultiplexer() method:

```Cpp
Kaa::getKaaClient().getBootstrapDemultiplexer();
```

## Step 5 - Push a response to the Kaa SDK

The data demultiplexer contains only one method

[void processResponse(byte [] response)](http://kaaproject.github.io/kaa/doc/client-java-core/latest/org/kaaproject/kaa/client/channel/KaaDataDemultiplexer.html)

The method deserializes the response and decodes the raw data into SyncResponse which consists of subresponses for all services. Then the subresponses are delivered to each service for subsequent processing.

# Creating custom user verifier

To implement a custom user verifier, you need to complete the following steps.

1.  Design and compile a configuration schema.
2.  Implement the user verifier based on AbstractKaaUserVerifier.
3.  Develop the user verifier descriptor.
4.  Provision the user verifier.

We recommend that you use one of the [existing user verifier implementations](https://github.com/kaaproject/kaa/tree/v0.9.0/server/verifiers) as a reference.

## Configuration schema

A user verifier configuration schema is an Avro compatible schema that defines configuration parameters of the user verifier. The following parameters in the schema affect Kaa Admin UI layout.

*   displayName - displays the name of the field on UI
*   by_default - displays the default value of the field on UI  

```json
    {
     "namespace": "org.kaaproject.kaa.schema.sample",
     "type": "record",
     "name": "CustomUserVerifierConfiguration",
     "fields": [
       {
            "name": "app_id",
            "displayName": "Application id",
            "type": "string"
       },
       {
            "name": "app_secret",
            "displayName": "Application secret",
            "type": "string"
       },
       {
           "name": "max_parallel_connections",
           "displayName": "Maximal number of allowed connections per verifier",
           "type": "int", "by_default": "5"
        }
     ]
    }
```

Once you have prepared your schema, you can compile it using the following command.  
`java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>`  
Please refer to [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema) for more information. It is also possible to integrate the schema compilation with [avro-maven-plugin](http://avro.apache.org/docs/current/gettingstartedjava.html).

## User verifier implementation

All Kaa user verifiers extend generic abstract class org.kaaproject.kaa.server.common.verifier.AbstractUserVerifier<T>. The following code example illustrates the implementation of a custom user verifier.

```Java
package org.kaaproject.kaa.sample.verifier;

import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.schema.sample.CustomUserVerifierConfiguration;

/**
 *
 * Sample user verifier implementation that uses {@link CustomUserVerifierConfiguration} as configuration.
 *
 */
public class CustomUserVerifier extends AbstractKaaUserVerifier<CustomUserVerifierConfiguration> {
    /**
    * Initialize a user verifier instance with a particular configuration and
    * common transport properties. The configuration is a serialized Avro
    * object. The serialization is done using the schema specified in
    * {@link KaaUserVerifierConfig}.
    *
    * @param context the user verifier initialization context
    * @param configuration the configuration object that you have specified during verifier provisioning.
    */
    @Override
    public void init(UserVerifierContext context, CustomUserVerifierConfiguration configuration) {

    }

    /**
    * Verifies the access token.
    *
    * @param userExternalId the user external id
    * @param userAccessToken the access token
    * @param callback User verification callback, which helps to identify verification status and
    * possible reason failure
    * @return true, if verified
    */
    @Override
    public void checkAccessToken(String userExternalId, String userAccessToken, UserVerifierCallback callback) {

    }

    /**
    * Starts a user verifier instance. This method should block its caller thread
    * until the user verifier is started. This method should not block its caller
    * thread after startup sequence is successfully completed.
    */
    @Override
    public void start() {

    }

    /**
    * Stops the user verifier instance. This method should block its current thread
    * until user verifier is stopped. User verifier may be started again after it is
    * stopped.
    */
    @Override
    public void stop() {

    }

    /**
    * Gets the configuration class.
    *
    * @return the configuration class
    */
    @Override
    public Class<CustomUserVerifierConfiguration> getConfigurationClass() {
        return CustomUserVerifierConfiguration.class;
    }
}
```


## User verifier descriptor

A user verifier descriptor provides Kaa with the information on how to locate and configure your custom user verifier. To implement a user verifier descriptor, you need to implement the PluginConfig interface at first.

It is also important to provide your class with the @KaaPluginConfig annotiation. This annotation helps Kaa Admin UI to find all available user verifiers in the class path.

**NOTE**
A user verifier descriptor is optional if you are going to configure your user verifiers using only REST API.

The following code example illustrates the implementation of a user verifier descriptor.

```Java
package org.kaaproject.kaa.sample.verifier.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.schema.sample.CustomUserVerifierConfiguration;

@KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
public class CustomUserVerifierConfig implements PluginConfig {

    /**
    * Returns the plugin display name. There is no strict rule for this
    * name to be unique.
    *
    * @return the plugin display name
    */
    @Override
    public String getPluginTypeName() {
        return TRUSTFUL_VERIFIER_NAME;
    }

    /**
    * Returns the class name of the plugin implementation.
    *
    * @return the class name of the plugin implementation
    */
    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.schema.sample.verifier.CustomUserVerifier";
    }

    /**
    * Returns the avro schema of the plugin configuration.
    *
    * @return the avro schema of the plugin configuration
    */
    @Override
    public Schema getPluginConfigSchema() {
        return CustomUserVerifierConfiguration.SCHEMA$;
    }
}
```

## User verifier provisioning

To provision your user verifier, do the following:

1.  Place the user verifier descriptor and configuration classes into the Admin UI class path.
2.  Place the user verifier implementation classes into the Operations Server class path.
3.  Use [Admin UI999](https://docs.kaaproject.org/display/KAA/Administration+UI+guide#AdministrationUIguide-Userverifiers) or [REST API999](https://docs.kaaproject.org/display/KAA/Admin+REST+API#AdminRESTAPI-Create/edituserverifier) to create/update/delete your user verifier instances.
