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

RUN_DIR=`pwd`

function help {
    echo "Choose one of the following: {build|run|deploy|clean}"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

LIBS_PATH="libs"
KAA_LIB_PATH="$LIBS_PATH/kaa"
BUILD_DIR="build"
PROJECT_HOME=$(pwd)
APP_NAME="sample_c_client"

function prepare_build {
    mkdir -p build;
    cd $KAA_LIB_PATH && ./build.sh build && cd $BUILD_DIR && cp libkaa* "$PROJECT_HOME/$BUILD_DIR/" && cd $PROJECT_HOME
    cd $BUILD_DIR && cmake -DAPP_NAME=$APP_NAME .. && cd $PROJECT_HOME
}

function build {
    cd $BUILD_DIR && make && cd $PROJECT_HOME
}

function clean {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf $BUILD_DIR
}

function run {
    cd $PROJECT_HOME/$BUILD_DIR
    ./$APP_NAME
}

for cmd in $@
do

case "$cmd" in
    build)
        prepare_build &&
        build
    ;;

    run)
        run
    ;;

    deploy)
        clean
        prepare_build
        build
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