---
layout: page
title: Couchbase log appender
permalink: /:path/
nav: /:path/Programming-guide/Key-platform-features/Data-collection/Couchbase-log-appender
sort_idx: 30
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

* TOC
{:toc}

The Couchbase log appender is responsible for transferring logs from the Operations service to the Couchbase storage. Logs are stored in document storage.

Each log document consists of the following fields:  

where:

* <i>id</i> - randomly generated UUID string
* <i>header</i> - includes log's metainformation: <i>endpointKeyHash</i> (a key hash identifying the endpoint which produced the log record), 
<i>[applicationToken]({{root_url}}Glossary)</i> (matches the token of the current application), <i>headerVersion</i> (currently is 1), <i>timestamp</i> (a timestamp in milliseconds
when logs were uploaded to the key/value storage)
* <i>event</i> - log body (log item, received from a client)

Values are stored as serialized Generic Records using record wrapper avro schema.

# Creating Couchbase log appender in Admin UI

The easiest way to create a Couchbase log appender for your application is by using Admin UI.

To create a log appender of the Couchbase storage type, do the following:

1. In the <b>Log appenders</b> window, click <b>Add log appender</b>.
2. Enter the log appender name and description, select the minimum and maximum supported schema version, select necessary log metadata fields.
3. Set the log appender type to <i>Couchbase</i>.
4. Fill in the Couchbase server list and other fields.
5. Click <b>Add</b>.

<img src="attach/create-couchbase-log-appende.png">

# Creating Couchbase log appender with Admin REST API

It is also possible to create a Couchbase log appender for your application by using [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs #TODO).
The following example illustrates how to provision the Couchbase log appender via Admin REST API.

## Configuration

The Couchbase log appender configuration should match the to
[this](https://github.com/kaaproject/kaa/blob/master/server/appenders/couchbase-appender/src/main/avro/couchbase-appender-config.avsc) Avro schema.

The following configuration example matches the previous schema.

```json
{
    "couchbaseServerUris":[
        {
            "serverUri":"http://127.0.0.1:8091/pools"
        }
    ],
    "bucket":"kaa",
    "password":null
}
```

## Administration

The following Admin REST API call example illustrates how to create a new Couchbase log appender.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d @couchbaseLogAppender.json "http://localhost:8080/kaaAdmin/rest/api/logAppender" | python -mjson.tool
```
where file ```couchbaseLogAppender.json``` contains following data:

```json
{
    "pluginClassName":"org.kaaproject.kaa.server.appenders.couchbase.appender.CouchbaseLogAppender",
    "pluginTypeName":"Couchbase",
    "applicationId":"5",
    "applicationToken":"82635305199158071549",
    "name":"Sample Couchbase log appender",
    "description":"Playing with Couchbase log appender",
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
    "jsonConfiguration":"{\"couchbaseServerUris\":[{\"serverUri\":\"http://127.0.0.1:8091/pools\"}],\"bucket\":\"kaa\",\"password\":{\"string\":\"\"}}"
}
```

Example result:

```json
{
    "applicationId": "5",
    "applicationToken": "82635305199158071549",
    "confirmDelivery": true,
    "createdTime": 1466071152031,
    "createdUsername": "devuser",
    "description": "Playing with Couchbase log appender",
    "headerStructure": [
        "KEYHASH",
        "VERSION",
        "TIMESTAMP",
        "TOKEN",
        "LSVERSION"
    ],
    "id": "65551",
    "jsonConfiguration": "{\"couchbaseServerUris\":[{\"serverUri\":\"http://127.0.0.1:8091/pools\"}],\"bucket\":\"kaa\",\"password\":{\"string\":\"\"}}",
    "maxLogSchemaVersion": 2147483647,
    "minLogSchemaVersion": 1,
    "name": "Sample Couchbase log appender",
    "pluginClassName": "org.kaaproject.kaa.server.appenders.couchbase.appender.CouchbaseLogAppender",
    "pluginTypeName": "Couchbase",
    "tenantId": "1"
}
```

# Playing with Couchbase log appender

1. Download and install Couchbase by following [link](http://developer.couchbase.com/documentation/server/current/getting-started/installing.html#installing)
2. After successful installation open http://%your_host_name%:8091/. <br/>
You should see something like that: <br/>
<img src="attach/couchbase-start.png"> <br/>
3. Click **Setup** <br/>
<img src="attach/couchbase-start-next.png"> <br/>
4. Click **Next** <br/>
<img src="attach/couchbase-start-next2.png"> <br/>
5. Click **Next** <br/>
<img src="attach/couchbase-start-next3.png"> <br/>
6. Click **Next** <br/>
<img src="attach/couchbase-start-next4.png"> <br/>
7. Read and agree to the terms and conditions associated with this product. Click **Next** <br/>
<img src="attach/couchbase-start-next5.png"> <br/>
8. Create an administrator account for this Server. Click **Next**. <br/>
You should see something like that: <br/>
<img src="attach/couchbase-8091.png"> <br/>
9. Go to Admin UI and add a Couchbase log appender to Data Collection demo application. <br/>
<img src="attach/add-couchbase-log-appender.png"> <br/>
10. Define a url and bucket which will be used for receiving logs. <br/>
<img src="attach/define-url.png"> <br/>
11. Go to sandbox and download binary for testing appender: <br/>
<img src="attach/data-collection-demo-in-sandbox.png"> <br/>
Use instruction from sandbox to run demo application. <br/>
12. Open http://%your_host_name%:8091 and choose : Data Buckets -> kaa -> Documents: <br/>
<img src="attach/data-buckets-default-documents.png"> <br/>
13. If you launched Data collection demo application before it, you would be see logs from kaa server. This should seems like that: <br/>
<img src="attach/logs-from-kaa-server1.png"> <br/>
<img src="attach/logs-from-kaa-server2.png">
