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

# Available options



#   - `KAA_MAX_LOG_LEVEL` - maximum log level the SDK should produce.
#
#       Values:
#
#       - `0` - `NONE`
#       - `1` - `FATAL`
#       - `2` - `ERROR`
#       - `3` - `WARN`
#       - `4` - `INFO`
#       - `5` - `DEBUG`
#       - `6` - `TRACE`
#
#       Default: `4`.
#
#   - `KAA_WITHOUT_EVENTS` - disable Event feature.
#
#       Values:
#
#       - `1` - Event feature is disabled
#       - `0` - Event feature is enabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_NOTIFICATION` - disable Notification feature.
#
#       Values:
#
#       - `1` - Notification feature is disabled
#       - `0` - Notification feature is enabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_CONFIGURATION` - disable Configuration feature.
#
#       Values:
#
#       - `1` - Configuration feature is disabled
#       - `0` - Configuration feature is enabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_LOGGING` - disable Logging feature.
#
#       Values:
#
#       - `1` - Logging feature is disabled
#       - `0` - Logging feature is enabled
#
#       Default: `0`.
#
#   - `KAA_WITH_SQLITE_STORAGE` - enables SQLite storage for Logging feature.
#   That requires SQLite3 headers present on the system.
#
#       Values:
#
#       - `0` - SQLite storage is disabled
#       - `1` - SQLite storage is enabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_THREADSAFE` - disable thread safe mode. Otherwise, Kaa SDK will maintain a thread pool.
#
#       Values:
#
#       - `0` - Threadsafe mode is enabled
#       - `1` - Threadsafe mode is disabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_CONNECTIVITY_CHECKER` - disable connectivity checking. Should be used if you implement custom connectivity checker.
#
#       Values:
#
#       - `0` - Default connectivity checking is enabled
#       - `1` - Default connectivity checking is disabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_OPERATION_HTTP_CHANNEL` - disable default Operation HTTP channel. Should be used if you implement custom Operation channel.
#
#       Values:
#
#       - `0` - Default Operation HTTP channel is enabled
#       - `1` - Default Operation HTTP channel is disabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_OPERATION_TCP_CHANNEL` - disable default Operation TCP channel. Should be used if you implement custom Operation channel.
#
#       Values:
#
#       - `0` - Default Operation TCP channel is enabled
#       - `1` - Default Operation TCP channel is disabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL` - disable default Operation long poll channel. Should be used if you implement custom Operation channel.
#
#       Values:
#
#       - `0` - Default Operation long poll channel is enabled
#       - `1` - Default Operation long poll channel is disabled
#
#       Default: `0`.
#
#   - `KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL` - disable default Bootstrap HTTP channel. Should be used if you implement custom Bootstrap channel.
#
#       Values:
#
#       - `0` - Default Bootstrap HTTP channel is enabled
#       - `1` - Default Bootstrap HTTP channel is disabled
#
#       Default: `0`.

cmake_minimum_required(VERSION 2.8.12 FATAL_ERROR)
project(KaaCppSdk CXX)

macro(DECCHAR2HEX VAR VAL)
    if(${VAL} LESS 10)
        SET(${VAR} ${VAL})
    elseif(${VAL} EQUAL 10)
        SET(${VAR} "A")
    elseif(${VAL} EQUAL 11)
        SET(${VAR} "B")
    elseif(${VAL} EQUAL 12)
        SET(${VAR} "C")
    elseif(${VAL} EQUAL 13)
        SET(${VAR} "D")
    elseif(${VAL} EQUAL 14)
        SET(${VAR} "E")
    elseif(${VAL} EQUAL 15)
        SET(${VAR} "F")
    else()
        MESSAGE(FATAL_ERROR "Invalid format for hexidecimal character")
    endif()
endmacro(DECCHAR2HEX)

macro(DEC2HEX16 VAR VAL)
    if(${VAL} EQUAL 0)
        SET(${VAR} 0)
    else()
        SET(VAL2 ${VAL})
        SET(${VAR} "")

        foreach(i 0 4 8 12)
            MATH(EXPR VALCHAR "((${VAL}>>${i})&15)")
            DECCHAR2HEX(VALCHAR ${VALCHAR})
            SET(${VAR} "${VALCHAR}${${VAR}}")
        endforeach(i)
    endif()
endmacro(DEC2HEX16)

# Path to CMake's scripts.
set(CMAKE_MODULE_PATH 
         ${CMAKE_MODULE_PATH}
         ${CMAKE_CURRENT_SOURCE_DIR}/Modules)

#Checks whether avrogen script exists
set(WORKDIR ${CMAKE_CURRENT_SOURCE_DIR})
message("==================================")
if(WIN32)
    set(AVROGEN "avrogen.bat")
    if(NOT EXISTS "${WORKDIR}/${AVROGEN}")
       message("Error: avrogen.bat hasn't been found.")
    else()
       execute_process(COMMAND "${AVROGEN}"
                       WORKING_DIRECTORY ${WORKDIR})
    endif()
