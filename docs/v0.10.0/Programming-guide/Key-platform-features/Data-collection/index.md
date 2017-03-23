---
layout: page
title: Data collection
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

* TOC
{:toc}

The data collection subsystem in Kaa is designed to collect records (logs) of pre-configured structure, store them in the [client]({{root_url}}Glossary/#kaa-client), transfer from the client to [Operations service]({{root_url}}Glossary/#operations-service), persist on server for further processing, or submit to the immediate stream analysis.
The log structure in Kaa is determined by a configurable schema for each [application]({{root_url}}Glossary/#kaa-application) individually.
Log appenders on the Operations service side write the logs received by the Operations service to a specific storage place.
You can have several log appenders working simultaneously.

This section explains how you can use the data collection feature in Kaa.

## Basic architecture

The logs are collected from the endpoints and transferred to the server in the format defined in the [log schema](#log-schema) created by developer for the application.
Log appenders submit the logs received by the server to to the analytics system.
See the picture below.

![Basic data collection management](attach/basic-data-collection-management.png)

The data collection subsystem provides the following features:

* Generation of the logging model and related API calls in the [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk).
* Enforcement of data integrity and validity.
* Efficient delivery of logs to Operations services.
* Storing log contents using the log appenders configured for the application.

The application developer is responsible for designing the log schema and enabling the endpoint logging API from the client application.

## Log record

A log record consists of:

* Log events.
* Log record header.
* Client/server-side endpoint profile.

Log events and record header data match the corresponding [log schema](#log-schema) and [log record header schema](#log-record-header-schema) respectively.
To add client/server-side endpoint profile data to the log record, follow to the corresponding [log appender documentation](#existing-log-appender-implementations).

## Log schema

The log schema is fully compatible with the [Apache Avro schema](http://avro.apache.org/docs/current/spec.html#schemas).
There is one log schema defined by default for each Kaa application.
This schema supports versioning, therefore, whenever a new log schema is configured on the Kaa service for the application, this new schema gets a new sequence version assigned.
The Kaa service maintains compatibility with the older versions of the log schema to ensure proper functioning of the clients that for some reason are not yet upgraded to the latest schema version.

Below are basic log schema examples.

A simple definition of a log record with no data fields.

```json
{  
    "name":"EmptyLog",  
    "namespace":"org.kaaproject.sample",  
    "type":"record",  
    "fields":[
    ]
}
```

### Adding log schema

The default log schema installed for Kaa applications is empty.
To configure your own log schema, use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Logging/saveLogSchema) or open the **Log** page of the application and follow the same steps as described in [Setting client-side EP profile schema]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/#setting-client-side-ep-profile-schema).
See also the [logging REST API]({{root_url}}Programming-guide/Server-REST-APIs/#/Logging).

For the purpose of this guide, a typical log schema structure is used:

* log level
* tag
* message

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
                    "DEBUG",
                    "ERROR",
                    "FATAL",
                    "INFO",
                    "TRACE",
                    "WARN"
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
        }
    ]
}
```


## Schema for log record header

The schema of a log record header is fully compatible with the [Apache Avro schema](http://avro.apache.org/docs/current/spec.html#schemas).
This schema defines the structure of the log record header that will be automatically added to the [log record](#log-record) in Kaa.

The schema for the log record header is defined below:

```json
{
    "name":"recordHeader",
    "type":[
        {
            "type":"record",
            "name":"RecordHeader",
            "namespace":"org.kaaproject.kaa.server.common.log.shared.avro.gen",
            "fields":[
                {
                    "name":"endpointKeyHash",
                    "type":[
                        {
                            "type":"string",
                            "avro.java.string":"String"
                        },
                        "null"
                    ]
                },
                {
                    "name":"applicationToken",
                    "type":[
                        {
                            "type":"string",
                            "avro.java.string":"String"
                        },
                        "null"
                    ]
                },
                {
                    "name":"headerVersion",
                    "type":[
                        "int",
                        "null"
                    ]
                },
                {
                    "name":"timestamp",
                    "type":[
                        "long",
                        "null"
                    ]
                },
                {
                    "name":"logSchemaVersion",
                    "type":[
                        "int",
                        "null"
                    ]
                }
            ]
        },
        "null"
    ]
}
```

### Adding log record header

You can use a log appender to configure the header data of a log record.

To do this:

* Under the **Schemas** section of the application, click **Log appenders**, then click **Add log appender**.
On the **Log appender details** page, edit the **Log metadata** field.

	![Add log record header](attach/add-log-record-header.png)

OR
	
* Use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Logging/editLogAppender) call to create a log appender.
To do this, add the corresponding fields to the <code>headerStructure</code> list.

Below is the list of available record header fields:

|Name       |Description                                                             |
|-----------|------------------------------------------------------------------------|
|KEYHASH    |A key hash identifying the endpoint which produced the log record.      |
|VERSION    |Version of the header.                                                  |
|TIMESTAMP  |Timestamp in milliseconds when logs were uploaded to the Kaa server.    |
|TOKEN      |[Application token]({{root_url}}Glossary/#application-token).           |
|LSVERSION  |Version of the log schema.                                              |

## Log appenders

Log appender is a service utility that operates on the [Operations service]({{root_url}}Glossary/#operations-service) side.
The logs received by the Operations service from the endpoints are recorded by the log appender in the specific storage place depending on the log appender type.
You can have several log appenders working simultaneously.
Kaa provides several default implementations of log appenders but you can also develop and integrate [custom log appenders]({{root_url}}Customization-guide/Log-appenders).

### Confirm delivery option

Every [Kaa client]({{root_url}}Glossary/#kaa-client) stores logs in a special log storage before sending them to the [Kaa server]({{root_url}}Glossary/#kaa-server) node.
By default, when a log appender on the Kaa server side receives a log from the Kaa client, it will send a delivery confirmation back to the Kaa client.
In case of a successful delivery, the Kaa client deletes the log copies from its local storage.
In case of a delivery error, Kaa client receives the error code and either sends the log to the same server node again or sends it to a different node.
The delivery confirmation option makes the log appender verify that the received log has been recorded in the log storage prior to sending the confirmation.

The following scenario illustrates how the delivery confirmation works:

1. You set up a log appender with the **Confirm delivery** option enabled.
The Kaa client will receive a message about successful logs delivery only after the Kaa node successfully receives and writes the logs to external storage.
If the Kaa node fails to write the logs to the storage, the Kaa client will send the logs again.
If there are more than one log appender with the **Confirm delivery** option enabled, the Kaa client will receive a message about successful logs delivery only after each log appender successfully writes its logs to the storage.

2. You set up a log appender with the **Confirm delivery** option disabled.
The Kaa client will receive a message about successful logs delivery to the Kaa node, but if the Kaa node fails to write the logs to the external storage, the Kaa client will not be informed about the failure and will not try to send the logs again.
As a result, the logs will be lost.

3. You set up a log appender A with the **Confirm delivery** option enabled, and a log appender B with that option disabled.
The Kaa client will receive a message about successful logs delivery as soon as log appender A confirms the delivery.
Any errors that might happen while log appender B writes its logs to the storage will not be taken into account.

To summarize, the delivery confirmation option allows you to have the guaranteed delivery of every log record to the external storage.

>**NOTE:** By default, Kaa client uses its internal memory to temporarily store the logs before sending them to the server.
>This means that if you reset your endpoint before the client delivers the logs, those logs will be lost.
>To avoid this, use a persistent log storage to store the data that is not yet confirmed as delivered.

### Existing log appender implementations

There are several default log appender implementations that are available out of the box for each Kaa installation.
A Kaa developer can add, update, and delete log appenders using the Administration UI or server REST APIs.
After you added a log appender, make sure you have correctly [configured the data collection feature](#data-collection-api).

For more information about architecture, configuration and administration of a particular log appender, use the following sections of Kaa documentation.

|Log appender|Description|
|------------------------|
|[Cassandra]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Cassandra-log-appender)|Transfers logs from the Operations service to the [Cassandra](http://cassandra.apache.org/) database. You can flexibly configure Cassandra storage (Cassandra nodes, authentication credentials, keyspace name, table name, column mapping, clustering and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Cassandra-log-appender/#configure-log-appender).|
|[Couchbase]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Couchbase-log-appender)|Transfers logs from the Operations service to the [Couchbase](http://www.couchbase.com/) database. You can configure Couchbase servers list, bucket name and password.|
|[File system]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender)|Stores the received logs in the local file system of the Operations service. This log appender can be used for test purposes or in conjunction with tools like Flume and others. You can configure logs root path, file name pattern, maximum number of records in a file, maximum file size and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/File-system-log-appender/#configure-log-appender).|
|[Flume]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Flume-log-appender)|Encapsulates the received logs into Flume events and sends these events to external Flume sources via Avro RPC. You can configure Flume event format, hosts balancing type, include client-side and/or server-side profile data and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Flume-log-appender/#configure-log-appender).|
|[Kafka]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Kafka-log-appender)|Transfers logs from the Operations service to the [Apache Kafka](http://kafka.apache.org/) service. The logs are stored within the specified topic. You can configure Kafka bootstrap servers list, topic name, compression type and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Kafka-log-appender/#configure-log-appender).|
|[MongoDB]({{root_url}}Programming-guide/Key-platform-features/Data-collection/MongoDB-log-appender)|Transfers logs from the Operations service to the [MongoDB](https://www.mongodb.com/) database. You can configure MongoDB nodes list, authentication credentials, MongoDB database name and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/MongoDB-log-appender/#configure-log-appender). The logs are stored in the table named **logs_$applicationToken**, where **$applicationToken** matches the token of the current application.|
|[Oracle NoSQL]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Oracle-NoSQL-log-appender)|Transfers logs from the Operations service to the [Oracle NoSQL](http://www.oracle.com/technetwork/database/database-technologies/nosqldb/overview/index.html) key/value storage. You can configure KVStore name, KVStore nodes and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Oracle-NoSQL-log-appender/#configure-log-appender).|
|[REST]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Rest-log-appender)|Transfers logs from Operations service to your custom service. You can configure host, port, relative URI path, method type and [other parameters]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Rest-log-appender/#configure-log-appender).|

### Custom log appender implementations

To learn how to create and integrate custom log appenders, see [Log appenders section in the Customization guide]({{root_url}}Customization-guide/Log-appenders).

## Data collection API

### Log delivery

The logging subsystem API varies depending on the [SDK type]({{root_url}}Glossary/#kaa-sdk-type).
However, the general approach is the same.

To transfer logs to the Kaa Operations service, the Kaa client application uses the following code.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp">C++</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1" >

```java
// Configure the log delivery listener
kaaClient.setLogDeliveryListener(new LogDeliveryListener() {
    @Override
    public void onLogDeliverySuccess(BucketInfo bucketInfo) { /* Called on success */ }
    @Override
    public void onLogDeliveryFailure(BucketInfo bucketInfo) { /* Called on failure */ }
    @Override
    public void onLogDeliveryTimeout(BucketInfo bucketInfo) { /* Called on timeout */ }
});
// Create a log entity according to the (org.kaaproject.sample.LogData) sample schema above
LogData logRecord = new LogData(Level.INFO, "tag", "message");
// Push the record to the collector
RecordFuture logDeliveryStatus = kaaClient.addLogRecord(logRecord);
// Get log delivery information
RecordInfo logDeliveryReport = logDeliveryStatus.get();
```

</div>
<div id="Cpp" class="tab-pane fade" markdown="1" >

```cpp
#include <iostream>
#include <exception>
#include <kaa/Kaa.hpp>
 
...
 
// Create an endpoint instance
auto kaaClient = kaa::Kaa::newClient();
// Start an endpoint
kaaClient->start();

// Create a log entity (according to the org.kaaproject.sample.LogData sample schema above) 
kaa::KaaUserLogRecord logRecord;
logRecord.level = kaa_log::Level::INFO;
logRecord.tag = "tag";
logRecord.message = "message";
 
// Push the record to the collector
auto recordDeliveryCallback = kaaClient->addLogRecord(logRecord);
 
try {
    auto recordInfo = recordDeliveryCallback.get();
    auto bucketInfo = recordInfo.getBucketInfo();
    std::cout << "Received log record delivery info. Bucket Id [" <<  bucketInfo.getBucketId() << "]. "
        << "Record delivery time [" << recordInfo.getRecordDeliveryTimeMs() << " ms]." << std::endl;
} catch (std::exception& e) {
    std::cout << "Exception was caught while waiting for callback result: " << e.what() << std::endl;
}
```

</div>
<div id="C" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <kaa/gen/kaa_logging_gen.h>
#include <kaa/platform/kaa_client.h>
#include <extensions/logging/kaa_logging.h>
 
kaa_client_t *kaa_client = /* ... */;
void *log_storage_context         = NULL;
void *log_upload_strategy_context = NULL;
 
/* Optional context that passed to log delivery callbacks */
void *log_delivery_context = NULL;
 
/* Assume Kaa SDK is already initialized */
 
/* Set of routines that handles log delivery events */

static void success_log_delivery_callback(void *context, const kaa_log_bucket_info_t *bucket)
{
    /* ... */
}
 
static void failed_log_delivery_callback(void *context, const kaa_log_bucket_info_t *bucket)
{
    /* ... */
}
 
static void timeout_log_delivery_callback(void *context, const kaa_log_bucket_info_t *bucket)
{
    /* ... */
}
 
/* Log delivery listener callbacks. Each callback called whenever something happen with a log bucket. */
kaa_log_delivery_listener_t log_listener = {
     .on_success = success_log_delivery_callback,   /* Called if log delivered successfully */
     .on_failed  = failed_log_delivery_callback,    /* Called if delivery failed */
     .on_timeout = timeout_log_delivery_callback,   /* Called if timeout occurs */
     .ctx        = log_delivery_context,            /* Optional context */
};
 
/* The internal memory log storage distributed with Kaa SDK */
kaa_error_t error_code = ext_unlimited_log_storage_create(&log_storage_context, kaa_client_get_context(kaa_client)->logger);
 
/* Check error code */
 
/* Specify log bucket size constraints */
kaa_log_bucket_constraints_t bucket_sizes = {
     .max_bucket_size       = 512,  /* Bucket size in bytes */
     .max_bucket_log_count  = 5,    /* Maximum log count in one bucket */
};
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                              , log_storage_context
                              , log_upload_strategy_context
                              , &bucket_sizes);
 
/* Check error code */
 
/* Add listeners to a log collector */
kaa_logging_set_listeners(kaa_client_get_context(kaa_client)->log_collector, &log_listener);
 
/* Create and add a log record */
 
kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
 
log_record->level = ENUM_LEVEL_KAA_TRACE;
log_record->tag = kaa_string_copy_create("SOME_TAG");
log_record->message = kaa_string_copy_create("SOME_MESSAGE");
 
/* Log information. Populated when log is added via kaa_logging_add_record() */
kaa_log_record_info_t log_info;
 
/* Add log record */
error_code = kaa_logging_add_record(kaa_client_get_context(kaa_client)->log_collector, log_record, &log_info);
 
/* Check error code */
 
log_record->destroy(log_record);
```

</div>
<div id="Objective-C" class="tab-pane fade" markdown="1" >

```objc
// Create a log entity (according to the org.kaaproject.sample.LogData sample schema above)
LogData *logRecord = [[LogData alloc] initWithLevel:LEVEL_INFO tag:@"tag" message:@"message"];
 
// Push the record to the collector
BucketRunner *runner = [kaaClient addLogRecord:logRecord];
 
// Add callback for log delivery
@try {
    [[[NSOperationQueue alloc] init] addOperationWithBlock:^{
        BucketInfo *bucketInfo = [runner getValue];
        NSLog(@"Received log record delivery info. Bucket Id [%d]. Record delivery time [%f ms]", bucketInfo.bucketId, bucketInfo.bucketDeliveryDuration);
    }];
}
@catch (NSException *exception) {
    NSLog(@"Exception was caught while waiting for callback");
}
```

</div>
</div>

### Log storage

By default, the Kaa SDK uses an in-memory log storage. Normally, this storage does not persist data when the client is restarted. If this is a concern, 
Java/Objective-C/C++ SDKs provide a persistent log storage based on SQLite database.

Below is an example of using SQLite log storage for Java/Objective-C/C++ SDKs and a custom implementation for C SDK:

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java2">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp2">C++</a></li>
  <li><a data-toggle="tab" href="#Android2">Android</a></li>
  <li><a data-toggle="tab" href="#C2">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C2">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java2" class="tab-pane fade in active" markdown="1" >

```java
// Default SQLite database name
String databaseName = "kaa_logs";
// Default maximum bucket size in bytes
int maxBucketSize = 16 * 1024;
// Default maximum amount of log records in a bucket
int maxRecordCount = 256;
// Setting SQLite log storage implementation
kaaClient.setLogStorage(new DesktopSQLiteDBLogStorage(databaseName, maxBucketSize, maxRecordCount));
```

</div>
<div id="Android2" class="tab-pane fade in active" markdown="1" >

```java
// Setting SQLite log storage implementation
kaaClient.setLogStorage(new AndroidSQLiteDBLogStorage(/*Android context*/, "kaa_logs"/* default value */, 16 * 1024/* default value */, 256/* default value */)));
```

</div>
<div id="Cpp2" class="tab-pane fade" markdown="1" >

```cpp
#include <memory>
#include <kaa/log/SQLiteDBLogStorage.hpp>
#include <kaa/Kaa.hpp>
 
...
 
// Create an endpoint instance
auto kaaClient = kaa::Kaa::newClient();	 
// Create the storage
auto persistentStorage = std::make_shared<kaa::SQLiteDBLogStorage>(kaaClient->getKaaClientContext());
// Setting SQLite log storage implementation
kaaClient->setLogStorage(persistentStorage);
```

</div>
<div id="C2" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
 
kaa_client_t *kaa_client = NULL /* ... */;
 
/*
 * Log storage described in "kaa/platform/ext_log_storage.h"
 */
 
void *persistent_log_storage_context = NULL;
 
 
/*
 * Log upload strategy described in "kaa/platform/ext_log_upload_strategy.h"
 */
void *persistent_log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK is already initialized.
 * Create persistent_log_storage_context and persistent_log_upload_strategy_context instances.
 */
 
/* Specify log bucket size constraints */
kaa_log_bucket_constraints_t bucket_sizes = {
     .max_bucket_size       = 512,  /* Bucket size in bytes */
     .max_bucket_log_count  = 5,    /* Maximum log count in one bucket */
};
 
/* Initialize the log storage and strategy (by default it is not set) */
kaa_error_t error_code;
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , persistent_log_storage_context
                            , persistent_log_upload_strategy_context
                            , &bucket_sizes);
 
/* Check error code */
```

</div>
<div id="Objective-C2" class="tab-pane fade" markdown="1" >

```objc
// Default maximum bucket size in bytes
int maxBucketSize = 16 * 1024;
// Default maximum amount of log records in a bucket
int maxRecordCount = 256;
// Setting log storage implementation.
[kaaClient setLogStorage:[[SQLiteLogStorage alloc] initWithBucketSize:maxBucketSize bucketRecordCount:maxRecordCount]];
```

</div>
</div>

### Log upload strategies

A log upload strategy defines under what conditions Kaa endpoints must send log data to the server.

Kaa provides the following built-in strategies.

**Periodic** strategy uploads the logs after the set period of time passes since the last upload.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java3">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp3">C++</a></li>
  <li><a data-toggle="tab" href="#C3">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C3">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java3" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to upload no less than a hour worth of logs
kaaClient.setLogUploadStrategy(new PeriodicLogUploadStrategy(60, TimeUnit.MINUTES));
```

</div>
<div id="Cpp3" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs each 60 seconds
kaaClient->setLogUploadStrategy(std::make_shared<kaa::PeriodicLogUploadStrategy>(60, kaaClient->getKaaClientContext()));
```

</div>
<div id="C3" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on timeout. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_BY_TIMEOUT_STRATEGY);
 
/* Check error code */
 
/* Strategy will upload logs every 5 seconds. */
error_code = ext_log_upload_strategy_set_upload_timeout(log_upload_strategy_context, 5);
 
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C3" class="tab-pane fade" markdown="1" >

```c
// Create log upload strategy, which will upload logs every 20 seconds
PeriodicLogUploadStrategy *uploadStrategy = [[PeriodicLogUploadStrategy alloc] initWithTimeLimit:20 timeUnit:TIME_UNIT_SECONDS];
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

>**NOTE:** The decision on whether to upload the collected logs is taken each time a new log record is added.
>This means that a log uplod will be triggered by the next log record added after the specified period of time.
{:.note}

**Log count** strategy uploads the logs upon reaching the set limit of log records number.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java4">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp4">C++</a></li>
  <li><a data-toggle="tab" href="#C4">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C4">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java4" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to upload logs every fifth log record added
kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(5));
// Configure the strategy to upload logs immediately
kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
```

</div>
<div id="Cpp4" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs immediately after the 5th log record is added
kaaClient->setLogUploadStrategy(std::make_shared<kaa::RecordCountLogUploadStrategy>(5, kaaClient->getKaaClientContext()));
```

</div>
<div id="C4" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log count. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, THRESHOLD_COUNT_FLAG);
  
