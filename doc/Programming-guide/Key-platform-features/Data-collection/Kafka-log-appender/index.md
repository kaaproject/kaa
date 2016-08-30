---
layout: page
title: Kafka log appender
permalink: /:path/
nav: /:path/Programming-guide/Key-platform-features/Data-collection/Kafka-log-appender
sort_idx: 60
---

{% include variables.md %}

* TOC
{:toc}

The Kafka log appender is responsible for transferring logs from the Operations service to the Apache Kafka service. The logs are stored in the specified topic.

# Creating Kafka log appender in Admin UI

The easiest way to create a Kafka log appender for your application is by using Admin UI.

To create a log appender of the Kafka storage type, do the following:

1. In the **Log appenders** window, click **Add log appender**.
2. Enter the log appender name and description, select the minimum and maximum supported schema version, select necessary log metadata fields.
3. Set the log appender type to _Kafka_.
4. Fill in the Kafka log appender configuration form.
5. Click **Add** button. Log appender is ready and operational at this point.

![Add log appender in Admin UI](attach/add-log-appender-in-admin-ui.png)


# Creating Kafka log appender with Admin REST API

It is also possible to create a Kafka log appender for your application by using [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Logging/editLogAppender).
The following example illustrates how to create the Kafka log appender for the Cell Monitor demo application available in Kaa Sandbox.

## Configuration