else()
    set(AVROGEN "avrogen.sh")
    if(NOT EXISTS "${WORKDIR}/${AVROGEN}")
       message("Error: avrogen.sh hasn't been found.")
    else()
       execute_process(COMMAND chmod +x ./${AVROGEN}
                       WORKING_DIRECTORY ${WORKDIR})
       execute_process(COMMAND "./${AVROGEN}" 
                       WORKING_DIRECTORY ${WORKDIR})
    endif()
endif()

#
# Sets common compilation flags.
# 
if(NOT MSVC)
   set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -ggdb")
endif()

# "no-unused-private-field" is added since in most of cases it is false positive
if(NOT MSVC)
    list(APPEND KAA_COMPILE_OPTIONS
                    -Wno-unused-private-field)
endif()

#Compiler specific flags
if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    list(APPEND KAA_COMPILE_OPTIONS 
                    -Wall
                    -std=gnu++11
                    -stdlib=libc++)
endif()

if(CMAKE_COMPILER_IS_GNUCXX)
    list(APPEND KAA_COMPILE_OPTIONS -Wall)

    if(CMAKE_SYSTEM_NAME STREQUAL "QNX")
        list(APPEND KAA_COMPILE_OPTIONS -std=gnu++11)
    else()
        list(APPEND KAA_COMPILE_OPTIONS -std=c++11)
    endif()
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
    if("${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
        set(KAA_MAX_LOG_LEVEL 6)
    else()
        set(KAA_MAX_LOG_LEVEL 4)
    endif()
endif()

list(APPEND KAA_COMPILE_DEFINITIONS KAA_MAX_LOG_LEVEL=${KAA_MAX_LOG_LEVEL})

if(KAA_WITHOUT_LOGGING)
    set(KAA_WITH_SQLITE_LOG_STORAGE 0)
endif()

# Disables Kaa library modules.
message("==================================")
message("KAA_MAX_LOG_LEVEL=${KAA_MAX_LOG_LEVEL}")

if(NOT KAA_WITHOUT_EVENTS)
    message("EVENTS ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_USE_EVENTS)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/event/registration/EndpointRegistrationManager.cpp
            impl/event/registration/UserTransport.cpp
            impl/event/EventManager.cpp
            impl/event/EventTransport.cpp
    )
endif()

if(NOT KAA_WITHOUT_NOTIFICATIONS) 
    message("NOTIFICATIONS ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_USE_NOTIFICATIONS)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/notification/NotificationManager.cpp
            impl/notification/NotificationTransport.cpp
    )
endif()

if(NOT KAA_WITHOUT_CONFIGURATION)
    message("CONFIGURATION ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_USE_CONFIGURATION)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/configuration/ConfigurationTransport.cpp
            impl/configuration/manager/ConfigurationManager.cpp
            impl/configuration/storage/FileConfigurationStorage.cpp
    )
endif()


if(NOT KAA_WITHOUT_LOGGING )
    message("LOGGING ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_USE_LOGGING)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/log/LoggingTransport.cpp
            impl/log/LogCollector.cpp
            impl/log/LogStorageConstants.cpp
            impl/log/RecordFuture.cpp
            impl/log/MemoryLogStorage.cpp
            impl/log/DefaultLogUploadStrategy.cpp
    )

    if(KAA_WITH_SQLITE_LOG_STORAGE)
        message("SQLITE_LOG_STORAGE ENABLED")
        list(APPEND KAA_COMPILE_DEFINITIONS KAA_USE_SQLITE_LOG_STORAGE)
        set(KAA_SOURCE_FILES
                ${KAA_SOURCE_FILES}
                impl/log/SQLiteDBLogStorage.cpp
        )
    endif() 
endif()

if(NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL)
    message("OPERATION_TCP_CHANNEL ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_DEFAULT_TCP_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationTcpChannel.cpp
    )
endif()

if(NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL)
    message("OPERATION_LONG_POLL_CHANNEL ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_DEFAULT_LONG_POLL_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationLongPollChannel.cpp
    )
endif()

if(NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL)
    message("OPERATION_HTTP_CHANNEL ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_DEFAULT_OPERATION_HTTP_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationHttpChannel.cpp
    )
endif()

if(NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL)
    message("BOOTSTRAP_HTTP_CHANNEL ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultBootstrapChannel.cpp
    )
endif()

if(NOT KAA_WITHOUT_CONNECTIVITY_CHECKER)
    message("CONNECTIVITY_CHECKER ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_DEFAULT_CONNECTIVITY_CHECKER)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/channel/connectivity/IPConnectivityChecker.cpp
    )
endif()

if(NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/http/HttpUrl.cpp
            impl/http/MultipartPostHttpRequest.cpp
            impl/http/HttpResponse.cpp
            impl/http/HttpClient.cpp
            impl/transport/HttpDataProcessor.cpp
            impl/channel/impl/AbstractHttpChannel.cpp
    )
endif()

if(NOT KAA_WITHOUT_THREADSAFE OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL)
    message("KAA_THREADSAFE ENABLED")
    list(APPEND KAA_COMPILE_DEFINITIONS KAA_THREADSAFE)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            impl/utils/ThreadPool.cpp
    )
