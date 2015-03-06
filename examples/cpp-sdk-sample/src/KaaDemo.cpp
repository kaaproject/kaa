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

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>

#include <kaa/profile/AbstractProfileContainer.hpp>
#include <kaa/gen/ProfileGen.hpp>

#include <kaa/configuration/storage/IConfigurationPersistenceManager.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>

#include <kaa/logging/Log.hpp>

#include <stdio.h>

using namespace kaa;

// Profile container based on AbstractProfileContainer class that is provided by the SDK
class UserProfileContainer : public AbstractProfileContainer<Profile> {
public:
    UserProfileContainer(const Profile& profile) : profile_(profile) { }

    virtual Profile getProfile()
    {
        return profile_;
    }

    void changeProfile(const Profile& profile)
    {
        profile_ = profile;

        // Update method should be called to notify about changes in the profile.
        updateProfile();
    }
private:
    Profile profile_;
};

class UserConfigurationReceiver : public IConfigurationReceiver {
public:

    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        KAA_LOG_TRACE(boost::format("Configuration received: %1%") % configuration.message.get_string());
    }

};

int main()
{
    Kaa::init();

    IKaaClient& kaaClient = Kaa::getKaaClient();

    Profile clientProfile;
    clientProfile.build = "Client build";
    clientProfile.id = "Client ID";
    clientProfile.os = OS::Linux;
    clientProfile.os_version = "Client OS Version";

    kaaClient.getProfileManager().setProfileContainer(std::make_shared<UserProfileContainer>(clientProfile));

    // Setupping configuration subunit

    IConfigurationStoragePtr storage(std::make_shared<FileConfigurationStorage>("configuration.bin"));
    kaaClient.getConfigurationPersistenceManager().setConfigurationStorage(storage);
    UserConfigurationReceiver receiver;
    kaaClient.getConfigurationManager().subscribeForConfigurationChanges(receiver);

    Kaa::start();

    for (int i = 0; i < 100; ++i)
        sleep(1);

    Kaa::stop();

    return 0;
}
