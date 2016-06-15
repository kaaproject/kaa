---
layout: page
title: Oracle NoSQL log appender
permalink: /:path/
nav: /:path/Programming-guide/Key-platform-features/Data-collection/Oracle-NoSQL-log-appender
sort_idx: 80
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

* TOC
{:toc}

The Oracle NoSQL log appender is responsible for transferring logs from the Operations server to the Oracle NoSQL key/value storage.
Logs are stored in the storage using the following key path:

```bash
${applicationToken}/${logSchemaVersion}/${endpointKeyHash}/${uploadTimestamp}/${counter}
```

The path variables used are:

|Path Variable|Description|
|---|---|
|applicationToken|The token of the application|
|logSchemaVersion|The version of the log schema|
|endpointKeyHash|The ID of the endpoint the log data belongs to|
|uploadTimestamp|The timestamp of log upload to the storage (in milliseconds)|
|count|The log record ID|

<br/>

Values are stored as serialized generic records using record wrapper Avro schema.

# Creating Oracle NoSQL log appender with Admin UI

The easiest way to create an instance of the Oracle NoSQL log appender for the application is by using Admin UI.

To create a log appender of the Oracle NoSQL key/value storage type, do the following:

1. In the <b>Log appenders</b> window, click <b>Add log appender</b>.
2. Enter the log appender name and description, select the minimum and maximum supported schema version, select necessary log metadata fields.
3. Set the log appender type to <i>Oracle NoSQL</i>.
4. Fill in the Oracle NoSQL log appender configuration form.
5. Click <b>Add</b>.

<img src="attach/add-log-appender-in-admin-ui.png">

# Creating Oracle NoSQL log appender with Admin REST API

It is also possible to create an instance of the Oracle NoSQL log appender for the application by using the
[REST API]({{root_url}}Programming-guide/Server-REST-APIs #TODO). The following example illustrates how to provision the Oracle NoSQL log appender via the
Admin REST API.

## Configuration

The Oracle NoSQL log appender configuration should match to
[this](https://github.com/kaaproject/kaa/blob/master/server/appenders/oracle-nosql-appender/src/main/avro/oracle-nosql-appender-config.avsc) Avro schema.

The following configuration example matches the previous schema.

```json
{
    "storeName":"kvstore",
    "kvStoreNodes":[
        {
            "host":"localhost",
            "port":5000
        }
    ],
    "username":null,
    "walletDir":null,
    "pwdFile":null,
    "securityFile":null,
    "transport":null,
    "ssl":null,
    "sslCipherSuites":null,
    "sslProtocols":null,
    "sslHostnameVerifier":null,
    "sslTrustStore":null,
    "sslTrustStoreType":null
}
```

## Administration

The following Admin REST API call example illustrates how to create a new instance of the Oracle NoSQL log appender:

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.appenders.oraclenosql.appender.OracleNoSqlLogAppender", "applicationId": 119, "applicationToken": "91786338058670361194", "jsonConfiguration": "{\"storeName\":\"kvstore\",\"kvStoreNodes\":[{\"host\":\"localhost\",\"port\":5000}],\"username\":null,\"walletDir\":null,\"pwdFile\":null,\"securityFile\":null,\"transport\":null,\"ssl\":null,\"sslCipherSuites\":null,\"sslProtocols\":null,\"sslHostnameVerifier\":null,\"sslTrustStore\":null,\"sslTrustStoreType\":null}", "description": "Sample Oracle NoSQL appender", "headerStructure": [], "name": "Oracle NoSQL appender", "maxLogSchemaVersion": 2147483647, "minLogSchemaVersion": 1, "tenantId": "70"}' "http://localhost:8080/kaaAdmin/rest/api/logAppender" | python -mjson.tool
```

Example result:

```json
{
    "appenderClassName":"org.kaaproject.kaa.server.appenders.oraclenosql.appender.OracleNoSqlLogAppender",
    "applicationId":"70",
    "applicationToken":"946558468095768",
    "configuration":"{\"storeName\":\"kvstore\",\"kvStoreNodes\":[{\"host\":\"localhost\",\"port\":5000}],\"username\":null,\"walletDir\":null,\"pwdFile\":null,\"securityFile\":null,\"transport\":null,\"ssl\":null,\"sslCipherSuites\":null,\"sslProtocols\":null,\"sslHostnameVerifier\":null,\"sslTrustStore\":null,\"sslTrustStoreType\":null}",
    "createdTime":1417107992158,
    "createdUsername":"devuser",
    "description":"Sample Oracle NoSQL appender",
    "headerStructure":[

    ],
    "id":"167",
    "name":"Oracle NoSQL appender",
    "maxLogSchemaVersion":2147483647,
    "minLogSchemaVersion":1,
    "status":"REGISTERED",
    "tenantId":"10",
    "typeName":"Oracle NoSQL"
}
```

# Example

1. Download archive with [Oracle nosql database](#http://www.oracle.com/technetwork/database/database-technologies/nosqldb/downloads/index.html)
and install it to your kaa server.
2. Use [following](https://blogs.oracle.com/charlesLamb/entry/oracle_nosql_database_in_5) tutorial for more information about this database.
3. Create an application using Admin UI or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs #TODO).
4. Add custom log schema that will be using for saving logs in database.<br/>
<img src="attach/nosql-log-appender1.png">
5. Add Oracle Nosql log appender and define configuration for it.<br/>
<img src="attach/nosql-log-appender2.png">
6. Generate SDK appropriate to your platform and write code to send logs to database.

Your client code might look like this:

```bash
...
KaaClient client = ...
...
Data data = new Data("your log data");
//send logs to oracle database
client.addLogRecord(data);
...
```

<b>To see logs<b>:

open admin console:

```bash
java -jar path_to_oracle_db/lib/kvstore.jar runadmin -port $your_port$ -host $your_host$
```

connect to your store:

```bash
connect store -name kvstore  -host $oracle_db_host$  -port $oracle_db_port$;
```

to see logs from kaa:

```bash
get kv -start /${applicationToken} -all
```

Than in your database you will see something like that:

```json
/97657068517919541825/2/519xnHqR4xVpq2MSoLSUKgmSTa4=/1456227512249/-/0
{
    "recordHeader":{
        "org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader":{
            "endpointKeyHash":null,
            "applicationToken":null,
            "headerVersion":null,
            "timestamp":null,
            "logSchemaVersion":null
        }
    },
    "recordData":{
        "org.kaaproject.kaa.example.nosql.Data":{
            "logInfo":{
                "string":"your log data"
            }
        }
    }
}
```