/* Check error code */
 
/* After 50th log in storage, upload will be initiated. */
error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, 50);
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C4" class="tab-pane fade" markdown="1" >

```objc
// Create log upload strategy based on number of log records
RecordCountLogUploadStrategy *uploadStrategy = [[RecordCountLogUploadStrategy alloc] initWithCountThreshold:10];
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

**Storage size** strategy uploads the logs upon reaching the set limit of local space to store the log records.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java5">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp5">C++</a></li>
  <li><a data-toggle="tab" href="#C5">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C5">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java5" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to upload logs every 64 KB of data collected
kaaClient.setLogUploadStrategy(new StorageSizeLogUploadStrategy(64 * 1024));
```

</div>
<div id="Cpp5" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs immediately after the volume of collected logs exceeds 100 bytes
kaaClient->setLogUploadStrategy(std::make_shared<kaa::StorageSizeLogUploadStrategy>(100, kaaClient->getKaaClientContext()));
```

</div>
<div id="C5" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log storage volume. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, THRESHOLD_VOLUME_FLAG);
 
/* Check error code */
 
/* Set log upload strategy based on log records size (in bytes). In this case threshold will be set to 1 KB. */
error_code = ext_log_upload_strategy_set_threshold_volume(log_upload_strategy_context, 1024);
 
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C5" class="tab-pane fade" markdown="1" >

```objc
// Create log upload strategy based on log records size (in bytes). In this case threshold will be set to 1 KB.
StorageSizeLogUploadStrategy *uploadStrategy = [[StorageSizeLogUploadStrategy alloc] initWithVolumeThreshold:1024];
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

