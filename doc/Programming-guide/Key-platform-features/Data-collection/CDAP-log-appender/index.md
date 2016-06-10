---
layout: page
title: CDAP log appender
permalink: /:path/
nav: /:path/Programming-guide/Key-platform-features/Data-collection/CDAP-log-appender
sort_idx: 20
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

* [Creating CDAP log appender in Admin UI](#creating-cdap-log-appender-in-admin-ui)
* [Creating CDAP log appender with REST API](#creating-cdap-log-appender-with-rest-api)
  * [Configuration](#configuration)
  * [Administration](#administration)

The CDAP log appender is responsible for the logs transfer to the CDAP platform. Logs are stored in a stream that is specified by the stream configuration parameter.

### Creating CDAP log appender in Admin UI

The easiest way to create a CDAP log appender for your application is by using Admin UI.

To create a log appender which will be integrated with CDAP, do the following:

1. In the <b>Log appenders</b> window, click <b>Add log appender</b>.
2. Enter the log appender name and description, select the minimum and maximum supported log schema version, and select necessary log metadata fields.
3. Set the log appender type to <i>Cdap.</i>
4. Fill in the CDAP log appender configuration form.
5. Click <b>Add</b>.

<img src="attach/creating-cdap-log-appender-in-admin-ui.png">



### Creating CDAP log appender with REST API

It is also possible to create a CDAP log appender for your application by using REST API. The following example illustrates how to provision the CDAP log appender via REST API.

### Configuration

The CDAP log appender configuration should match to
[this](https://github.com/kaaproject/kaa/blob/master/server/appenders/cdap-appender/src/main/avro/cdap-appender-config.avsc) Avro schema.

The following configuration example matches the previous schema. 

```json
{
    "stream":"testStreamName",
    "host":"localhost",
    "port":10000,
    "ssl":null,
    "verifySslCert":null,
    "writerPoolSize":null,
    "callbackThreadPoolSize":{
        "int":2
    },
    "version":null,
    "authClient":null,
    "username":null,
    "password":null
}
```

### Administration

The following REST API call example illustrates how to create a new CDAP log appender.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.appenders.cdap.appender.CdapLogAppender", "applicationId": 119, "applicationToken": "91786338058670361194", "jsonConfiguration": "{\"stream\":\"stream\",\"host\":\"localhost\",\"port\":10000,\"ssl\":null,\"verifySslCert\":null,\"writerPoolSize\":null,\"callbackThreadPoolSize\":{\"int\":2},\"version\":null,\"authClient\":null,\"username\":null,\"password\":null}", "description": "New sample Mongo db log appender", "headerStructure": [ "KEYHASH","TIMESTAMP" ], "name": "New Mongo DB appender", "maxLogSchemaVersion": 2147483647, "minLogSchemaVersion": 1, "tenantId": "70"}' "http://localhost:8080/kaaAdmin/rest/api/logAppender" | python -mjson.tool
```

Example result:

```json
{
    "appenderClassName":"org.kaaproject.kaa.server.appenders.cdap.appender.CdapLogAppender",
    "applicationId":"70",
    "applicationToken":"946558468095768",
    "configuration":"{\"stream\":\"stream\",\"host\":\"localhost\",\"port\":10000,\"ssl\":null,\"verifySslCert\":null,\"writerPoolSize\":null,\"callbackThreadPoolSize\":{\"int\":2},\"version\":null,\"authClient\":null,\"username\":null,\"password\":null}",
    "createdTime":1417105293146,
    "createdUsername":"devuser",
    "description":"Sample Cdap appender",
    "headerStructure":[
        "KEYHASH",
        "TOKEN"
    ],
    "id":"165",
    "name":"Cdap appender",
    "maxLogSchemaVersion":2147483647,
    "minLogSchemaVersion":1,
    "status":"REGISTERED",
    "tenantId":"10",
    "typeName":"Cdap"
}
```
