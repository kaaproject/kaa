---
layout: page
title: 0.9.0 to 0.10.0
permalink: /:path/
sort_idx: 10
---
 This guide describes how to upgrade Kaa server 0.9.0 to the new Kaa server 0.10.0 version.
 
 >**Important note:**
 >
 >This script was tested only on the official Kaa Sandbox 0.9.0.
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
 
 In order to update your Kaa server from 0.9.x to 0.10.x follow next steps:
 
 1. Download the Kaa 0.10.0 debian package at [Download Kaa](http://www.kaaproject.org/download-kaa/) page.
 2. Download the [upgradeKaa090to0100.sh](https://github.com/kaaproject/kaa/blob/v0.10.0/server/upgrade/data-migration-0.9.0-0.10.0/upgradeKaa090to0100.sh) and [data-migration.jar](http://repository.kaaproject.org/repository/releases/org/kaaproject/kaa/server/upgrade/data-migration/0.10.0/db-migration.one-jar.jar) in order to upgrade databases.
 3. Put all downloaded files to the same directory on the Kaa server.
 4. Execute the command below:

```bash
 sudo ./upgradeKaa081to090.sh
```
    
 >**Note:** 
 >
 > During installation of debian package the script may ask you about resolving conflicts in the /usr/lib/kaa-node/conf/ folder,
 > be attentive about merging configurations because incorrect server setting can lead the Kaa server to malfunction.