A combination of the **periodic** and **log count** strategies.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java6">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp6">C++</a></li>
  <li><a data-toggle="tab" href="#C6">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C6">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java6" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to upload logs every fifth log record added ...
// .. OR the next log record added after 60 seconds pass since the last upload
kaaClient.setLogUploadStrategy(new RecordCountWithTimeLimitLogUploadStrategy(5, 60, TimeUnit.SECONDS));
```

</div>
<div id="Cpp6" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs immediately after the 5th log record is added or each 60 seconds
kaaClient->setLogUploadStrategy(std::make_shared<kaa::RecordCountWithTimeLimitLogUploadStrategy>(5, 60, kaaClient->getKaaClientContext()));
```

</div>
<div id="C6" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log count and timeout. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_BY_RECORD_COUNT_AND_TIMELIMIT);
 
/* Check error code */
 
/* After 50th log in storage, upload will be initiated. */
error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, 50);
 
/* Check error code */
 
/* Strategy will upload logs every 5 seconds. */
error_code = ext_log_upload_strategy_set_upload_timeout(log_upload_strategy_context, 5);
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C6" class="tab-pane fade" markdown="1" >

```objc
// Create log upload strategy, which will upload logs when either log count threshold or time limit is reached
RecordCountWithTimeLimitLogUploadStrategy *uploadStrategy = [[RecordCountWithTimeLimitLogUploadStrategy alloc] initWithCountThreshold:10 timeLimit:20 timeUnit:TIME_UNIT_SECONDS];
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

A combination of the **periodic** and **storage size** strategies.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java7">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp7">C++</a></li>
  <li><a data-toggle="tab" href="#C7">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C7">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java7" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to upload logs every 8 KB of data collected ...
// .. OR the next log record added after 10 seconds pass since the last upload
kaaClient.setLogUploadStrategy(new StorageSizeWithTimeLimitLogUploadStrategy(8 * 1024, 10, TimeUnit.SECONDS));
```

