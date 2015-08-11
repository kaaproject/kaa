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

function help {
    echo "Choose one of the following: {build|run|deploy|clean}"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

APP_NAME="demo_client"
PROJECT_HOME=$(pwd)
BUILD_DIR="build"
LIBS_PATH="libs"
KAA_LIB_PATH="$LIBS_PATH/kaa"
KAA_C_LIB_HEADER_PATH="$KAA_LIB_PATH/src"
KAA_CPP_LIB_HEADER_PATH="$KAA_LIB_PATH/kaa"
KAA_SDK_TAR="kaa-client*.tar.gz"
ESPTOOL="esptool.py"

#Wifi settings
SSID="xxx"
PASSWORD="xxxxxxxx"

function build_thirdparty {
    if [[ ! -d "$KAA_C_LIB_HEADER_PATH" &&  ! -d "$KAA_CPP_LIB_HEADER_PATH" ]]
    then
        KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

        if [ -z "$KAA_SDK_TAR_NAME" ]
        then
            echo "Please, put the generated C/C++ SDK tarball into the libs/kaa folder and re-run the script."
            exit 1
        fi

        mkdir -p $KAA_LIB_PATH &&
        tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
    fi

    if [ ! -d "$KAA_LIB_PATH/$BUILD_DIR" ]
    then
        cd $KAA_LIB_PATH &&
        mkdir -p $BUILD_DIR && cd $BUILD_DIR &&
        cmake -DKAA_DEBUG_ENABLED=0 \
              -DKAA_MAX_LOG_LEVEL=4 \
              -DKAA_PLATFORM=esp8266 \
              -DCMAKE_TOOLCHAIN_FILE=../toolchains/esp8266.cmake \
              -DKAA_WITHOUT_EVENTS=1 \
              -DKAA_WITHOUT_NOTIFICATION=1 \
              ..
    fi

    cd "$PROJECT_HOME/$KAA_LIB_PATH/$BUILD_DIR"
    echo `ls`
    make -j4 &&
    cd $PROJECT_HOME
}

function build_app {
    echo "Enter WiFi SSID:"
    read SSID
    echo "Enter WiFi Password:"
    read PASSWORD
    cd $PROJECT_HOME &&
    mkdir -p "$PROJECT_HOME/$BUILD_DIR" &&
    cp "$KAA_LIB_PATH/$BUILD_DIR/"libkaa* "$PROJECT_HOME/$BUILD_DIR/" &&
    cd $BUILD_DIR &&
    cmake -DAPP_NAME=$APP_NAME -DCMAKE_TOOLCHAIN_FILE=esp8266.cmake \
          -DSSID=$SSID -DPWD=$PASSWORD ..
    make
}

function clean {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

function run {
    cd "$PROJECT_HOME/$BUILD_DIR"
    sudo $ESPTOOL write_flash 0x00000 0x00000.bin 0x40000 0x40000.bin
}

for cmd in $@
do

case "$cmd" in
    build)
        build_thirdparty &&
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        build_thirdparty
        build_app
        run
        ;;

    clean)
        clean
    ;;
    
    *)
        help
    ;;
esac

done
