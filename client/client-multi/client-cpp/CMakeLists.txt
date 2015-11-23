#
# Copyright 2014-2015 CyberVision, Inc.
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

cmake_minimum_required(VERSION 2.8.8)

project (Kaa-cpp)
enable_language(CXX)

# Path to CMake's scripts.
set(CMAKE_MODULE_PATH 
	 ${CMAKE_MODULE_PATH}
	 ${CMAKE_CURRENT_SOURCE_DIR}/Modules
    )

#Checks whether avrogen.sh exists
set (AVROGEN "avrogen.sh")
set (WORKDIR ${CMAKE_CURRENT_SOURCE_DIR})
message("==================================")
if (NOT EXISTS "${WORKDIR}/${AVROGEN}")
   message("Error: avrogen.sh hasn't been found.")
else()
   message("avrogen.sh has been found")
   execute_process(COMMAND chmod +x ./${AVROGEN}
                   COMMAND "./${AVROGEN}" 
                   WORKING_DIRECTORY ${WORKDIR}
                  )
endif()

#
# Sets common compilation flags.
# 
# NOTE: Platform specific compilation flags should be set 
# in the corresponding platform CMake script(s).
if (NOT MSVC)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif ()

if (KAA_DEBUG_ENABLED)
    if (NOT MSVC)
       set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -ggdb")
    endif () 
else()
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Os")
    if (CMAKE_COMPILER_IS_GNUCXX)
        set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")
    endif()
endif()

#Compiler specific flags
if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=gnu++11 -stdlib=libc++")
endif()


if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++0x") 
endif ()

if (WIN32)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_WIN32_WINNT=0x0502") 
endif (WIN32)

# Release version is built by default.
if(KAA_DEBUG_ENABLED)
    set(CMAKE_BUILD_TYPE DEBUG)
else()
    set(CMAKE_BUILD_TYPE RELEASE)
endif()

#
# Sets maximum Kaa SDK log level.
#
# Logging constants:
# NONE  - 0
# FATAL - 1
# ERROR - 2
# WARN  - 3
# INFO  - 4
# DEBUG - 5
# TRACE - 6
if(NOT DEFINED KAA_MAX_LOG_LEVEL)
    if(KAA_DEBUG_ENABLED)
        set(KAA_MAX_LOG_LEVEL 6)
    else()
        set(KAA_MAX_LOG_LEVEL 4)
    endif()
endif()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_MAX_LOG_LEVEL=${KAA_MAX_LOG_LEVEL}")

if (NOT KAA_WITHOUT_LOGGING AND KAA_WITH_SQLITE_LOG_STORAGE)
    set(SQLITE_LOG_STORAGE_ENABLED 1)
endif()

# Disables Kaa library modules.
message("==================================")
message("KAA_MAX_LOG_LEVEL=${KAA_MAX_LOG_LEVEL}")

if ( NOT KAA_WITHOUT_EVENTS )
    message("EVENTS ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_EVENTS")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/event/registration/EndpointRegistrationManager.cpp
            impl/event/registration/UserTransport.cpp
            impl/event/EventManager.cpp
            impl/event/EventTransport.cpp
    )
endif()

if ( NOT KAA_WITHOUT_NOTIFICATIONS ) 
    message("NOTIFICATIONS ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_NOTIFICATIONS")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/notification/NotificationManager.cpp
            impl/notification/NotificationTransport.cpp
    )
    
endif()

if ( NOT KAA_WITHOUT_CONFIGURATION )
    message("CONFIGURATION ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_CONFIGURATION")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/configuration/ConfigurationTransport.cpp
            impl/configuration/manager/ConfigurationManager.cpp
            impl/configuration/storage/FileConfigurationStorage.cpp
    )
endif()


if ( NOT KAA_WITHOUT_LOGGING )
    message("LOGGING ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_LOGGING")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/log/LoggingTransport.cpp
            impl/log/LogCollector.cpp
            impl/log/LogRecord.cpp
            impl/log/MemoryLogStorage.cpp
            impl/log/DefaultLogUploadStrategy.cpp
    )

    if (KAA_WITH_SQLITE_LOG_STORAGE)
        message("SQLITE_LOG_STORAGE ENABLED")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_SQLITE_LOG_STORAGE")
        set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/log/SQLiteDBLogStorage.cpp
        )
    endif() 
endif()

if ( NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL )
    message("OPERATION_TCP_CHANNEL ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_TCP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationTcpChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL )
    message("OPERATION_LONG_POLL_CHANNEL ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_LONG_POLL_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationLongPollChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL )
    message("OPERATION_HTTP_CHANNEL ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_OPERATION_HTTP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationHttpChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL )
    message("BOOTSTRAP_HTTP_CHANNEL ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultBootstrapChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_CONNECTIVITY_CHECKER )
    message("CONNECTIVITY_CHECKER ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_CONNECTIVITY_CHECKER")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/connectivity/IPConnectivityChecker.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL )
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/http/HttpUrl.cpp
            impl/http/MultipartPostHttpRequest.cpp
            impl/http/HttpResponse.cpp
            impl/http/HttpClient.cpp
            impl/transport/HttpDataProcessor.cpp
            impl/channel/impl/AbstractHttpChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_THREADSAFE OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL)
    message("KAA_THREADSAFE ENABLED")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_THREADSAFE")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/utils/ThreadPool.cpp
    )
endif()
message("==================================")

#
# Find third-party dependencies
#
find_package (Boost 1.54 REQUIRED COMPONENTS log system)
find_package (Avro REQUIRED)
find_package (Botan REQUIRED)


if (SQLITE_LOG_STORAGE_ENABLED)
    find_package (Sqlite3 REQUIRED)
endif()

if (WIN32 AND NOT CYGWIN AND NOT MSYS)
    add_definitions (/EHs)
    add_definitions (
        -DBOOST_REGEX_DYN_LINK
        -DBOOST_FILESYSTEM_DYN_LINK
        -DBOOST_SYSTEM_DYN_LINK
        -DBOOST_PROGRAM_OPTIONS_DYN_LINK
        -DBOOST_LOG_DYN_LINK
        -DBOOST_ALL_NO_LIB)
else()
    add_definitions (${Boost_LIB_DIAGNOSTIC_DEFINITIONS} -DBOOST_LOG_DYN_LINK)
endif()

# Sets path(s) to source files.
include_directories (${CMAKE_CURRENT_SOURCE_DIR} 
                     ${Boost_INCLUDE_DIRS}
                     ${AVRO_INCLUDE_DIRS} 
                     ${BOTAN_INCLUDE_DIR})
                     
if (SQLITE_LOG_STORAGE_ENABLED)
    include_directories (${SQLITE3_INCLUDE_DIR})
endif()

set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
        impl/ClientStatus.cpp
        impl/KaaDefaults.cpp
        impl/Kaa.cpp
        impl/KaaClient.cpp
        impl/logging/Log.cpp
        impl/logging/LoggerFactory.cpp
        impl/security/KeyUtils.cpp
        impl/security/RsaEncoderDecoder.cpp
        impl/common/EndpointObjectHash.cpp
        impl/profile/ProfileTransport.cpp
        impl/bootstrap/BootstrapManager.cpp
        impl/bootstrap/BootstrapTransport.cpp
        impl/channel/SyncDataProcessor.cpp
        impl/channel/RedirectionTransport.cpp
        impl/channel/KaaChannelManager.cpp
        impl/kaatcp/KaaTcpCommon.cpp
        impl/kaatcp/KaaTcpParser.cpp
        impl/kaatcp/ConnackMessage.cpp
        impl/kaatcp/KaaSyncResponse.cpp
        impl/kaatcp/KaaTcpResponseProcessor.cpp
        impl/channel/connectivity/PingConnectivityChecker.cpp
        impl/channel/TransportProtocolIdConstants.cpp
        impl/channel/IPTransportInfo.cpp
        impl/failover/DefaultFailoverStrategy.cpp
        impl/context/AbstractExecutorContext.cpp
        impl/context/SimpleExecutorContext.cpp
        impl/KaaClientProperties.cpp
    )

# Builds Kaa libraries.
add_library(kaacpp_o OBJECT ${KAA_SOURCE_FILES})
add_library (kaacpp SHARED ./kaa $<TARGET_OBJECTS:kaacpp_o>)

target_link_libraries(kaacpp
                     ${BOTAN_LIBRARY}
                     ${AVRO_LIBRARIES}
                     ${Boost_LIBRARIES}
    )

if (SQLITE_LOG_STORAGE_ENABLED)
    target_link_libraries(kaacpp ${SQLITE3_LIBRARY})
endif()

if (WIN32)
    target_link_libraries(kaacpp dbghelp)
else (WIN32)
    target_link_libraries(kaacpp pthread)
endif (WIN32)

add_library (kaacpp_s STATIC ./kaa $<TARGET_OBJECTS:kaacpp_o>)
# Sets paths Kaa libraries will be installed in.
if(NOT KAA_INSTALL_PATH)
	set(KAA_INSTALL_PATH "/usr/local")
endif()

message(STATUS "KAA WILL BE INSTALLED TO  ${KAA_INSTALL_PATH} ")
set(CMAKE_INSTALL_PREFIX ${KAA_INSTALL_PATH} ) 
install (DIRECTORY kaa/ DESTINATION ${KAA_INSTALL_PATH}/include/kaa
    FILES_MATCHING PATTERN *.hpp)
install (TARGETS kaacpp kaacpp_s DESTINATION ${KAA_INSTALL_PATH}/lib)