endif()
message("==================================")

option(KAA_RUNTIME_KEY_GENERATION "Enable RSA key generation at runtime" ON)
if(${KAA_RUNTIME_KEY_GENERATION})
    add_definitions(-DKAA_RUNTIME_KEY_GENERATION)
endif()

#
# Find third-party dependencies
#
find_package(Boost 1.54 REQUIRED COMPONENTS log system thread)
find_package(Avro REQUIRED)
find_package(Botan REQUIRED)

if(KAA_WITH_SQLITE_LOG_STORAGE)
    find_package(Sqlite3 REQUIRED)
endif()

if(WIN32 AND NOT CYGWIN AND NOT MSYS)
    if(CMAKE_SYSTEM_VERSION)
        string(REGEX REPLACE "^([0-9]+).*" "\\1" verMajor ${CMAKE_SYSTEM_VERSION})
        string(REGEX REPLACE "^[0-9]+.([0-9]+).*" "\\1" verMinor ${CMAKE_SYSTEM_VERSION})
        math(EXPR ver "((${verMajor} << 8) + ${verMinor})")
        DEC2HEX16(version ${ver})
        message(STATUS "CMAKE_SYSTEM_VERSION=${CMAKE_SYSTEM_VERSION}")
        message(STATUS "VERSION=${version}")

        list(APPEND KAA_COMPILE_DEFINITIONS _WIN32_WINNT=0x${version})
    endif() 

    list(APPEND KAA_COMPILE_DEFINITIONS
                    /EHs
                    BOOST_REGEX_DYN_LINK
                    BOOST_FILESYSTEM_DYN_LINK
                    BOOST_SYSTEM_DYN_LINK
                    BOOST_PROGRAM_OPTIONS_DYN_LINK
                    BOOST_LOG_DYN_LINK
                    BOOST_ALL_NO_LIB)
else()
    list(APPEND KAA_COMPILE_DEFINITIONS ${Boost_LIB_DIAGNOSTIC_DEFINITIONS})
    if(NOT Boost_USE_STATIC_LIBS)
       list(APPEND KAA_COMPILE_DEFINITIONS BOOST_LOG_DYN_LINK)
    endif()
endif()

set(KAA_INCLUDE_DIRS
        ${CMAKE_CURRENT_SOURCE_DIR} 
        ${Boost_INCLUDE_DIRS}
        ${AVRO_INCLUDE_DIRS} 
        ${BOTAN_INCLUDE_DIR})

set(KAA_THIRDPARTY_LIBRARIES 
        ${BOTAN_LIBRARY} 
        ${AVRO_LIBRARIES} 
        ${Boost_LIBRARIES}
        # The CMAKE_THREAD_LIBS_INIT variable is the replacement of a platform-dependent thread library.
        # It is initialized on calling find_package(Boost).
        ${CMAKE_THREAD_LIBS_INIT}
)

if(KAA_WITH_SQLITE_LOG_STORAGE)
    set(KAA_INCLUDE_DIRS
            ${KAA_INCLUDE_DIRS}
            ${SQLITE3_INCLUDE_DIR})

    set(KAA_THIRDPARTY_LIBRARIES
            ${KAA_THIRDPARTY_LIBRARIES} 
            ${SQLITE3_LIBRARY})
endif()

if(WIN32 AND "${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
    set(KAA_THIRDPARTY_LIBRARIES
            ${KAA_THIRDPARTY_LIBRARIES}
            dbghelp)
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "QNX")
    set(KAA_THIRDPARTY_LIBRARIES
            ${KAA_THIRDPARTY_LIBRARIES}
            socket)
endif()

set(KAA_SOURCE_FILES
        ${KAA_SOURCE_FILES}
        impl/ClientStatus.cpp
        impl/KaaDefaults.cpp
        impl/Kaa.cpp
        impl/KaaClient.cpp
        impl/logging/Log.cpp
        impl/logging/DefaultLogger.cpp
        impl/logging/LoggingUtils.cpp
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
add_library(kaacpp ${KAA_SOURCE_FILES})
target_compile_options(kaacpp PUBLIC ${KAA_COMPILE_OPTIONS})
target_compile_definitions(kaacpp PUBLIC ${KAA_COMPILE_DEFINITIONS})
target_include_directories(kaacpp PUBLIC ${KAA_INCLUDE_DIRS})
target_link_libraries(kaacpp PRIVATE ${KAA_THIRDPARTY_LIBRARIES})

# Install Kaa headers/libraries.
message(STATUS "KAA WILL BE INSTALLED TO ${CMAKE_INSTALL_PREFIX}")

install(DIRECTORY kaa/ 
        DESTINATION include/kaa
        FILES_MATCHING PATTERN *.hpp)

install(TARGETS kaacpp 
        DESTINATION lib)
