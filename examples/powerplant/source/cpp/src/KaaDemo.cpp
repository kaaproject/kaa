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

#include <kaa/configuration/storage/IConfigurationPersistenceManager.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>

using namespace kaa;

bool isShutdown = false;
std::uint32_t threshold_log_count = 1;
const char savedConfig[] = "saved_config.cfg";

class UserConfigurationReceiver : public IConfigurationReceiver {
public:
    void displayConfiguration(const KaaRootConfiguration &configuration)
    {
        if (!configuration.AddressList.is_null()) {
            cout << "Configuration body:" << endl;
            auto links = configuration.AddressList.get_array();
            for (auto& e : links) {
                 cout << e.label << " - " << e.url << endl;
            }
        }
    }
    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        displayConfiguration(configuration);
    }
};

int main()
{
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();

    auto logUploadStrategy = std::make_shared<DefaultLogUploadStrategy>(&kaaClient.getChannelManager());
    logUploadStrategy->setCountThreshold(threshold_log_count);
    kaaClient.setLogUploadStrategy(logUploadStrategy);

    auto configurationStorage = std::make_shared<FileConfigurationStorage>(savedConfig);
    kaaClient.setConfigurationStorage(configurationStorage);
    UserConfigurationReceiver receiver;
    kaaClient.addConfigurationListener(receiver);

    Kaa::start();

    while (!isShutdown) {
        KaaUserLogRecord logRecord;

        kaaClient.addLogRecord(logRecord);
    }

    // Stop the Kaa client and release all the resources which were in use.
    Kaa::stop();

    return 0;
}
