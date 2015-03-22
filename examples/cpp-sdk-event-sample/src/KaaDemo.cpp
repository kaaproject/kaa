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
        KAA_LOG_TRACE("Kaa Demo ThermostatInfoRequest event received!");

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
        KAA_LOG_TRACE("Kaa Demo ThermostatInfoResponse event received!");

        if (!event.thermostatInfo.is_null()) {
            nsThermostatEventClassFamily::ThermostatInfo info = event.thermostatInfo.get_ThermostatInfo();
            if (!info.degree.is_null()) {
                KAA_LOG_TRACE(boost::format("Kaa Demo degree=%1%") % info.degree.get_int());
            }
            if (!info.targetDegree.is_null()) {
                KAA_LOG_TRACE(boost::format("Kaa Demo targetDegree=%1%") % info.targetDegree.get_int());
            }
            if (!info.isSetManually.is_null()) {
                KAA_LOG_TRACE(boost::format("Kaa Demo isSetManually=%1%") % info.isSetManually.get_bool());
            }
        }
    }

    virtual void onEvent(const nsThermostatEventClassFamily::ChangeDegreeRequest& event, const std::string& source)
    {
        KAA_LOG_TRACE("Kaa Demo ChangeDegreeRequest event received!");
        if (!event.degree.is_null()) {
            KAA_LOG_TRACE(boost::format("Kaa Demo changing degree to %1%") % event.degree.get_int());
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
        KAA_LOG_TRACE(boost::format("Kaa Demo found %1% event listeners") % eventListeners.size());

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
        KAA_LOG_TRACE("Kaa Demo event listeners not found");
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
                                      new ThermoEventListenersCallback(kaaClient_.getEventFamilyFactory()) /* TODO */);
    }

    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        KAA_LOG_TRACE(boost::format("Kaa Demo attach failed (%1%): %2%") % errorCode % reason);
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

    kaaClient.getEventFamilyFactory().getThermostatEventClassFamily().addEventFamilyListener(
            new ThermoEventClassFamilyListener(kaaClient.getEventFamilyFactory()) /* TODO */);
    kaaClient.attachUser(KAA_USER_ID, KAA_USER_ACCESS_TOKEN, std::make_shared<UserAttachCallback>(kaaClient));

    while (1)
        std::this_thread::sleep_for(std::chrono::seconds(1));

    /*
     * Stop the Kaa endpoint.
     */
    Kaa::stop();

    return 0;
}
