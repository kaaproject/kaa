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

RUN_DIR=`pwd`

MAIN_SOURCE_NAME=main.cpp
MAKEFILE_NAME=Makefile
PUBLIC_KEY_HEADER=kaa_public_key.h
PREV_SUFFIX=prev

LIBS_PATH="libs"
KAA_LIB_PATH="$LIBS_PATH/kaa"
KAA_C_LIB_HEADER_PATH="$KAA_LIB_PATH/src"
KAA_CPP_LIB_HEADER_PATH="$KAA_LIB_PATH/kaa"
KAA_SDK_TAR="kaa-client*.tar.gz"

function renameOld {
    mv "$MAPPLE_HOME_VAR/$MAIN_SOURCE_NAME" "$MAPPLE_HOME_VAR/$MAIN_SOURCE_NAME.$PREV_SUFFIX"
    mv "$MAPPLE_HOME_VAR/$MAKEFILE_NAME" "$MAPPLE_HOME_VAR/$MAKEFILE_NAME.$PREV_SUFFIX"
}

function generateKey {
    java -jar pub_key_generator.jar $PUBLIC_KEY_HEADER
}

function unpackSources {
    if [[ ! -d "$KAA_C_LIB_HEADER_PATH" &&  ! -d "$KAA_CPP_LIB_HEADER_PATH" ]]
    then
        KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

        if [ -z "$KAA_SDK_TAR_NAME" ]
        then
            echo "Please, put the generated C SDK tarball into the libs/kaa folder and re-run the script."
            exit 1
        fi

        mkdir -p $KAA_LIB_PATH &&
        tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
    fi
}

function copySources {
    mkdir -p "$MAPPLE_HOME_VAR/libraries/"
    ln -sf $(readlink -f $KAA_C_LIB_HEADER_PATH/kaa) $(readlink -f $MAPPLE_HOME_VAR/libraries/kaa)

    cp $MAIN_SOURCE_NAME "$MAPPLE_HOME_VAR/$MAIN_SOURCE_NAME"
    cp $MAKEFILE_NAME "$MAPPLE_HOME_VAR/$MAKEFILE_NAME"
    cp $PUBLIC_KEY_HEADER "$MAPPLE_HOME_VAR/$PUBLIC_KEY_HEADER"
}

if [ -z ${MAPPLE_HOME_VAR+x} ]; then 
    echo "Set MAPPLE_HOME_VAR to the valid Mapple home directory!";
    exit 1;
fi

if [ ! -d "$MAPPLE_HOME_VAR" ]; then
    echo "Mapple home directory '$MAPPLE_HOME_VAR' doesn't exist!";
    exit 1;
fi

renameOld;
generateKey;
unpackSources;
copySources;
