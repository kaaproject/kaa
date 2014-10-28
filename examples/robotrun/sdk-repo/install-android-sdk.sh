#
# Copyright 2014 CyberVision, Inc.
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

#!/bin/bash

artifact_file=$(readlink -f $1)

if [ `uname -o` = "Cygwin" ]
then
    artifact_file=$(cygpath -aw $artifact_file)
fi

cd ..
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                         -Dfile="$artifact_file" -DgroupId=org.kaaproject.kaa.examples.robotrun \
                         -DartifactId=android-sdk -Dversion=0.0.1-SNAPSHOT \
                         -Dpackaging=jar -DlocalRepositoryPath=sdk-repo