</div>
<div id="Cpp7" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs immediately after the volume of collected logs exceeds 100 bytes or each 60 seconds
kaaClient->setLogUploadStrategy(std::make_shared<kaa::StorageSizeWithTimeLimitLogUploadStrategy>(100, 60, kaaClient->getKaaClientContext()));
```

</div>
<div id="C7" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log storage volume and timeout. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_BY_STORAGE_SIZE_AND_TIMELIMIT);
 
/* Check error code */
 
/* Set log upload strategy based on log records size (in bytes). In this case threshold will be set to 1 KB. */
error_code = ext_log_upload_strategy_set_threshold_volume(log_upload_strategy_context, 1024);
 
/* Check error code */
 
/* Strategy will upload logs every 5 seconds. */
error_code = ext_log_upload_strategy_set_upload_timeout(log_upload_strategy_context, 5);
 
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C7" class="tab-pane fade" markdown="1" >

```objc
// Create log upload strategy, which will upload log records when either volume threshold (in bytes) or time limit is reached.
StorageSizeWithTimeLimitLogUploadStrategy *uploadStrategy = [[StorageSizeWithTimeLimitLogUploadStrategy alloc] initWithThresholdVolume:1024 timeLimit:20 timeUnit:TIME_UNIT_SECONDS];
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

