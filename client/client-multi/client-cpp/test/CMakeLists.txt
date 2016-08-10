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

cmake_minimum_required(VERSION 2.8.8)
project (Kaa-cpp-test)

if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "-Wall -std=c++0x --coverage -O3 -g -pthread")
endif ()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=gnu++11 -stdlib=libc++ -g --coverage")
endif()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_EVENTS")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_NOTIFICATIONS")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_CONFIGURATION")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_LOGGING")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_USE_SQLITE_LOG_STORAGE")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_TCP_CHANNEL")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_LONG_POLL_CHANNEL")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_OPERATION_HTTP_CHANNEL")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_DEFAULT_CONNECTIVITY_CHECKER")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_THREADSAFE")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_MAX_LOG_LEVEL=6")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DKAA_RUNTIME_KEY_GENERATION=ON")

set ( CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../Modules/)

add_definitions (-DBOOST_TEST_DYN_LINK -DBOOST_LOG_DYN_LINK -DRESOURCE_DIR="${CMAKE_CURRENT_SOURCE_DIR}/resources")

find_package (Avro REQUIRED)
find_package (Botan REQUIRED)
find_package (Boost 1.54 REQUIRED
    COMPONENTS unit_test_framework log thread system)
find_package (Sqlite3 REQUIRED)

include_directories (
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/..
        ${Boost_INCLUDE_DIRS}
        ${Avro_INCLUDE_DIRS} 
        ${BOTAN_INCLUDE_DIR}
        ${SQLITE3_INCLUDE_DIR}

)

set ( KAA_TEST_SOURCES 
        ../impl/ClientStatus.cpp
        ../impl/Kaa.cpp
        ../impl/KaaClient.cpp
        ../impl/KaaDefaults.cpp
        ../impl/logging/Log.cpp
        ../impl/logging/DefaultLogger.cpp
        ../impl/logging/LoggingUtils.cpp
        ../impl/http/HttpUrl.cpp
        ../impl/http/MultipartPostHttpRequest.cpp
        ../impl/http/HttpResponse.cpp
        ../impl/http/HttpClient.cpp
        ../impl/security/KeyUtils.cpp
        ../impl/security/RsaEncoderDecoder.cpp
        ../impl/common/EndpointObjectHash.cpp
        ../impl/profile/ProfileTransport.cpp
        ../impl/transport/HttpDataProcessor.cpp
        ../impl/bootstrap/BootstrapManager.cpp
        ../impl/bootstrap/BootstrapTransport.cpp
        ../impl/configuration/ConfigurationTransport.cpp
        ../impl/configuration/manager/ConfigurationManager.cpp
        ../impl/configuration/storage/FileConfigurationStorage.cpp
        ../impl/event/registration/EndpointRegistrationManager.cpp
        ../impl/event/registration/UserTransport.cpp
        ../impl/event/EventManager.cpp
        ../impl/event/EventTransport.cpp
        ../impl/log/LoggingTransport.cpp
        ../impl/channel/impl/DefaultOperationLongPollChannel.cpp
        ../impl/channel/impl/DefaultOperationHttpChannel.cpp
        ../impl/channel/impl/DefaultBootstrapChannel.cpp
        ../impl/channel/impl/DefaultOperationTcpChannel.cpp
        ../impl/channel/impl/AbstractHttpChannel.cpp
        ../impl/channel/SyncDataProcessor.cpp
        ../impl/channel/RedirectionTransport.cpp
        ../impl/channel/KaaChannelManager.cpp
        ../impl/notification/NotificationTransport.cpp
        ../impl/notification/NotificationManager.cpp
        ../impl/log/LogCollector.cpp
        ../impl/log/LogStorageConstants.cpp
        ../impl/log/RecordFuture.cpp
        ../impl/log/DefaultLogUploadStrategy.cpp
        ../impl/log/MemoryLogStorage.cpp
        ../impl/log/SQLiteDBLogStorage.cpp
        ../impl/kaatcp/KaaTcpCommon.cpp
        ../impl/kaatcp/KaaTcpParser.cpp
        ../impl/kaatcp/ConnackMessage.cpp
        ../impl/kaatcp/KaaSyncResponse.cpp
        ../impl/kaatcp/KaaTcpResponseProcessor.cpp
        ../impl/channel/connectivity/IPConnectivityChecker.cpp
        ../impl/channel/connectivity/PingConnectivityChecker.cpp
        ../impl/channel/TransportProtocolIdConstants.cpp
        ../impl/channel/IPTransportInfo.cpp
        ../impl/failover/DefaultFailoverStrategy.cpp
        ../impl/utils/ThreadPool.cpp
        ../impl/context/SimpleExecutorContext.cpp
        ../impl/context/AbstractExecutorContext.cpp
        ../impl/KaaClientProperties.cpp
        TestRunner.cpp
        impl/KaaTestUtils.cpp
        impl/channel/impl/DefaultBootstrapChannelTest.cpp
        impl/common/EndpointObjectHashTest.cpp
        impl/common/AvroByteArrayConverterTest.cpp
        impl/bootstrap/BootstrapFailoverTest.cpp
        impl/configuration/ConfigurationManagerTest.cpp
        impl/configuration/FileConfigurationStorageTest.cpp
        impl/http/HttpUrlTest.cpp
        impl/http/HttpResponseTest.cpp
        impl/http/HttpRequestTest.cpp
        impl/ClientStatusTest.cpp
        impl/KaaClientTest.cpp
        impl/event/EndpointRegistrationManagerTest.cpp
        impl/security/KeyUtilsTest.cpp
        impl/event/EventTransportTest.cpp
        impl/channel/KaaChannelManagerTest.cpp
        impl/notification/NotificationTransportTest.cpp
        impl/notification/NotificationManagerTest.cpp
        impl/kaatcp/KaaTcpTest.cpp
        impl/channel/IPConnectivityCheckerTest.cpp
        impl/log/DefaultLogUploadStrategyTest.cpp
        impl/log/MemoryLogStorageTest.cpp
        impl/log/LogCollectorTest.cpp
        impl/log/SQLiteDBLogStorageTest.cpp
        impl/utils/KaaTimerTest.cpp
        impl/utils/ThreadPoolTest.cpp
        impl/log/strategies/RecordCountLogUploadStrategyTest.cpp
        impl/log/strategies/StorageSizeLogUploadStrategyTest.cpp
        impl/log/strategies/PeriodicLogUploadStrategyTest.cpp
        impl/log/strategies/PeriodicLogUploadStrategyTest.cpp
        impl/log/strategies/RecordCountWithTimeLimitLogUploadStrategyTest.cpp
        impl/log/strategies/StorageSizeWithTimeLimitLogUploadStrategyTest.cpp
        impl/profile/ProfileManagerTest.cpp
        impl/channel/PingConnectivityCheckerTest.cpp
        impl/KaaClientPropertiesTest.cpp
        impl/profile/ProfileTransportTest.cpp
        impl/channel/SyncDataProcessorTest.cpp
    )

add_executable ( kaatest  ${KAA_TEST_SOURCES})
target_link_libraries ( kaatest pthread
    ${BOTAN_LIBRARY}
    ${AVRO_LIBRARIES} 
    ${Boost_LIBRARIES}
    ${SQLITE3_LIBRARY}
)

