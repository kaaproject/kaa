---
layout: page
title: 0.9.x to 0.10.x
permalink: /:path/
sort_idx: 10
---

This guide describes how to upgrade your [Kaa server]({{root_url}}Glossary/#kaa-server) from version 0.9.0 to 0.10.0.

>**CAUTION:** The below script was only tested on the official Kaa Sandbox 0.9.0.
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

To update your Kaa server from version 0.9.x to 0.10.x:

1. [Download](http://www.kaaproject.org/download-kaa/) the Kaa 0.10.0 Debian package.

2. Download the [upgradeKaa090to0100.sh](https://github.com/kaaproject/kaa/blob/v0.10.0/server/upgrade/data-migration-0.9.0-0.10.0/upgradeKaa090to0100.sh) and [data-migration_0.9.0-0.10.0.jar](http://repository.kaaproject.org/repository/releases/org/kaaproject/kaa/server/upgrade/data-migration/0.10.0/data-migration_0.9.0-0.10.0.jar) to upgrade the databases.

3. Save all downloaded files in the same directory on your Kaa server.

4. Run the following command.

		sudo ./upgradeKaa090to0100.sh
	
>**IMPORTANT:** During installation of the Debian package, the script may ask you to resolve conflicts in the /usr/lib/kaa-node/conf/ folder.
>Be careful when merging configurations as incorrect server settings can cause malfunction of Kaa server.
{:.important}