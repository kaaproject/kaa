#!/bin/bash
#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


RUN_DIR=`pwd`

function help {
    echo "Choose one of the following: {build|install|test|clean}"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

DEBUG_ENABLED=1
UNITTESTS_COMPILE=0
MAX_LOG_LEVEL=6
COLLECT_COVERAGE=0

function prepare_build {
    mkdir -p build;
    cd build;
    cmake -DKAA_DEBUG_ENABLED=$DEBUG_ENABLED -DKAA_MAX_LOG_LEVEL=$MAX_LOG_LEVEL -DKAA_UNITTESTS_COMPILE=$UNITTESTS_COMPILE -DKAA_COLLECT_COVERAGE=$COLLECT_COVERAGE ..;
    cd ..
}

function build {
    cd build && make && cd ..
}

function execute_tests {
    cd build
    FAILUTE_COUNTER=0
    FAILED_TESTS=""
    for test in test_*
    do
        echo -e "Starting test $test"
        ./$test
        TEST_RESULT=$?
        echo -e "Test $test finished"
        if [ $TEST_RESULT -ne 0 ]
        then
            FAILUTE_COUNTER=$((FAILUTE_COUNTER + 1))
            FAILED_TESTS="$test\n$FAILED_TESTS"
        fi
    done
    if [[ -f ../gcovr ]]
    then
        chmod +x ../gcovr
        ../gcovr -d -x -f ".*" -e ".*(test|avro|gen).*" -o ./gcovr-report.xml -v > ./gcovr.log
    fi
    if [ "$FAILUTE_COUNTER" -ne "0" ]
    then
        echo -e "\n$FAILUTE_COUNTER TEST(S) FAILED:\n$FAILED_TESTS"
    else
        echo -e "\nTESTS WERE SUCCESSFULLY PASSED\n"
    fi
    cd ..
    
}

function check_installed_software {
    RATS_VERSION="$(rats -h)"
    if [[ $RATS_VERSION = RATS* ]] 
    then
        RATS_INSTALLED=1
    else
        RATS_INSTALLED=0
    fi
    
    CPPCHECK_VERSION="$(cppcheck --version)"
    if [[ $CPPCHECK_VERSION = Cppcheck* ]] 
    then
        CPPCHECK_INTSALLED=1
    else
        CPPCHECK_INSTALLED=0
    fi
    
    VALGRIND_VERSION="$(valgrind --version)"
    if [[ $VALGRIND_VERSION = valgrind* ]] 
    then
        VALGRIND_INSTALLED=1
    else
        VALGRIND_INSTALLED=0
    fi
}

function run_valgrind {
    echo "Starting valgrind..." 
    cd build
    if [[ ! -d valgrindReports ]]
    then
        mkdir valgrindReports
    fi
    for test in test_*
    do
        valgrind --leak-check=full --show-reachable=yes --trace-children=yes -v --log-file=./valgrind.log --xml=yes --xml-file=./valgrindReports/$test.memreport.xml ./$test
        chmod 0666 ./valgrindReports/$test.memreport.xml
    done
    cd ..
    echo "Valgrind analysis finished."
}

function run_cppcheck {
    echo "Starting Cppcheck..."
    cppcheck --enable=all --std=c99 --xml --suppress=unusedFunction src/ test/ 2>build/cppcheck_.xml > build/cppcheck.log
    sed 's@file=\"@file=\"client\/client-multi\/client-c\/@g' build/cppcheck_.xml > build/cppcheck.xml
    rm build/cppcheck_.xml
    echo "Cppcheck analysis finished."
}

function run_rats {
    echo "Starting RATS..."
    rats --xml `find src/ -name *.[ch]` > build/rats-report.xml
    echo "RATS analysis finished."
}

function run_analysis {
    check_installed_software
    if [[ VALGRIND_INSTALLED -eq 1 ]]
    then
        run_valgrind
    fi
    
    if [[ CPPCHECK_INTSALLED -eq 1 ]]
    then
        run_cppcheck
    fi
    
    if [[ RATS_INSTALLED -eq 1 ]]
    then
        run_rats
    fi
} 

function clean {
    if [[ -d build ]]
    then
        cd build
        if [[ -f Makefile ]]
        then 
            make clean
        fi
        cd .. && rm -r build
    fi
}

for cmd in $@
do

case "$cmd" in
    build)
        COLLECT_COVERAGE=0
        UNITTESTS_COMPILE=0
        prepare_build &&
        build
    ;;

    install)
        cd build && make install && cd ..
    ;;

    test)
        COLLECT_COVERAGE=1
        UNITTESTS_COMPILE=1
        prepare_build &&
        build &&
        execute_tests &&
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