A combination of the **log count** and **storage size** strategies.



<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java8">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp8">C++</a></li>
  <li><a data-toggle="tab" href="#C8">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C8">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java8" class="tab-pane fade in active" markdown="1" >

```java
// Create an instance of the default log upload strategy
DefaultLogUploadStrategy customizedStrategy = new DefaultLogUploadStrategy();
// Configure it to upload logs every fifteen log records ...
strategy.setCountThreshold(15);
// ... OR every 32 KB of data collected
strategy.setVolumeThreshold(32 * 1024);
kaaClient.setLogUploadStrategy(customizedStrategy);
```

</div>
<div id="Cpp8" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy to upload logs immediately after the 5th log record is added or the volume of collected logs exceeds 100 bytes
auto logUploadStrategy = std::make_shared<kaa::DefaultLogUploadStrategy>(kaaClient->getKaaClientContext());
logUploadStrategy->setCountThreshold(5);
logUploadStrategy->setVolumeThreshold(100);
kaaClient->setLogUploadStrategy(logUploadStrategy);
```

</div>
<div id="C8" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log storage volume and count. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
 
/* Check error code */
 
/* After 50th log in storage, upload will be initiated. */
error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, 50);
 
/* Check error code */
 
/* Set log upload strategy based on log records size (in bytes). In this case threshold will be set to 1 KB. */
error_code = ext_log_upload_strategy_set_threshold_volume(log_upload_strategy_context, 1024);
 
/* Check error code */
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C8" class="tab-pane fade" markdown="1" >

```objc
// Create log upload strategy, which will upload log records when either volume threshold or log count threshold is reached.
DefaultLogUploadStrategy *uploadStrategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
// Set volume threshold (in bytes) for our strategy
uploadStrategy.volumeThreshold = 1024;
// Set log count threshold of our strategy
uploadStrategy.countThreshold = 10;
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>

