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

help() {
    echo "Choose one of the following: {build|install|test|analyze|clean}"
    exit 1
}

if [ $# -eq 0 ]; then
    help
fi

if [ -z "${MAX_LOG_LEVEL+x}" ]; then
    MAX_LOG_LEVEL=6
fi

prepare_build() {
    mkdir -p build-posix
    cd build-posix
    cmake -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=$MAX_LOG_LEVEL -DKAA_UNITTESTS_COMPILE=1 -DKAA_COLLECT_COVERAGE=1 -DKAA_ENCRYPTION=1 .. -DCMAKE_C_FLAGS="-Werror"
    cd ..
}

build() {
    cd build-posix
    make
    cd ..
}

run_analysis() {
    cd build-posix
    ctest -T memcheck
    ctest -T coverage
    make cppcheck
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

for cmd in "$@"; do

case "$cmd" in
    build)
        prepare_build
        build
    ;;

    install)
        cd build-posix && make install && cd ..
    ;;

    test)
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
