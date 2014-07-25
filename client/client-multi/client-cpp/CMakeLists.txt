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

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")

find_package (Boost 1.55 REQUIRED COMPONENTS log signals system thread)

find_package (Avro REQUIRED)

find_package (Botan REQUIRED)

add_definitions (${Boost_LIB_DIAGNOSTIC_DEFINITIONS} -DBOOST_LOG_DYN_LINK)

include_directories (${CMAKE_CURRENT_SOURCE_DIR} 
                     ${Boost_INCLUDE_DIRS}
                     ${Avro_INCLUDE_DIRS} 
                     ${BOTAN_INCLUDE_DIRS})

set (KAA_SOURCE_FILES
        impl/ClientStatus.cpp
        impl/KaaDefaults.cpp
        impl/Kaa.cpp
        impl/KaaClient.cpp
        impl/logging/Log.cpp
        impl/logging/LoggerFactory.cpp
        impl/http/HttpUrl.cpp
        impl/http/MultipartPostHttpRequest.cpp
        impl/http/HttpResponse.cpp
        impl/http/HttpClient.cpp
        impl/security/KeyUtils.cpp
        impl/security/RsaEncoderDecoder.cpp
        impl/common/EndpointObjectHash.cpp
        impl/profile/ProfileListener.cpp
        impl/profile/ProfileTransport.cpp
        impl/transport/HttpDataProcessor.cpp
        impl/bootstrap/BootstrapManager.cpp
        impl/bootstrap/BootstrapTransport.cpp
        impl/common/CommonTypesFactory.cpp
        impl/common/AvroGenericUtils.cpp
        impl/common/types/CommonArray.cpp
        impl/common/types/CommonEnum.cpp
        impl/common/types/CommonFixed.cpp
        impl/common/types/CommonRecord.cpp
        impl/common/types/SetValueHelper.cpp
        impl/configuration/ConfigurationProcessor.cpp
        impl/configuration/ConfigurationTransport.cpp
        impl/configuration/manager/ConfigurationManager.cpp
        impl/configuration/manager/Strategies.cpp
        impl/configuration/storage/ConfigurationPersistanceManager.cpp
        impl/configuration/delta/DeltaHandlerId.cpp
        impl/configuration/delta/DefaultDeltaManager.cpp
        impl/configuration/delta/DefaultConfigurationDeltaFactory.cpp
        impl/schema/SchemaProcessor.cpp
        impl/schema/storage/SchemaPersistanceManager.cpp
        impl/notification/NotificationManager.cpp
        impl/notification/NotificationTransport.cpp
        impl/event/registration/EndpointRegistrationManager.cpp
        impl/event/registration/UserTransport.cpp
        impl/event/EventManager.cpp
        impl/event/EventTransport.cpp
        impl/log/LoggingTransport.cpp
        impl/channel/impl/DefaultOperationLongPollChannel.cpp
        impl/channel/impl/DefaultOperationHttpChannel.cpp
        impl/channel/impl/DefaultBootstrapChannel.cpp
        impl/channel/OperationsDataProcessor.cpp
        impl/channel/BootstrapDataProcessor.cpp
        impl/channel/RedirectionTransport.cpp
        impl/channel/server/AbstractServerInfo.cpp
        impl/channel/KaaChannelManager.cpp
        impl/log/LogCollector.cpp
        impl/log/LogRecord.cpp
        impl/log/MemoryLogStorage.cpp
    )

add_library(kaacpp_o OBJECT ${KAA_SOURCE_FILES})

add_library (kaacpp SHARED ./kaa $<TARGET_OBJECTS:kaacpp_o>)
add_library (kaacpp_s STATIC ./kaa $<TARGET_OBJECTS:kaacpp_o>)

install (TARGETS kaacpp kaacpp_s
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION lib)

install (DIRECTORY kaa/ DESTINATION include/kaa
    FILES_MATCHING PATTERN *.hpp)

