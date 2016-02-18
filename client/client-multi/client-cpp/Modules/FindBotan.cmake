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

# -*- mode: cmake; -*-
# - Try to find libbotan include dirs and libraries
# Usage of this module as follows:
# This file defines:
# * BOTAN_FOUND if protoc was found
# * BOTAN_LIBRARY The lib to link to (currently only a static unix lib, not
# portable) 
# * BOTAN_INCLUDE The include directories for libbotan.

message(STATUS "FindBotan check")
IF (NOT WIN32)
  include(FindPkgConfig)
  if ( PKG_CONFIG_FOUND )

     pkg_check_modules (PC_BOTAN botan-1.11)

     set(BOTAN_DEFINITIONS ${PC_BOTAN_CFLAGS_OTHER})
  endif(PKG_CONFIG_FOUND)
endif (NOT WIN32)

#
# set defaults
SET(_botan_HOME "/usr/local")
SET(_botan_INCLUDE_SEARCH_DIRS
  ${CMAKE_INCLUDE_PATH}
  /usr/local/include
  /usr/include
  )

SET(_botan_LIBRARIES_SEARCH_DIRS
  ${CMAKE_LIBRARY_PATH}
  /usr/local/lib
  /usr/lib
  )

##
if( "${BOTAN_HOME}" STREQUAL "")
  if("" MATCHES "$ENV{BOTAN_HOME}")
    message(STATUS "BOTAN_HOME env is not set, setting it to /usr/local")
    set (BOTAN_HOME ${_botan_HOME})
  else("" MATCHES "$ENV{BOTAN_HOME}")
    set (BOTAN_HOME "$ENV{BOTAN_HOME}")
  endif("" MATCHES "$ENV{BOTAN_HOME}")
else( "${BOTAN_HOME}" STREQUAL "")
  message(STATUS "BOTAN_HOME is not empty: \"${BOTAN_HOME}\"")
endif( "${BOTAN_HOME}" STREQUAL "")
##

message(STATUS "Looking for botan in ${BOTAN_HOME}")

IF( NOT ${BOTAN_HOME} STREQUAL "" )
    SET(_botan_INCLUDE_SEARCH_DIRS 
        ${BOTAN_HOME}/include 
        ${_botan_INCLUDE_SEARCH_DIRS}
    )
    SET(_botan_LIBRARIES_SEARCH_DIRS 
        ${BOTAN_HOME}
        ${BOTAN_HOME}/lib 
        ${_botan_LIBRARIES_SEARCH_DIRS}
    )    
    SET(_botan_HOME ${BOTAN_HOME})
ENDIF( NOT ${BOTAN_HOME} STREQUAL "" )

IF( NOT $ENV{BOTAN_INCLUDEDIR} STREQUAL "" )
  SET(_botan_INCLUDE_SEARCH_DIRS $ENV{BOTAN_INCLUDEDIR} ${_botan_INCLUDE_SEARCH_DIRS})
ENDIF( NOT $ENV{BOTAN_INCLUDEDIR} STREQUAL "" )

IF( NOT $ENV{BOTAN_LIBRARYDIR} STREQUAL "" )
  SET(_botan_LIBRARIES_SEARCH_DIRS $ENV{BOTAN_LIBRARYDIR} ${_botan_LIBRARIES_SEARCH_DIRS})
ENDIF( NOT $ENV{BOTAN_LIBRARYDIR} STREQUAL "" )

IF( BOTAN_HOME )
  SET(_botan_INCLUDE_SEARCH_DIRS ${BOTAN_HOME}/include ${_botan_INCLUDE_SEARCH_DIRS})
  SET(_botan_LIBRARIES_SEARCH_DIRS ${BOTAN_HOME}/lib ${_botan_LIBRARIES_SEARCH_DIRS})
  SET(_botan_HOME ${BOTAN_HOME})
ENDIF( BOTAN_HOME )

# find the include files
FIND_PATH(BOTAN_INCLUDE_DIR botan/version.h
   HINTS
     ${_botan_INCLUDE_SEARCH_DIRS}
     ${PC_BOTAN_INCLUDEDIR}
     ${PC_BOTAN_INCLUDE_DIRS}
    ${CMAKE_INCLUDE_PATH}
)

# locate the library
IF(WIN32)
  SET(BOTAN_LIBRARY_NAMES ${BOTAN_LIBRARY_NAMES} libbotan.lib botan.lib)
ELSE(WIN32)
  SET(BOTAN_LIBRARY_NAMES ${BOTAN_LIBRARY_NAMES} libbotan.so libbotan-1.11.so)
ENDIF(WIN32)
FIND_LIBRARY(BOTAN_LIBRARY NAMES ${BOTAN_LIBRARY_NAMES}
  HINTS
    ${_botan_LIBRARIES_SEARCH_DIRS}
    ${PC_BOTAN_LIBDIR}
    ${PC_BOTAN_LIBRARY_DIRS}
)

# if the include and the program are found then we have it
IF(BOTAN_INCLUDE_DIR AND BOTAN_LIBRARY) 
  SET(BOTAN_FOUND "YES")
ENDIF(BOTAN_INCLUDE_DIR AND BOTAN_LIBRARY)

if( NOT WIN32 AND NOT APPLE)
  list(APPEND BOTAN_LIBRARY "-lrt")
endif( NOT WIN32 AND NOT APPLE)

MARK_AS_ADVANCED(
  BOTAN_FOUND
  BOTAN_LIBRARY
  BOTAN_INCLUDE_DIR
)

if (BOTAN_FOUND)
    message (STATUS "Include directory: ${BOTAN_INCLUDE_DIR}")
    message (STATUS "Library: ${BOTAN_LIBRARY}")
endif ()