>**NOTE:** This is the default behavior with the maximum number of log records set to **64** and the maximum local storage space set to **8 KB**.
{:.note}

**Max parallel upload** strategy limits the number of log batches sent without receiving a response from the server. You can configure this strategy for just one log batch to ensure sequential, ordered log delivery to the server.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java9">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp9">C++</a></li>
  <li><a data-toggle="tab" href="#C9">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C9">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java9" class="tab-pane fade in active" markdown="1" >

```java
// Configure the strategy to preserve the exact order of log uploads
DefaultLogUploadStrategy customizedStrategy = new DefaultLogUploadStrategy();
strategy.setMaxParallelUploads(1);
kaaClient.setLogUploadStrategy(customizedStrategy);
```

</div>
<div id="Cpp9" class="tab-pane fade" markdown="1" >

```cpp
// Configure the strategy not to upload logs until a previous upload is successfully performed
auto logUploadStrategy = std::make_shared<kaa::DefaultLogUploadStrategy>(kaaClient->getKaaClientContext());
logUploadStrategy->setMaxParallelUploads(1);
kaaClient->setLogUploadStrategy(logUploadStrategy);
```

</div>
<div id="C9" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <extensions/logging/kaa_logging.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>
 
kaa_client_t *kaa_client = NULL;
 
