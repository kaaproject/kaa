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

#include <kaa/Kaa.hpp>
#include <kaa/log/DefaultLogUploadStrategy.hpp>

using namespace kaa;

int main()
{
    const std::size_t LOG_NUMBER_TO_SEND = 100;

    /*
     * Initialize the Kaa endpoint.
     */
    Kaa::init();
    IKaaClient& kaaClient = Kaa::getKaaClient();

    /*
     * Create the log upload strategy and specifies the threshold record count to initiate upload.
     */
    const std::size_t DEFAULT_UPLOAD_COUNT_THRESHOLD = 5;
    std::shared_ptr<DefaultLogUploadStrategy> uploadStrategy(new DefaultLogUploadStrategy(&kaaClient.getChannelManager()));
    uploadStrategy->setCountThreshold(DEFAULT_UPLOAD_COUNT_THRESHOLD);

    /*
     * Set the user-defined log upload strategy.
     */
    kaaClient.getLogCollector().setUploadStrategy(uploadStrategy);

    /*
     * Run the Kaa endpoint.
     */
    Kaa::start();

    /*
     * Create and initialize the log record.
     */
    KaaUserLogRecord logRecord;
    logRecord.level = Level::DEBUG;
    logRecord.tag = "Demo tag";
    logRecord.message = "Demo log message";

    /*
     * Add the log record every second.
     */
    size_t logNumber = 0;
    while (logNumber++ < LOG_NUMBER_TO_SEND) {
        kaaClient.getLogCollector().addLogRecord(logRecord);
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    /*
     * Stop the Kaa endpoint.
     */
    Kaa::stop();

    return 0;
}
