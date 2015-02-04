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

cmake_minimum_required(VERSION 2.8.8)
project (Kaa-cpp)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/Modules)

if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++0x -g")
endif ()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=gnu++11 -stdlib=libc++ -g")
endif()

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")

if ( NOT KAA_WITHOUT_EVENTS )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_EVENTS")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/event/registration/EndpointRegistrationManager.cpp
            impl/event/registration/UserTransport.cpp
            impl/event/EventManager.cpp
            impl/event/EventTransport.cpp
    )
endif()

if ( NOT KAA_WITHOUT_NOTIFICATIONS )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_NOTIFICATIONS")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/notification/NotificationManager.cpp
            impl/notification/NotificationTransport.cpp
    )
endif()

if ( NOT KAA_WITHOUT_CONFIGURATION )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_CONFIGURATION")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/configuration/ConfigurationProcessor.cpp
            impl/configuration/ConfigurationTransport.cpp
            impl/configuration/manager/ConfigurationManager.cpp
            impl/configuration/manager/Strategies.cpp
            impl/configuration/storage/ConfigurationPersistenceManager.cpp
            impl/configuration/delta/DeltaHandlerId.cpp
            impl/configuration/delta/DefaultDeltaManager.cpp
            impl/configuration/delta/DefaultConfigurationDeltaFactory.cpp
            impl/schema/SchemaProcessor.cpp
            impl/schema/storage/SchemaPersistenceManager.cpp
            impl/common/CommonTypesFactory.cpp
            impl/common/AvroGenericUtils.cpp
            impl/common/types/CommonArray.cpp
            impl/common/types/CommonEnum.cpp
            impl/common/types/CommonFixed.cpp
            impl/common/types/CommonRecord.cpp
            impl/common/types/SetValueHelper.cpp
    )
endif()

if ( NOT KAA_WITHOUT_LOGGING )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_LOGGING")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/log/LoggingTransport.cpp
            impl/log/LogCollector.cpp
            impl/log/LogRecord.cpp
            impl/log/MemoryLogStorage.cpp
            impl/log/LogUploadFailoverStrategy.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_TCP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationTcpChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_LONG_POLL_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationLongPollChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_OPERATION_HTTP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultOperationHttpChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/impl/DefaultBootstrapChannel.cpp
    )
endif()

if ( NOT KAA_WITHOUT_CONNECTIVITY_CHECKER )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_CONNECTIVITY_CHECKER")
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/channel/connectivity/PingConnectivityChecker.cpp
    )
endif()

if ( NOT KAA_WITHOUT_OPERATION_HTTP_CHANNEL OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL )
    set (KAA_SOURCE_FILES ${KAA_SOURCE_FILES}
            impl/http/HttpUrl.cpp
            impl/http/MultipartPostHttpRequest.cpp
            impl/http/HttpResponse.cpp
            impl/http/HttpClient.cpp
            impl/transport/HttpDataProcessor.cpp
    )
endif()

if ( NOT KAA_WITHOUT_THREADSAFE OR NOT KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL OR NOT KAA_WITHOUT_OPERATION_TCP_CHANNEL )
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_THREADSAFE")
endif()

find_package (Boost 1.55 REQUIRED COMPONENTS log system)

find_package (Avro REQUIRED)

find_package (Botan REQUIRED)

add_definitions (${Boost_LIB_DIAGNOSTIC_DEFINITIONS} -DBOOST_LOG_DYN_LINK)

include_directories (${CMAKE_CURRENT_SOURCE_DIR} 
                     ${Boost_INCLUDE_DIRS}
                     ${Avro_INCLUDE_DIRS} 
                     ${BOTAN_INCLUDE_DIRS})

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
        impl/profile/ProfileListener.cpp
        impl/profile/ProfileTransport.cpp
        impl/bootstrap/BootstrapManager.cpp
        impl/bootstrap/BootstrapTransport.cpp
        impl/channel/OperationsDataProcessor.cpp
        impl/channel/BootstrapDataProcessor.cpp
        impl/channel/RedirectionTransport.cpp
        impl/channel/KaaChannelManager.cpp
        impl/kaatcp/KaaTcpCommon.cpp
        impl/kaatcp/KaaTcpParser.cpp
        impl/kaatcp/ConnackMessage.cpp
        impl/kaatcp/KaaSyncResponse.cpp
        impl/kaatcp/KaaTcpResponseProcessor.cpp
    )

add_library(kaacpp_o OBJECT ${KAA_SOURCE_FILES})

add_library (kaacpp SHARED ./kaa $<TARGET_OBJECTS:kaacpp_o>)

target_link_libraries(kaacpp pthread
    ${BOTAN_LIBRARY}
    ${AVRO_LIBRARIES}
    ${Boost_LIBRARIES})

add_library (kaacpp_s STATIC ./kaa $<TARGET_OBJECTS:kaacpp_o>)

install (TARGETS kaacpp kaacpp_s
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION lib)

install (DIRECTORY kaa/ DESTINATION include/kaa
    FILES_MATCHING PATTERN *.hpp)

