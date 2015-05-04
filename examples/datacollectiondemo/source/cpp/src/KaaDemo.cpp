/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <memory>
#include <thread>
#include <cstdint>
#include <string>

#include <kaa/Kaa.hpp>
#include <kaa/log/ILogStorageStatus.hpp>
#include <kaa/log/DefaultLogUploadStrategy.hpp>

using namespace kaa;

// The default strategy uploads logs after either a threshold logs count
// or a threshold logs size has been reached.
// The following custom strategy uploads every log record as soon as it is created.
class LogUploadStrategy : public DefaultLogUploadStrategy {
public:
    LogUploadStrategy(IKaaChannelManagerPtr manager)
        : DefaultLogUploadStrategy(manager) {}

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status)
    {
        if (status.getRecordsCount() >= 1) {
            return LogUploadStrategyDecision::UPLOAD;
        }
        return LogUploadStrategyDecision::NOOP;
    }
};

/*
 * A demo application that shows how to use the Kaa logging API.
 */
int main()
{
    const std::size_t LOGS_TO_SEND_COUNT = 5;

    std::cout << "Data collection demo started" << std::endl;
    std::cout << "--= Press Enter to exit =--" << std::endl;

    //Create a Kaa client with the Kaa desktop context.
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();

    // Set a custom strategy for uploading logs.
    kaaClient.setLogUploadStrategy(std::make_shared<LogUploadStrategy>(&kaaClient.getChannelManager()));

    // Start the Kaa client and connect it to the Kaa server.
    Kaa::start();


    // Send LOGS_TO_SEND_COUNT logs in a loop.
    size_t logNumber = 0;
    while (logNumber++ < LOGS_TO_SEND_COUNT) {
        KaaUserLogRecord logRecord;
        logRecord.level = kaa_log::Level::KAA_INFO;
        logRecord.tag = "TAG";
        logRecord.message = "MESSAGE_" + std::to_string(logNumber);

        std::cout << "Going to send " << logNumber << "th record" << std::endl;

        kaaClient.addLogRecord(logRecord);
    }

    // Wait for the Enter key before exiting.
    std::cin.get();

    // Stop the Kaa client and release all the resources which were in use.
    Kaa::stop();

    std::cout << "Data collection demo stopped" << std::endl;

    return 0;
}
