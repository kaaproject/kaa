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

adminDaoProperties="/etc/kaa-node/conf/admin-dao.properties"
sqlDaoProperties="/etc/kaa-node/conf/sql-dao.properties"
noSqlDaoProperties="/etc/kaa-node/conf/nosql-dao.properties"

username=$(grep "jdbc_username" $sqlDaoProperties | cut -d "=" -f2)
password=$(grep "jdbc_password" $sqlDaoProperties | cut -d "=" -f2)
host=$(grep "jdbc_host_port" $sqlDaoProperties | cut -d "=" -f2 | cut -d ":" -f1)
dbName=$(grep "db_name" $sqlDaoProperties | cut -d "=" -f2)
nosql=$(grep "nosql_db_provider_name" $noSqlDaoProperties | cut -d "=" -f2)
driverClassName=$(grep "jdbc_driver_className" $sqlDaoProperties | cut -d "=" -f2)
jdbcUrl=$(grep "jdbc_url" $adminDaoProperties | cut -d "=" -f2)

echo "Stop kaa-node service"
sudo service kaa-node stop

echo "Sleep 10 sec"
sleep 10

echo "Install kaa-node.deb 0.10.0 version"
sudo dpkg -i kaa-node.deb

echo "Upgrade databases data"
java -jar data-migration_0.9.0-0.10.0.jar -u $username -p $password -h $host -db $dbName -nosql $nosql -driver $driverClassName -url $jdbcUrl

echo "Start kaa-node"
sudo service kaa-node start

echo "Upgrade finished!"
