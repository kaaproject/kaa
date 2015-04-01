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
#include <iostream>

#include <kaa/Kaa.hpp>
#include <kaa/event/registration/IUserAttachCallback.hpp>
#include <kaa/event/IFetchEventListeners.hpp>
#include <kaa/event/gen/ThermostatEventClassFamilyGen.hpp>

using namespace kaa;

static const char * const THERMO_REQUEST_FQN = "org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest";
static const char * const CHANGE_DEGREE_REQUEST_FQN = "org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest";

class ThermoEventClassFamilyListener: public ThermostatEventClassFamily::ThermostatEventClassFamilyListener {
public:

    ThermoEventClassFamilyListener(EventFamilyFactory& factory) : eventFactory_(factory) { }

    virtual void onEvent(const nsThermostatEventClassFamily::ThermostatInfoRequest& event, const std::string& source)
    {
        std::cout << "Kaa Demo ThermostatInfoRequest event received!" << std::endl;

        nsThermostatEventClassFamily::ThermostatInfo info;
        info.degree.set_int(-5);
        info.targetDegree.set_int(10);
        info.isSetManually.set_null();

        nsThermostatEventClassFamily::ThermostatInfoResponse infoResponse;
        infoResponse.thermostatInfo.set_ThermostatInfo(info);

        eventFactory_.getThermostatEventClassFamily().sendEventToAll(infoResponse);
    }

    virtual void onEvent(const nsThermostatEventClassFamily::ThermostatInfoResponse& event, const std::string& source)
    {
        std::cout << "Kaa Demo ThermostatInfoResponse event received!" << std::endl;

        if (!event.thermostatInfo.is_null()) {
            nsThermostatEventClassFamily::ThermostatInfo info = event.thermostatInfo.get_ThermostatInfo();
            if (!info.degree.is_null()) {
                std::cout << "Kaa Demo degree=" << info.degree.get_int() << std::endl;
            }
            if (!info.targetDegree.is_null()) {
                std::cout << "Kaa Demo targetDegree=" << info.targetDegree.get_int() << std::endl;
            }
            if (!info.isSetManually.is_null()) {
                std::cout << "Kaa Demo isSetManually=" << info.isSetManually.get_bool() << std::endl;
            }
        }
    }

    virtual void onEvent(const nsThermostatEventClassFamily::ChangeDegreeRequest& event, const std::string& source)
    {
        std::cout << "Kaa Demo ChangeDegreeRequest event received!" << std::endl;
        if (!event.degree.is_null()) {
            std::cout << "Kaa Demo changing degree to " << event.degree.get_int() << std::endl;
        }
    }

private:
    EventFamilyFactory& eventFactory_;

};

class ThermoEventListenersCallback: public IFetchEventListeners {
public:

    ThermoEventListenersCallback(EventFamilyFactory& factory) : eventFactory_(factory) { }

    virtual void onEventListenersReceived(const std::vector<std::string>& eventListeners)
    {
        std::cout << "Kaa Demo found " << eventListeners.size() << " event listeners" << std::endl;

        ThermostatEventClassFamily& family = eventFactory_.getThermostatEventClassFamily();

        TransactionIdPtr trxId = eventFactory_.startEventsBlock();

        nsThermostatEventClassFamily::ChangeDegreeRequest changeDegree;
        changeDegree.degree.set_int(10);
        family.addEventToBlock(trxId, changeDegree);

        nsThermostatEventClassFamily::ThermostatInfoRequest infoRequest;
        family.addEventToBlock(trxId, infoRequest);

        eventFactory_.submitEventsBlock(trxId);
    }

    virtual void onRequestFailed()
    {
        std::cout << "Kaa Demo event listeners not found" << std::endl;
    }

private:
    EventFamilyFactory& eventFactory_;

};

class UserAttachCallback: public IUserAttachCallback {
public:

    UserAttachCallback(IKaaClient& client) : kaaClient_(client) { }

    virtual void onAttachSuccess()
    {
        kaaClient_.findEventListeners(std::list<std::string>( { THERMO_REQUEST_FQN, CHANGE_DEGREE_REQUEST_FQN }),
                                      std::make_shared<ThermoEventListenersCallback>(kaaClient_.getEventFamilyFactory()));
    }

    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Kaa Demo attach failed (" << (int) errorCode << "): " << reason << std::endl;
    }

private:
    IKaaClient& kaaClient_;
};

int main()
{
    const char * const KAA_USER_ID = "user@email.com";
    const char * const KAA_USER_ACCESS_TOKEN = "token";

    /*
     * Initialize the Kaa endpoint.
     */
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();


    /*
     * Run the Kaa endpoint.
     */
    Kaa::start();

    ThermoEventClassFamilyListener thermoListener(kaaClient.getEventFamilyFactory());

    kaaClient.getEventFamilyFactory().getThermostatEventClassFamily().addEventFamilyListener(thermoListener);
    kaaClient.attachUser(KAA_USER_ID, KAA_USER_ACCESS_TOKEN, std::make_shared<UserAttachCallback>(kaaClient));

    while (1)
        std::this_thread::sleep_for(std::chrono::seconds(1));

    /*
     * Stop the Kaa endpoint.
     */
    Kaa::stop();

    return 0;
}