/*
 * The log storage.
 */
void *log_storage_context = NULL;
 
/*
 * Log upload strategy.
 */
void *log_upload_strategy_context = NULL;
 
/*
 * Assume Kaa SDK and log storage are already initialized.
 */
 
kaa_log_bucket_constraints_t bucket_sizes = { /* Specify bucket size constraints */ };
 
/* Create a strategy based on log storage volume. */
kaa_error_t error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
 
/* Check error code */
 
/* After 50th log in storage, upload will be initiated. */
error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, 50);
 
/* Check error code */
 
/* Configure the strategy not to upload logs until a previous upload is successfully performed */
error_code = ext_log_upload_strategy_set_max_parallel_uploads(log_upload_strategy_context, 1)
 
/* Initialize the log storage and strategy (by default it is not set) */
error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                            , log_storage_context
                            , log_upload_strategy_context
                            , &bucket_sizes);
```

</div>
<div id="Objective-C9" class="tab-pane fade" markdown="1" >

```objc
// Create default log upload strategy
DefaultLogUploadStrategy *uploadStrategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
// Limit the maximum number of parallel log uploads
uploadStrategy.maxParallelUploads = 1;
// Configure client to use our newly created strategy
[kaaClient setLogUploadStrategy:uploadStrategy];
```

</div>
</div>
