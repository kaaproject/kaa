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


# Find the CUnit headers and libraries
#
#  CUNIT_INCLUDE_DIRS - The CUnit include directory (directory where CUnit/CUnit.h was found)
#  CUNIT_LIBRARIES    - The libraries needed to use CUnit
#  CUNIT_FOUND        - True if CUnit found in system

 
FIND_PATH(CUNIT_INCLUDE_DIR NAMES CUnit/CUnit.h)
MARK_AS_ADVANCED(CUNIT_INCLUDE_DIR)
 
FIND_LIBRARY(CUNIT_LIBRARY NAMES 
    cunit
    libcunit
    cunitlib
)
MARK_AS_ADVANCED(CUNIT_LIBRARY)
 
INCLUDE(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(CUnit DEFAULT_MSG CUNIT_LIBRARY CUNIT_INCLUDE_DIR)
 
IF(CUNIT_FOUND)
  SET(CUNIT_LIBRARIES ${CUNIT_LIBRARY})
  SET(CUNIT_INCLUDE_DIRS ${CUNIT_INCLUDE_DIR})
ENDIF(CUNIT_FOUND)
