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
# -*- mode: cmake; -*-
# - Try to find libbotan include dirs and libraries
# Usage of this module as follows:
# This file defines:
# * BOTAN_FOUND if protoc was found
# * BOTAN_LIBRARY The lib to link to (currently only a static unix lib, not
# portable)
# * BOTAN_INCLUDE_DIR The include directories for libbotan.

if(NOT WIN32)
    include(FindPkgConfig)
    if(PKG_CONFIG_FOUND)
        pkg_check_modules(BOTAN botan-1.11)
    endif(PKG_CONFIG_FOUND)
endif(NOT WIN32)

# find the include files
find_path(BOTAN_INCLUDE_DIR botan/version.h
    HINTS
    ${CMAKE_FIND_ROOT_PATH}/include
    ${BOTAN_INCLUDE_DIRS})

# locate the library
if(WIN32)
    set(BOTAN_LIBRARY_NAMES ${BOTAN_LIBRARY_NAMES} libbotan.lib botan.lib)
else(WIN32)
    if(Botan_USE_STATIC_LIBS)
        set(BOTAN_LIBRARY_NAMES ${BOTAN_LIBRARY_NAMES} libbotan.a libbotan-1.11.a)
    else(Botan_USE_STATIC_LIBS)
        set(BOTAN_LIBRARY_NAMES ${BOTAN_LIBRARY_NAMES} libbotan.so libbotan-1.11.so)
    endif(Botan_USE_STATIC_LIBS)
endif(WIN32)

find_library(BOTAN_LIBRARY NAMES ${BOTAN_LIBRARY_NAMES}
    HINTS
    ${CMAKE_FIND_ROOT_PATH}/lib
    ${BOTAN_LIBRARY_DIRS})

# if the include and the program are found then we have it
if(BOTAN_INCLUDE_DIR AND BOTAN_LIBRARY)
    set(BOTAN_FOUND "YES")
endif(BOTAN_INCLUDE_DIR AND BOTAN_LIBRARY)

if(NOT WIN32 AND NOT APPLE)
    list(APPEND BOTAN_LIBRARY "-lrt")
endif(NOT WIN32 AND NOT APPLE)

MARK_AS_ADVANCED(BOTAN_FOUND
    BOTAN_LIBRARY
    BOTAN_INCLUDE_DIR)

if(BOTAN_FOUND)
    message(STATUS "Include directory: ${BOTAN_INCLUDE_DIR}")
    message(STATUS "Library: ${BOTAN_LIBRARY}")
endif()
