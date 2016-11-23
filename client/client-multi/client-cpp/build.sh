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

set -e

help() {
    echo "Choose one of the following: {build|install|test|clean}"
    exit 1
}

RUN_DIR=$(pwd)
TEST_DIR=$RUN_DIR/test
TEST_RESULT=1
TEST_BUILD_FAILED=1

# Temporary solution
if [ ! -x "$RUN_DIR/gcovr" ]
then
    chmod +x "$RUN_DIR/gcovr"
fi

measure_coverage() {
    echo "Collecting coverage metrics..."
    cd "$RUN_DIR" && ./gcovr -d -x -f "($RUN_DIR).*" -e ".*(test|kaa/gen).*" -o "$RUN_DIR/gcovr-report.xml" -v > "$RUN_DIR/gcovr.log"
}

run_tests() {
    cd build && make -j4 && TEST_BUILD_FAILED=0 && ./kaatest && TEST_RESULT=0 # --report_level=detailed --report_format=xml 2>"$RUN_DIR/unittest_result.xml" && TEST_RESULT=0
}

test_cleanup() {
    echo "Cleaning up..."
    cd "$TEST_DIR/build" && make clean
    echo "Cleanup done."
}

if [ $# -eq 0 ]
then
    help
fi

mkdir -p build; (cd build && cmake -DKAA_WITH_SQLITE_LOG_STORAGE=1 ..)

for cmd in "$@"
do

case "$cmd" in
    build)
    (cd build && make -j4)
    ;;

    install)
    (cd build && make install)
    ;;

    clean)
    (cd build && make clean)
    test_cleanup
    ;;

    test)
    cd "$TEST_DIR"
    mkdir -p build; (cd build && cmake ..)
    run_tests
    if [ $TEST_BUILD_FAILED -eq 0 ]
    then
        measure_coverage
    fi
    test_cleanup
    if [ $TEST_RESULT -ne 0 ]
    then
        echo "Kaa C++ SDK unittests have failed!"
        exit $TEST_RESULT
    else
        echo "Kaa C++ SDK unittests have successfully passed!"
    fi
    ;;

    *)
    help
    ;;
esac

done
