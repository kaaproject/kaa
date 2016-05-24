#!/bin/sh
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

# Exits immediately if error occurs
set -e

RUN_DIR=`pwd`

help() {
    echo "Choose one of the following: {build|install|test|analyze|clean}"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

if [ -z ${MAX_LOG_LEVEL+x} ]
then
    MAX_LOG_LEVEL=6
fi

BUILD_TYPE="Debug"
UNITTESTS_COMPILE=0
COLLECT_COVERAGE=0

prepare_build() {
    mkdir -p build-posix
    cd build-posix
    cmake -DCMAKE_BUILD_TYPE=$BUILD_TYPE -DKAA_MAX_LOG_LEVEL=$MAX_LOG_LEVEL -DKAA_UNITTESTS_COMPILE=$UNITTESTS_COMPILE -DKAA_COLLECT_COVERAGE=$COLLECT_COVERAGE -DKAA_CPPCHECK=1 .. -DCMAKE_C_FLAGS="-Werror"
    cd ..
}

build() {
    cd build-posix
    make
    cd ..
}

run_rats() {
    echo "Starting RATS..."
    rats --xml `find src/ -name *.[ch]` > build-posix/rats-report.xml
    echo "RATS analysis finished."
}

run_analysis() {
    cd build-posix
    ctest -T memcheck
    ctest -T coverage
    make cppcheck
    if hash rats 2>/dev/null
    then
        run_rats
    fi
    cd ..
}

run_tests() {
    cd build-posix
    ctest --output-on-failure .
    cd ..
}

clean() {
    if [ -d build-posix ]
    then
        cd build-posix
        if [ -f Makefile ]
        then
            make clean
        fi
        cd .. && rm -r build-posix
    fi
}

for cmd in $@
do

case "$cmd" in
    build)
        COLLECT_COVERAGE=0
        UNITTESTS_COMPILE=0
        prepare_build
        build
    ;;

    install)
        cd build-posix && make install && cd ..
    ;;

    test)
        COLLECT_COVERAGE=1
        UNITTESTS_COMPILE=1
        prepare_build
        build
        run_tests
    ;;

    analyze)
        run_analysis
    ;;

    clean)
        clean
    ;;

    *)
        help
    ;;
esac

done
