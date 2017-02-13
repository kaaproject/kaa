---
layout: page
title: 0.8.1 to 0.9.0
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

This guide describes how to upgrade your [Kaa server]({{root_url}}Glossary/#kaa-server) from version 0.8.1 to 0.9.0.

>**CAUTION:**
>
>The below guide was only tested in a single-node environment with the official Kaa Sandbox 0.8.1 running on Ubuntu Linux.
>Please read the following disclaimer before running the script.
>
>This code is experimental and does not guarantee any particular results or outcomes.
>WE RECOMMEND THAT YOU SHOULD NOT USE THIS CODE FOR PRODUCTION INSTANCES OF THE KAA IOT PLATFORM UNTIL YOU HAVE COMPLETED SUFFICIENT AMOUNT OF VALIDATION AGAINST YOUR DEVELOPMENT AND TESTING INSTANCES.
>Please note that this code is licensed under Apache License Version 2.0, which governs the terms and conditions for its use, reproduction, and distribution, and states the following:
>
>“7. Disclaimer of Warranty.
>Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
>You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.”
>
>“8. Limitation of Liability.
>In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses), even if such Contributor has been advised of the possibility of such damages.”
{:.caution}

Follow these steps to run the upgrade script:

1. Go to your Kaa host machine.
2. Download the [upgradeKaa081to090.sh]({{github_url_raw}}server/upgrade/data-migration-0.8.1-0.9.0/upgradeKaa081to090.sh), [upgradeMongo081to090KaaVersion.js]({{github_url_raw}}server/upgrade/data-migration-0.8.1-0.9.0/upgradeMongo081to090KaaVersion.js), and [upgradePostgresql081to090kaaVersion.sql]({{github_url_raw}}server/upgrade/data-migration-0.8.1-0.9.0/upgradePostgresql081to090kaaVersion.sql) files.
3. Download the Kaa 0.9.0 debian package at [Download Kaa](http://www.kaaproject.org/download-kaa/) page.
4. Ensure that the files downloaded in steps 2 and 3 above are located in the same directory.
5. Execute the command below:

```bash
sudo ./upgradeKaa081to090.sh
```

>**NOTE:** If needed, enter the password to the PostgreSQL database and confirm modification of configuration files located in the `/usr/lib/kaa-node/conf/` directory by entering "Y" during the script execution.
{:.note}
