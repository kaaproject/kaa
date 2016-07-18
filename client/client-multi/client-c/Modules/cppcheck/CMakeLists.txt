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

# This cmake list is responsible for running cppcheck against sources.

find_program(CPPCHECK_COMMAND cppcheck)

if(CPPCHECK_COMMAND)
   add_custom_target(cppcheck
     COMMAND ${CPPCHECK_COMMAND} --quiet --enable=all --std=c99 --suppress=unusedFunction
                                 --force --error-exitcode=1 --template=gcc -I src/kaa
                                 --inline-suppr src/ test/
                                 -ithirdparty/
     WORKING_DERICTORY ${KAA_SDK_DIR}
     COMMENT "Running cppcheck"
     VERBATIM
     )
else()
  message (STATUS "Could NOT find cppcheck")
endif()
