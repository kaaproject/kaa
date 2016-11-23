#!/bin/bash
#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cat <<-EOF
--------------------------------------------------------------------------
IMPORTANT: READ BEFORE USING THIS SCRIPT

This code is experimental and does not guarantee any particular results
or outcomes.

WE RECOMMEND THAT YOU SHOULD NOT USE THIS CODE FOR PRODUCTION INSTANCES
OF THE KAA IOT PLATFORM UNTIL YOU HAVE COMPLETED SUFFICIENT AMOUNT OF
VALIDATION AGAINST YOUR DEVELOPMENT AND TESTING INSTANCES.

Please note that this code is licensed under Apache License Version 2.0,
which governs the terms and conditions for its use, reproduction,
and distribution, and states the following:

“7. Disclaimer of Warranty. Unless required by applicable law or agreed
to in writing, Licensor provides the Work (and each Contributor provides
its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
OF ANY KIND, either express or implied, including, without limitation,
any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY,
or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for
determining the appropriateness of using or redistributing the Work and
assume any risks associated with Your exercise of permissions under this
License.
8. Limitation of Liability. In no event and under no legal theory, whether
in tort (including negligence), contract, or otherwise, unless required by
applicable law (such as deliberate and grossly negligent acts) or agreed
to in writing, shall any Contributor be liable to You for damages,
including any direct, indirect, special, incidental, or consequential
damages of any character arising as a result of this License or out of
the use or inability to use the Work (including but not limited to damages
for loss of goodwill, work stoppage, computer failure or malfunction, or
any and all other commercial damages or losses), even if such Contributor
has been advised of the possibility of such damages.”
--------------------------------------------------------------------------
EOF

while true; do
    read -p "Do you wish to proceed with upgrade? (yes / no) " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

echo "Stop kaa-node service"
sudo service kaa-node stop

scriptDirectory="$(dirname "$(readlink -f ${BASH_SOURCE[0]})")"

sudo cp /usr/lib/kaa-node/conf/dao.properties $scriptDirectory
sudo cp /usr/lib/kaa-node/conf/admin-dao.properties $scriptDirectory

file="$scriptDirectory/dao.properties"
jdbc_host=$(grep "jdbc_host" $file | cut -d "=" -f2)
jdbc_port=$(grep "jdbc_port" $file | cut -d "=" -f2)
new="jdbc_host_port=$jdbc_host:$jdbc_port"
old="jdbc_host=.*"
sed -i "s@$old@$new@" $file
echo "#specify jdbc database provider name" >> $file
echo "sql_provider_name=postgresql" >> $file

echo "Upgrade postgresql 0.8.1 to 0.9.0 Kaa version"
sudo -u postgres psql -d kaa -a -f upgradePostgresql081to090kaaVersion.sql

echo "Upgrade mongo 0.8.1 to 0.9.0 Kaa version"
mongo < upgradeMongo081to090KaaVersion.js

echo "Install kaa-node.deb"
sudo dpkg -i kaa-node.deb

echo "Copy sql-dao.properties to /usr/lib/kaa-node/conf/"
sudo cp $scriptDirectory/dao.properties /usr/lib/kaa-node/conf/sql-dao.properties
echo "Copy admin-dao.properties to /usr/lib/kaa-node/conf/"
sudo cp $scriptDirectory/admin-dao.properties /usr/lib/kaa-node/conf/admin-dao.properties

echo "Download posgresql jdbc driver"
sudo wget -P /usr/lib/kaa-node/lib/ https://jdbc.postgresql.org/download/postgresql-9.3-1103.jdbc3.jar

echo "Start kaa-node service"
sudo service kaa-node start

echo "Upgrade finished!"
