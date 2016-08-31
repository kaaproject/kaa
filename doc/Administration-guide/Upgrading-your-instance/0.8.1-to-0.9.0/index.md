---
layout: page
title: 0.8.1 to 0.9.0
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

This guide describes how to upgrade Kaa server 0.8.1 to the new Kaa server 0.9.0 version.

>**Important note:**
>
>This script was tested only on the official Kaa Sandbox 0.8.1.
>
>Please, make sure you read the following disclaimer before using the instructions to run the script.

>**Caution:**
>
> IMPORTANT: READ BEFORE USING THIS SCRIPT
>
> This code is experimental and does not guarantee any particular results or outcomes. WE RECOMMEND THAT YOU SHOULD NOT USE THIS CODE FOR PRODUCTION INSTANCES
OF THE KAA IOT PLATFORM UNTIL YOU HAVE COMPLETED SUFFICIENT AMOUNT OF VALIDATION AGAINST YOUR DEVELOPMENT AND TESTING INSTANCES. Please note that this code is
licensed under Apache License Version 2.0, which governs the terms and conditions for its use, reproduction, and distribution, and states the following:
>
> “7. Disclaimer of Warranty. Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its
Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or
conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness
of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.”
>
> “8. Limitation of Liability. In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by
applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any
direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use
the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages
or losses), even if such Contributor has been advised of the possibility of such damages.”

Follow these steps to run the script:

1. Go to your Kaa host machine.
2. Download the upgradeKaa081to090.sh, upgradeMongo081to090KaaVersion.js, upgradePostgresql081to090kaaVersion.sql files from
[here](https://github.com/kaaproject/kaa/raw/master/server/upgrade/data-migration-0.8.1-0.9.0).
3. Download the Kaa 0.9.0 debian package at [Download Kaa](http://www.kaaproject.org/download-kaa/) page.
4. Files downloaded at step 2 and 3 should be located in the same directory.
5. Execute the command below:

```bash
 sudo ./upgradeKaa081to090.sh
```

>**Note:**
>
>If needed enter the password to the PostgreSQL database and confirm modification of configuration files located at /usr/lib/kaa-node/conf/ directory by
entering "Y" during the script execution.
