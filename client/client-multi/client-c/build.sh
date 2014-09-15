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
    echo "Choose one of the following: {build|install|test|clean}"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

mkdir -p build; cd build; cmake -DKAA_UNITTESTS_COMPILE=1 ..; cd ..

for cmd in $@
do

case "$cmd" in
    build)
    cd build && make && cd ..
    ;;

    install)
    cd build && make install && cd ..
    ;;

    test)
    cd build && make
    FAILUTE_COUNTER=0
    FAILED_TESTS=""
    for test in test_*
    do
        ./$test
        if [ "$?" -ne "0" ]
        then
            FAILUTE_COUNTER=$((FAILUTE_COUNTER + 1))
            FAILED_TESTS="$test\n$FAILED_TESTS"
        fi
    done
    if [ "$FAILUTE_COUNTER" -ne "0" ]
    then
        echo -e "\n$FAILUTE_COUNTER TEST(S) FAILED:\n$FAILED_TESTS"
    else
        echo -e "\nTESTS WERE SUCCESSFULLY PASSED\n"
    fi
    ;;

    clean)
        cd build && make clean && cd .. && rm -r build
    ;;
    
    *)
    help
    ;;
esac

done