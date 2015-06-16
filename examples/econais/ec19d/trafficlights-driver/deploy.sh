#
# Copyright 2014-2015 CyberVision, Inc.
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

KAA_LIB_PATH="libs/kaa"
KAA_C_SOURCES_PATH="$KAA_LIB_PATH/src"
KAA_SDK_TAR="kaa-client*.tar.gz"


function unpackSources {
    if [[ ! -d "$KAA_C_SOURCES_PATH" ]]
    then
        cd $KAA_LIB_PATH && 
        KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

        if [ -z "$KAA_SDK_TAR_NAME" ]
        then
            echo "Please, put the generated C SDK tarball into the libs/kaa folder and re-run the script."
            exit 1
        fi

        tar -zxf $KAA_SDK_TAR_NAME && cp -r ./src/kaa ../../src && cd ../../
    fi
}

function copySources {
    
    current_folder_name=${PWD##*/}
    if [ ! -d "$ECONAIS_HOME_VAR/applications" ]
    then
        echo "Can't find application folder. Check the path to the root Econais folder..."
        exit 1
    fi
    mkdir -p "$ECONAIS_HOME_VAR/applications/$current_folder_name"
    cp -r ./src "$ECONAIS_HOME_VAR/applications/$current_folder_name"
    cp -r ./cfg "$ECONAIS_HOME_VAR/applications/$current_folder_name"
    
}

if [ -z ${ECONAIS_HOME_VAR+x} ]; then 
    echo "Set ECONAIS_HOME_VAR to the valid Econais root directory!";
    exit 1;
fi

if [ ! -d "$ECONAIS_HOME_VAR" ]; then
    echo "Econais root directory '$ECONAIS_HOME_VAR' doesn't exist!";
    exit 1;
fi

unpackSources;
copySources;
