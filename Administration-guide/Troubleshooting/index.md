---
layout: page
title: Troubleshooting
permalink: /:path/
sort_idx: 888888880
---
{% include variables.md %}

- [How to's](#how-tos)
  - [How to change the service logging level](#how-to-change-the-service-logging-level)
    - [Supported log levels](#supported-log-levels)
  - [How to clear the Kaa logs](#how-to-clear-the-kaa-logs)
  - [How to download the Kaa logs from the Sandbox](#how-to-download-the-kaa-logs-from-the-sandbox)
- [Errors](#errors)
  - [ERROR when assembling binary for a Kaa applicaton demo from the Sandbox](#error-when-assembling-binary-for-a-kaa-applicaton-demo-from-the-sandbox)
- [Reporting issues to the Kaa Crew](#reporting-issues-to-the-kaa-crew)

This guide explains how to resolve some common issues while using Kaa as well as how to report issues to the Kaa Crew for getting help.

# How to's

## How to change the service logging level

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console">Console</a></li>
</ul>

<div class="tab-content">

<div id="Sandbox-web-ui" class="tab-pane fade in active" markdown="1">

Go to Kaa Sandbox web UI and select "Management" menu item in the upper right corner. In the "Kaa server logs" section you will see combo-box with current log-level for server logs.

<img src="attach/managment_tab.png" width="800" height="500">

Select the required log level and click the "Update" button. If you want to delete old log files, mark the "clean up old logfiles" checkbox.

</div><div id="Console" class="tab-pane fade" markdown="1">

1. Connect to your Kaa Sandbox via ssh:

```
    $ ssh kaa@<YOUR-SANDBOX-IP>   
    password: kaa
```

2. Stop the Kaa service:

```
    $ sudo service kaa-node stop
```

3. Change the log level for the Kaa service:

```
    $ sudo nano /usr/lib/kaa-node/conf/logback.xml
```

In the lines:

```
    <logger name="org.kaaproject.kaa" level="INFO"/>
    <logger name="org.kaaproject.kaa.server.common.Environment" level="INFO"/>
```

replace "INFO" with the required log level, for example "TRACE";
then save the changes: press Ctrl+X, enter "y" in a dialog line and press Enter.

4. Start the Kaa service:

```
    $ sudo service kaa-node start
```

</div></div>

### Supported log levels

Use the following log levels according to the log purpose.

| Log level | Description                                                                                                                       |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------|
| OFF       | Turns off logging                                                                                                                 |
| ERROR     | Logs run time errors or unexpected conditions that the program can gracefully recover from                                        |
| WARNING   | Logs same as previous, plus unexpected or undesirable run time conditions that are not necessarily affecting the program          |
| INFO      | Logs same as previous, plus important or interesting run time events that help understand what the program is doing at the moment |
| DEBUG     | Logs same as previous, plus detailed information according to the logical work flow of the system                                 |
| TRACE     | Logs same as previous, plus the most detailed information intended for development and debugging purposes only                    |

## How to clear the Kaa logs

1. Connect to your Kaa Sandbox via ssh:

```
    $ ssh kaa@<YOUR-SANDBOX-IP>
    password: kaa
```

2. Stop the Kaa service:

```
    $ sudo service kaa-node stop
```

3. Clear the Kaa logs:

```
    $ sudo rm -rf /var/log/kaa/*
```

4. Start the Kaa service:

```
    $ sudo service kaa-node start
```

## How to download the Kaa logs from the Sandbox

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui2">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console2">Console</a></li>
</ul>

<div class="tab-content">

<div id="Sandbox-web-ui2" class="tab-pane fade in active" markdown="1">

Go to Kaa Sandbox web UI and in a upper right corner select "Management" menu item. Click the "Download logs" button in the "Kaa server logs" section.

<img src="attach/managment_tab.png" width="800" height="500">

How to restart Kaa service

1. Connect to your Kaa Sandbox via ssh:

```
$ ssh kaa@<YOUR-SANDBOX-IP>
password: kaa
```

2. Restart Kaa service:

```
$ sudo service kaa-node restart
```

</div><div id="Console2" class="tab-pane fade" markdown="1">

The Kaa service logs can be found under /var/log/kaa:

- kaa-node.* files contain log information from the Kaa service component.

You can download logs from the Sandbox guest machine to the host machine as follows:

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Linux">Linux</a></li>
  <li><a data-toggle="tab" href="#Windows">Windows</a></li>
</ul>

<div class="tab-content">

<div id="Linux" class="tab-pane fade in active" markdown="1">

If the host OS is Linux, run the following in the terminal:

```
  $ scp kaa@<YOUR-SANDBOX-IP>:/var/log/kaa/* /home/<YOUR-HOST-USERNAME>/
```

How to restart Kaa service

1. Connect to your Kaa Sandbox via ssh:

```
    $ ssh kaa@<YOUR-SANDBOX-IP>
      password: kaa
```

2. Restart Kaa service:

```
    $ sudo service kaa-node restart
```

</div><div id="Windows" class="tab-pane fade" markdown="1">

If the host OS is Windows, do the following:

1. Install WinSCP.
2. Connect to <YOUR-SANDBOX-IP> via WinSCP. User: kaa. Password: kaa.
3. Copy logs from /var/log/kaa/ to your PC.

How to restart Kaa service

1. Connect to your Kaa Sandbox via ssh:

```
    $ ssh kaa@<YOUR-SANDBOX-IP>
    password: kaa
```

2. Restart Kaa service:

```
    $ sudo service kaa-node restart
```

</div></div>

</div></div>

# Errors

## ERROR when assembling binary for a Kaa applicaton demo from the Sandbox

| Error description                                                                                     | Possible cause                                                                   | Solution                                                                                                                           | Related documentation    |
|-------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|--------------------------|
| Unexpected error occurred: 500 Server ErrorUnexpected service error occurred: 500 Server ErrorFailed! | By default, the Kaa Sandbox components are not accessible from the host network. | Change the Sandbox host/IP on web UI or execute the following script in the Sandbox: ```$ sudo /usr/lib/kaa-sandbox/change_kaa_host.sh``` | [Kaa Sandbox - Networking]({{root_url}}Programming-guide/Getting-started/#networking) |

# Reporting issues to the Kaa Crew

You may seek help at the [Kaa official forum](http://www.kaaproject.org/forum/) by participating in existing topic discussions or, if no relevant topic was found, by starting a new topic, describing your issue and attaching logs. Please make sure the issue has not been yet addressed in other topics on the forum or in this guide.

Before sending logs to the Kaa forum follow these steps:

1. [Change the log level for the Kaa service to “TRACE”](#how-to-change-the-service-logging-level).
2. [Clear the Kaa logs](#how-to-clear-the-kaa-logs).
3. Reproduce your issue.
4. [Download the Kaa logs from the Sandbox](#how-to-download-the-kaa-logs-from-the-sandbox).
5. Create an archive file with your logs.
