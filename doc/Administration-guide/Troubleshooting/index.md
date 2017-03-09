---
layout: page
title: Troubleshooting
permalink: /:path/
sort_idx: 888888880
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to resolve some common issues while using Kaa as well as how to report issues to the Kaa crew for getting help.

## Change service logging level

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console">Console</a></li>
</ul>

<div class="tab-content"><div id="Sandbox-web-ui" class="tab-pane fade in active" markdown="1">

Go to Kaa Sandbox web UI and select **Management** menu item in the upper right corner.
In the **Kaa server logs** section you will see combo-box with current log-level for server logs.

<img src="attach/managment_tab.png" width="800" height="500">

Select the required log level and click the **Update** button.
If you want to delete old log files, mark the **clean up old logfiles** checkbox.

</div><div id="Console" class="tab-pane fade" markdown="1">

1. Connect to your [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox) via ssh:

   ```bash
   $ ssh kaa@<YOUR-SANDBOX-IP>
   password: kaa
   ```

2. Stop the Kaa service.

   ```bash
   $ sudo service kaa-node stop
   ```

3. Change the log level for the Kaa service.

   ```bash
   $ sudo nano /usr/lib/kaa-node/conf/logback.xml
   ```

    In the lines:

   ```bash
   <logger name="org.kaaproject.kaa" level="INFO"/>
   <logger name="org.kaaproject.kaa.server.common.Environment" level="INFO"/>
   ```

    replace "INFO" with the required log level, for example "TRACE";
    then save the changes: press **Ctrl+X**, enter **y** in the dialog line and press **Enter**.

4. Start the Kaa service.

   ```bash
   $ sudo service kaa-node start
   ```

</div></div>

### Supported log levels

Use the following log levels according to the log purpose.

| Log level | Description                                                                                                                       |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------|
| OFF       | Turns off logging                                                                                                                 |
| ERROR     | Logs run time errors or unexpected conditions that the program can gracefully recover from.                                        |
| WARNING   | Logs same as previous, plus unexpected or undesirable run time conditions that are not necessarily affecting the program.          |
| INFO      | Logs same as previous, plus important or interesting run time events that help understand what the program is doing at the moment. |
| DEBUG     | Logs same as previous, plus detailed information according to the logical work flow of the system.                                 |
| TRACE     | Logs same as previous, plus the most detailed information intended for development and debugging purposes only.                    |

## Clear Kaa logs

1. Connect to your Kaa Sandbox via ssh.

   ```bash
   $ ssh kaa@<YOUR-SANDBOX-IP>
   password: kaa
   ```

2. Stop the Kaa service.

   ```bash
   $ sudo service kaa-node stop
   ```

3. Clear the Kaa logs.

   ```bash
   $ sudo rm -rf /var/log/kaa/*
   ```

4. Start the Kaa service.

   ```bash
   $ sudo service kaa-node start
   ```

## Download Kaa logs from Sandbox

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui2">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console2">Console</a></li>
</ul>

<div class="tab-content"><div id="Sandbox-web-ui2" class="tab-pane fade in active" markdown="1">

Go to Kaa Sandbox web UI and in a upper right corner select **Management** menu item.
Click the **Download logs** button in the **Kaa server logs** section.

<img src="attach/managment_tab.png" width="800" height="500">

To restart Kaa service:

1. Connect to your Kaa Sandbox via ssh.

   ```bash
   $ ssh kaa@<YOUR-SANDBOX-IP>
   password: kaa
   ```

2. Restart Kaa service.

   ```bash
   $ sudo service kaa-node restart
   ```

</div><div id="Console2" class="tab-pane fade" markdown="1">

The Kaa service logs can be found in the `/var/log/kaa` directory where `kaa-node.*` files contain log information from the Kaa service component.

You can download logs from the Sandbox guest machine to the host machine as follows:

<ul>
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Linux">Linux</a></li>
  <li><a data-toggle="tab" href="#Windows">Windows</a></li>
</ul>

<div class="tab-content"><div id="Linux" class="tab-pane fade in active" markdown="1">

If the host OS is Linux, run the following command.

```bash
$ scp kaa@<YOUR-SANDBOX-IP>:/var/log/kaa/* /home/<YOUR-HOST-USERNAME>/
```

To restart Kaa service:

1. Connect to your Kaa Sandbox via ssh.

   ```bash
   $ ssh kaa@<YOUR-SANDBOX-IP>
   password: kaa
   ```

2. Restart Kaa service.

   ```bash
   $ sudo service kaa-node restart
   ```

</div><div id="Windows" class="tab-pane fade" markdown="1">

If the host OS is Windows, do the following:

1. Install WinSCP.

2. Connect to *YOUR-SANDBOX-IP* via WinSCP.
User: **kaa**.
Password: **kaa**.

3. Copy logs from `/var/log/kaa/` to your PC.

To restart Kaa service:

1. Connect to your Kaa Sandbox via ssh.

   ```bash
   $ ssh kaa@<YOUR-SANDBOX-IP>
   password: kaa
   ```

2. Restart Kaa service.

   ```bash
   $ sudo service kaa-node restart
   ```

</div></div>
</ul>
</div></div>

## Errors

### Error when assembling binary files for Kaa application demo from Sandbox

| Error description                                                                                     | Possible cause                                                                   | Solution                                                                                                                           | Related documentation    |
|-------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|--------------------------|
| Unexpected error occurred: 500 Server ErrorUnexpected service error occurred: 500 Server ErrorFailed! | By default, the Kaa Sandbox components are not accessible from the host network. | Change the Sandbox host/IP on web UI or execute the following script in the Sandbox: `$ sudo /usr/lib/kaa-sandbox/change_kaa_host.sh` | [Kaa Sandbox - Networking]({{root_url}}Getting-started/#networking) |

## Report issues to Kaa crew

If you cannot resolve an issue, you can [report it]({{root_url}}How-to-contribute/Jira-flow/) or [seek assistance from Kaa community]({{root_url}}How-to-contribute/).