The Kafka log appender configuration must match to
[this](https://github.com/kaaproject/kaa/blob/master/server/appenders/kafka-appender/src/main/avro/kafka-appender-config.avsc) Avro schema.

## Fields description

|Name                   |Description                                                                                                            |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------|
|bufferMemorySize       |message buffer size in bytes                                                                                           |
|executorThreadPoolSize |number of threads that can simultaneously perform operation with your Kafka                                            |
|kafkaAcknowledgement   |the number of acknowledgments the producer requires the leader to have received before considering a request complete  |
|kafkaCompression       |type of built-in message compression types                                                                             |
|kafkaKeyType           |type of generated message key                                                                                          |
|kafkaServers           |list of kafka bootstrap servers (hostname and port pairs)                                                              |
|partitionCount         |amount of event partitions                                                                                             |
|retries                |failover property. Amount of connection retries on failed message delivery                                             |
|topic                  |logs destination topic                                                                                                 |
|useDefaultPartitioner  |if false, appender will calculate partition independently                                                              |

An example configuration that matches to previously introduced Avro schema is as below:

```json
{
    "kafkaServers":[
        {
            "host":"localhost",
            "port":9092
        }
    ],
    "topic":"kaa",
    "useDefaultPartitioner":{
        "boolean":true
    },
    "partitionCount":{
        "int":1
    },
    "kafkaKeyType":{
        "org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaKeyType":"NULL"
    },
    "executorThreadPoolSize":{
        "int":1
    },
    "bufferMemorySize":{
        "long":33554432
    },
    "kafkaCompression":{
        "org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaCompression":"NONE"
    },
    "kafkaAcknowledgement":{
        "org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaAcknowledgement":"ONE"
    },
    "retries":{
        "int":0
    }
}
```

## Administration

The following Admin REST API call example illustrates how to create a new Kafka log appender.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d @kafkaLogAppender.json "http://localhost:8080/kaaAdmin/rest/api/logAppender" | python -mjson.tool
```

where file ```kafkaLogAppender.json``` contains following data:

```
{
    "pluginClassName":"org.kaaproject.kaa.server.appenders.kafka.appender.KafkaLogAppender",
    "pluginTypeName":"Kafka",
    "applicationId":"5",
    "applicationToken":"82635305199158071549",
    "name":"Sample Kafka log appender",
    "description":"Sample Kafka log appender",
    "headerStructure":[
        "KEYHASH",
        "VERSION",
        "TIMESTAMP",
        "TOKEN",
        "LSVERSION"
    ],
    "maxLogSchemaVersion":2147483647,
    "minLogSchemaVersion":1,
    "tenantId":"1",
    "jsonConfiguration":"{\"kafkaServers\":[{\"host\":\"localhost\",\"port\":9092}],\"topic\":\"kaa\",\"useDefaultPartitioner\":true,\"partitionCount\":1,\"kafkaKeyType\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaKeyType\":\"NULL\"},\"executorThreadPoolSize\":1,\"bufferMemorySize\":33554432,\"kafkaCompression\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaCompression\":\"NONE\"},\"kafkaAcknowledgement\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaAcknowledgement\":\"ONE\"},\"retries\":0}"
}
```

Example result:

```json
{
    "applicationId": "5",
    "applicationToken": "82635305199158071549",
    "confirmDelivery": true,
    "createdTime": 1466497790454,
    "createdUsername": "devuser",
    "description": "Sample Kafka log appender",
    "headerStructure": [
        "KEYHASH",
        "VERSION",
        "TIMESTAMP",
        "TOKEN",
        "LSVERSION"
    ],
    "id": "131077",
    "jsonConfiguration": "{\"kafkaServers\":[{\"host\":\"localhost\",\"port\":9092}],\"topic\":\"kaa\",\"useDefaultPartitioner\":true,\"partitionCount\":1,\"kafkaKeyType\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaKeyType\":\"NULL\"},\"executorThreadPoolSize\":1,\"bufferMemorySize\":33554432,\"kafkaCompression\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaCompression\":\"NONE\"},\"kafkaAcknowledgement\":{\"org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaAcknowledgement\":\"ONE\"},\"retries\":0}",
    "maxLogSchemaVersion": 2147483647,
    "minLogSchemaVersion": 1,
    "name": "Sample Kafka log appender",
    "pluginClassName": "org.kaaproject.kaa.server.appenders.kafka.appender.KafkaLogAppender",
    "pluginTypeName": "Kafka",
    "tenantId": "1"
}
```

# Playing with Kafka log appender

To check out Kafka log appender you can play with [Data collection demo](https://github.com/kaaproject/sample-apps/tree/master/datacollectiondemo/source).
Download [Kaa Sandbox](http://www.kaaproject.org/download-kaa/) then set up it and go to Data collection demo application.

>**NOTE:**
> Kafka must be installed, running and reachable from Kaa to complete this example. For details about Kafka installation refer to
[official Apache documentation](https://kafka.apache.org/07/quickstart.html).
> Kaa uses Zookeeper service listening on port 2181. Before starting Kafka you need to change Zookeeper to use some other port (e.g. 2183).
> For this, you will need to edit properties files (they are located within the Kafka installation directory) like below: <br/>
> ```config/zookeeper.properties``` : set ```clientPort=2183 ```; <br/>
```config/server.properties``` : set ```zookeeper.connect=localhost:2183```; <br/>
```config/consumer.properties``` : set ```zookeeper.connect=127.0.0.1:2183```

We have next log schema:

```json
{
    "type":"record",
    "name":"LogData",
    "namespace":"org.kaaproject.kaa.schema.sample.logging",
    "fields":[
        {
            "name":"level",
            "type":{
                "type":"enum",
                "name":"Level",
                "symbols":[
                    "KAA_DEBUG",
                    "KAA_ERROR",
                    "KAA_FATAL",
                    "KAA_INFO",
                    "KAA_TRACE",
                    "KAA_WARN"
                ]
            }
        },
        {
            "name":"tag",
            "type":"string"
        },
        {
            "name":"message",
            "type":"string"
        },
        {
            "name":"timeStamp",
            "type":"long"
        }
    ]
}
```

The following JSON example matches the schema above.

```json
{
    "level":"KAA_INFO",
    "tag":"TEST_TAG",
    "message":"My simple message",
    "timeStamp":"1466075369795"
}
```

1. Go to the Data collection demos in Sandbox.
![Data collection demo in Sandbox](attach/data-collection-demo-in-sandbox.png)
2. Follow **Installation** instructions.
3. In the Admin UI follow to **Data collection demo** application.
![Data collection demo UI](attach/data-collection-demo-in-sandbox2.png)
4. Go to application's **Log appenders** configuration and add a new one.
![Add log appender](attach/data-collection-demo-in-sandbox3.png)
5. Enter name of the new appender (in this example it is "Kafka").
6. Select **Kafka** appender type.
![Appender Type](attach/appender-type.png)
7. Set up appender **Configuration** similar to screenshot.
![Appender configuration](attach/appender-configuration.png)
In this example, Kafka server installed in the Sandbox VM.
8. Click **Add** button on the top of the screen to create and deploy appender.
![Add button](attach/add-button.png)
9. Verify that newly created appender has appeared in list.
![Verify newly created log appender](attach/verify-log-appender.png)
10. From Kafka installation directory run the next command:

    ```bash
    bin/kafka-console-consumer.sh --zookeeper localhost:2183 --topic kaa
    ```

    This will bring up Kafka consumer, so we can see logs transferred from Kaa.
11. Use instructions from Sandbox to run Data collection demo application and verify that logs have been successfully sent to Kaa.
12. After this you should see something like below:

    ```
    2016-06-21 12:38:12,260 [main] INFO  o.k.k.d.d.DataCollectionDemo - Data collection demo started
    2016-06-21 12:38:13,337 [pool-2-thread-1] INFO  o.k.k.d.d.DataCollectionDemo - Kaa client started
    2016-06-21 12:38:13,339 [main] INFO  o.k.k.d.d.DataCollectionDemo - Log record {"level": "KAA_INFO", "tag": "TAG", "message": "MESSAGE_0", "timeStamp": 1466501893337} sent
    2016-06-21 12:38:13,340 [main] INFO  o.k.k.d.d.DataCollectionDemo - Log record {"level": "KAA_INFO", "tag": "TAG", "message": "MESSAGE_1", "timeStamp": 1466501893337} sent
    2016-06-21 12:38:13,340 [main] INFO  o.k.k.d.d.DataCollectionDemo - Log record {"level": "KAA_INFO", "tag": "TAG", "message": "MESSAGE_2", "timeStamp": 1466501893337} sent
    2016-06-21 12:38:13,340 [main] INFO  o.k.k.d.d.DataCollectionDemo - Log record {"level": "KAA_INFO", "tag": "TAG", "message": "MESSAGE_3", "timeStamp": 1466501893337} sent
    2016-06-21 12:38:13,340 [main] INFO  o.k.k.d.d.DataCollectionDemo - Log record {"level": "KAA_INFO", "tag": "TAG", "message": "MESSAGE_4", "timeStamp": 1466501893337} sent
    2016-06-21 12:38:13,627 [main] INFO  o.k.k.d.d.DataCollectionDemo - Received log record delivery info. Bucket Id [0]. Record delivery time [290 ms].
    2016-06-21 12:38:13,627 [main] INFO  o.k.k.d.d.DataCollectionDemo - Received log record delivery info. Bucket Id [0]. Record delivery time [290 ms].
    2016-06-21 12:38:13,627 [main] INFO  o.k.k.d.d.DataCollectionDemo - Received log record delivery info. Bucket Id [0]. Record delivery time [290 ms].
    2016-06-21 12:38:13,627 [main] INFO  o.k.k.d.d.DataCollectionDemo - Received log record delivery info. Bucket Id [0]. Record delivery time [290 ms].
    2016-06-21 12:38:13,627 [main] INFO  o.k.k.d.d.DataCollectionDemo - Received log record delivery info. Bucket Id [0]. Record delivery time [290 ms].
    2016-06-21 12:38:13,628 [pool-2-thread-1] INFO  o.k.k.d.d.DataCollectionDemo - Kaa client stopped
    2016-06-21 12:38:13,629 [main] INFO  o.k.k.d.d.DataCollectionDemo - Data collection demo stopped
    ```

13. Let's verify that Kafka consumer receive logs. From Kafka installation directory run the next command:

    ```bash
    bin/kafka-console-consumer.sh --zookeeper localhost:2183 --topic kaa
    ```

14. You should observe similar output:

    ```
    {"header":{"endpointKeyHash":{"string":"UtzjR4tTem5XDJRZRX9ftZfR7ng="},"applicationToken":{"string":"82635305199158071549"},"headerVersion":{"int":1},"timestamp":{"long":1466501893600},"logSchemaVersion":{"int":2}},"event":{"level":"KAA_INFO","tag":"TAG","message":"MESSAGE_0","timeStamp":1466501893337}}
    {"header":{"endpointKeyHash":{"string":"UtzjR4tTem5XDJRZRX9ftZfR7ng="},"applicationToken":{"string":"82635305199158071549"},"headerVersion":{"int":1},"timestamp":{"long":1466501893600},"logSchemaVersion":{"int":2}},"event":{"level":"KAA_INFO","tag":"TAG","message":"MESSAGE_1","timeStamp":1466501893337}}
    {"header":{"endpointKeyHash":{"string":"UtzjR4tTem5XDJRZRX9ftZfR7ng="},"applicationToken":{"string":"82635305199158071549"},"headerVersion":{"int":1},"timestamp":{"long":1466501893600},"logSchemaVersion":{"int":2}},"event":{"level":"KAA_INFO","tag":"TAG","message":"MESSAGE_2","timeStamp":1466501893337}}
    {"header":{"endpointKeyHash":{"string":"UtzjR4tTem5XDJRZRX9ftZfR7ng="},"applicationToken":{"string":"82635305199158071549"},"headerVersion":{"int":1},"timestamp":{"long":1466501893600},"logSchemaVersion":{"int":2}},"event":{"level":"KAA_INFO","tag":"TAG","message":"MESSAGE_3","timeStamp":1466501893337}}
    {"header":{"endpointKeyHash":{"string":"UtzjR4tTem5XDJRZRX9ftZfR7ng="},"applicationToken":{"string":"82635305199158071549"},"headerVersion":{"int":1},"timestamp":{"long":1466501893600},"logSchemaVersion":{"int":2}},"event":{"level":"KAA_INFO","tag":"TAG","message":"MESSAGE_4","timeStamp":1466501893337}}
    ```

If your output doesn't match above one, please follow our [troubleshooting guide]({{root_url}}Administration-guide/Troubleshooting).
