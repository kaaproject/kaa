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

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>

#include <kaa/profile/AbstractProfileContainer.hpp>
#include <kaa/profile/gen/ProfileGen.hpp>

#include <kaa/configuration/storage/IConfigurationPersistenceManager.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>

#include <kaa/logging/Log.hpp>

#include <stdio.h>

using namespace kaa;
// A profile container thast is based on the AbstractProfileContainer class provided by the SDK.
class UserProfileContainer : public AbstractProfileContainer<kaa_profile::Profile> {
public:
     UserProfileContainer(const kaa_profile::Profile& profile) : profile_(profile) { }

     virtual kaa_profile::Profile getProfile()
     {
         return profile_;
     }
private:
     kaa_profile::Profile profile_;
};

class UserConfigurationReceiver : public IConfigurationReceiver {
public:
  void displayConfiguration(const KaaRootConfiguration &configuration)
  {
      if (!configuration.AddressList.is_null()) {
          KAA_LOG_TRACE("Configuration body:");
          auto links = configuration.AddressList.get_array();
          for(auto& e : links) {
              KAA_LOG_TRACE(boost::format("%1% - %2%") % (e.label) % (e.url));
          }
      }
  }
  virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
  {
      KAA_LOG_TRACE("Configuration was updated");
      displayConfiguration(configuration);
  }
};


int main()
{
    Kaa::init();
    KAA_LOG_TRACE("Configuration demo started")
    IKaaClient& kaaClient = Kaa::getKaaClient();
    KAA_LOG_TRACE("Configuration demo started");
    kaa_profile::Profile clientProfile{};
    kaaClient.setProfileContainer(std::make_shared<UserProfileContainer>(clientProfile));
    // Set up a configuration subunit
    IConfigurationStoragePtr storage(std::make_shared<FileConfigurationStorage>("saved_config.cfg"));
    kaaClient.setConfigurationStorage(storage);
    UserConfigurationReceiver receiver;
    kaaClient.addConfigurationListener(receiver);

    Kaa::start();

    for (int i = 0; i < 100; ++i)
        std::this_thread::sleep_for(std::chrono::seconds(1));

    Kaa::stop();
    KAA_LOG_TRACE("Configuration demo stopped")
    return 0;
}
