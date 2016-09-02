#
# Copyright 2013 Produban
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Tries to find Avro headers and libraries.
#
# Usage of this module as follows:
#
#  find_package(Avro)
#
# Variables defined by this module:
#
#  AVRO_FOUND              System has Avro C++ libs/headers
#  AVRO_LIBRARIES          The Avro C++ libraries
#  AVRO_INCLUDE_DIRS       The location of Avro C++ headers

message("\nLooking for Avro C++ headers and libraries")

if(NOT WIN32)
    include(FindPkgConfig)
    if(PKG_CONFIG_FOUND)
	    pkg_check_modules(PC_AVRO avro-cpp)
	    set(AVRO_DEFINITIONS ${PC_AVRO_CFLAGS_OTHER})
    endif(PKG_CONFIG_FOUND)
endif(NOT WIN32)

find_path(AVRO_INCLUDE_DIR
     avro/Encoder.hh
     HINTS
     ${CMAKE_FIND_ROOT_PATH}/include
     ${PC_AVRO_INCLUDEDIR}
     ${PC_AVRO_INCLUDE_DIRS})

if(Avro_USE_STATIC_LIBS)
    set(AVRO_LOOK_FOR_LIB_NAMES avrocpp_s)
else(Avro_USE_STATIC_LIBS)
    set(AVRO_LOOK_FOR_LIB_NAMES avrocpp)
endif(Avro_USE_STATIC_LIBS)

find_library(AVRO_LIBRARY
    NAMES
    ${AVRO_LOOK_FOR_LIB_NAMES}
    PATHS
    ${CMAKE_FIND_ROOT_PATH}/lib
    ${PC_AVRO_LIBDIR}
    ${PC_AVRO_LIBRARY_DIRS})

include(FindPackageHandleStandardArgs)

# handle the QUIETLY and REQUIRED arguments and set AVRO_FOUND to TRUE
# if all listed variables are TRUE
find_package_handle_standard_args(Avro
    DEFAULT_MSG
    AVRO_LIBRARY
    AVRO_INCLUDE_DIR)

mark_as_advanced(AVRO_FOUND
    AVRO_INCLUDE_DIR
    AVRO_LIBRARY)

if(AVRO_FOUND)
    set(AVRO_LIBRARIES ${AVRO_LIBRARY})
    set(AVRO_INCLUDE_DIRS ${AVRO_INCLUDE_DIR})

    get_filename_component(AVRO_LIBRARY_DIR ${AVRO_LIBRARY} PATH)
    get_filename_component(AVRO_LIBRARY_NAME ${AVRO_LIBRARY} NAME_WE)

    mark_as_advanced(AVRO_LIBRARY_DIR AVRO_LIBRARY_NAME)

    message(STATUS "Include directories: ${AVRO_INCLUDE_DIRS}")
    message(STATUS "Libraries: ${AVRO_LIBRARIES}")
endif(AVRO_FOUND)
