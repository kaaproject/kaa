---
layout: page
title: File system log appender
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

The file system log appender stores the received logs into the local file system of the [Operations service]({{root_url}}Glossary/#operations-service).
This log appender can be used for test purposes or in pair with tools like [Flume](https://flume.apache.org/) etc.
Log files are stored in the `/$logsRootPath/tenant_$tenantId/application_$applicationId` folder that includes `logsRootPath` as a configuration parameter, `tenantId` and `applicationId` as the IDs of the current tenant and the application respectively.
Access to the logs is controlled trough Linux file system permissions.

You can log in to the Operations service host and browse logs using the `kaa_log_user_$applicationToken` username and the pubic key created as a part of the configuration.

## Create file system log appender

>**IMPORTANT:** Prior to creating the log appender, you need to specify explicit user privileges in `/etc/sudoers` to allow the user `kaa` to create root path for logs.
>To do this, run:
>```
>sudo visudo
>```
>To give full sudo privileges for `kaa` user, add a new line:
>```
>kaa ALL=(ALL:ALL) ALL
>```
{:.important}

To create a file system log appender for your application using the [Administration UI]({{root_url}}Glossary/#administration-ui):

1. Log in to the **Administration UI** page as a [tenant developer]({{root_url}}Glossary/#tenant-developer).

2. Click **Applications** and open the **Log appenders** page of your application.
Click **Add log appender**.

3. On the **Log appender details** page, enter the necessary information and set the **Type** field to **File**.
	
	![File system log appender](attach/file-system-log-appender.png)

4. Fill in the **Configuration** section for your log appender and click **Add**.
See [Configure log appender](#configure-log-appender).

Alternatively, you can use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Logging/editLogAppender) to create or edit your log appender.

The following example illustrates how to create an instance of file system log appender using the server REST API.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d @fileSystemLogAppender.json "http://localhost:8080/kaaAdmin/rest/api/logAppender" | python -mjson.tool
```

where file `fileSystemLogAppender.json` contains the following data.

```json
{
    "pluginClassName":"org.kaaproject.kaa.server.appenders.file.appender.FileSystemLogAppender",
    "pluginTypeName":"File",
    "applicationId":"5",
    "applicationToken":"82635305199158071549",
    "name":"Sample File system log appender",
    "description":"Sample File system log appender",
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
    "jsonConfiguration":"{\"publicKey\":\"public Key\",\"logsRootPath\":\"/kaa_log_uploads\",\"rollingFileNamePatern\":\"logFile.%d{yyyy-MM-dd}.log\",\"rollingMaxHistory\":30,\"triggerMaxFileSize\":\"1GB\",\"encoderPattern\":\"%-4relative [%thread] %-5level %logger{35} - %msg%n\"}"
}
```

Below is an example result.

```json
{
    "applicationId":"5",
    "applicationToken":"82635305199158071549",
    "confirmDelivery":true,
    "createdTime":1466154396923,
    "createdUsername":"devuser",
    "description":"Sample File system log appender",
    "headerStructure":[
        "KEYHASH",
        "VERSION",
        "TIMESTAMP",
        "TOKEN",
        "LSVERSION"
    ],
    "id":"131072",
    "jsonConfiguration":"{\"publicKey\":\"public Key\",\"logsRootPath\":\"/kaa_log_uploads\",\"rollingFileNamePatern\":\"logFile.%d{yyyy-MM-dd}.log\",\"rollingMaxHistory\":30,\"triggerMaxFileSize\":\"1GB\",\"encoderPattern\":\"%-4relative [%thread] %-5level %logger{35} - %msg%n\"}",
    "maxLogSchemaVersion":2147483647,
    "minLogSchemaVersion":1,
    "name":"Sample File system log appender",
    "pluginClassName":"org.kaaproject.kaa.server.appenders.file.appender.FileSystemLogAppender",
    "pluginTypeName":"File",
    "tenantId":"1"
}
```

## Configure log appender

The configuration of file system log appender must match [this Avro schema]({{github_url}}server/appenders/file-appender/src/main/avro/file-appender-config.avsc).

Use the following parameters to configure your file system log appender.

|Name                   |Description                    |
|-----------------------|-------------------------------|
|`publicKey`              |Name of public key.             |
|`logsRootPath`           |Root path for logs.             |
|`rollingFileNamePatern`  |Pattern for creating file name. |
|`rollingMaxHistory`      |Maximum number of records in file. |
|`triggerMaxFileSize`     |Maximum file size of file.          |
|`encoderPattern`         |Encoder pattern.               |

Below is an example configuration that matches the mentioned Avro schema.

```json
{
    "publicKey":"public Key",
    "logsRootPath":"/kaa_log_uploads",
    "rollingFileNamePatern":"logFile.%d{yyyy-MM-dd}.log",
    "rollingMaxHistory":30,
    "triggerMaxFileSize":"1GB",
    "encoderPattern":"%-4relative [%thread] %-5level %logger{35} - %msg%n"
}
```


## Playing with file system log appender

The example below uses the **Data collection demo** from [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox).

To play around with the file system log appender:

1. Open the Data collection demo from Kaa Sandbox, follow the application installation instructions, and run the application.

2. Create a file system log appender as described [above](#create-file-system-log-appender).

3. Your running Data collection demo application will display the output similar to the example below.

		Data collection demo started
		Received new sample period: 1
		Sampled temperature 28 1474622330
		Sampled temperature 31 1474622331
		Sampled temperature 32 1474622332
		Sampled temperature 30 1474622333
		Sampled temperature 28 1474622334
		...
		
4. Verify that your logs have been persisted to the local file system.
Go to the Sandbox VM and open the file  `/kaa_log_uploads/tenant_<number_of_tenant>/application_<your_application_token>/application.log`.
This file should contain the following data.

		
		643854 [EPS-log-dispatcher-10] INFO  1.24212667430286144698 - {"Log Header": "{"endpointKeyHash":{"string":"tqoeo8S49HgakOV/2DfiEZLjGls="},"applicationToken":{"string":"24212667430286144698"},"headerVersion":{"int":1},"timestamp":{"long":1474622333932},"logSchemaVersion":{"int":2}}", "Event": {"temperature":28,"timeStamp":1474622330}}
		643854 [EPS-log-dispatcher-10] INFO  1.24212667430286144698 - {"Log Header": "{"endpointKeyHash":{"string":"tqoeo8S49HgakOV/2DfiEZLjGls="},"applicationToken":{"string":"24212667430286144698"},"headerVersion":{"int":1},"timestamp":{"long":1474622333932},"logSchemaVersion":{"int":2}}", "Event": {"temperature":31,"timeStamp":1474622331}}
		643854 [EPS-log-dispatcher-10] INFO  1.24212667430286144698 - {"Log Header": "{"endpointKeyHash":{"string":"tqoeo8S49HgakOV/2DfiEZLjGls="},"applicationToken":{"string":"24212667430286144698"},"headerVersion":{"int":1},"timestamp":{"long":1474622333932},"logSchemaVersion":{"int":2}}", "Event": {"temperature":32,"timeStamp":1474622332}}
		643854 [EPS-log-dispatcher-10] INFO  1.24212667430286144698 - {"Log Header": "{"endpointKeyHash":{"string":"tqoeo8S49HgakOV/2DfiEZLjGls="},"applicationToken":{"string":"24212667430286144698"},"headerVersion":{"int":1},"timestamp":{"long":1474622333932},"logSchemaVersion":{"int":2}}", "Event": {"temperature":30,"timeStamp":1474622333}}
		643854 [EPS-log-dispatcher-10] INFO  1.24212667430286144698 - {"Log Header": "{"endpointKeyHash":{"string":"tqoeo8S49HgakOV/2DfiEZLjGls="},"applicationToken":{"string":"24212667430286144698"},"headerVersion":{"int":1},"timestamp":{"long":1474622333932},"logSchemaVersion":{"int":2}}", "Event": {"temperature":28,"timeStamp":1474622334}}
		...
		
If you don't get the desired output or experience other problems, see [Troubleshooting]({{root_url}}Administration-guide/Troubleshooting).
