/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef DeviceEventClassFamily_HPP_
#define DeviceEventClassFamily_HPP_

#ifdef KAA_USE_EVENTS

#include <set>
#include <list>
#include <string>
#include <vector>
#include <sstream>

#include <cstdint>

#include "kaa/logging/Log.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/IEventFamily.hpp"
#include "kaa/event/IEventManager.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/transact/TransactionId.hpp"

#include "kaa/event/gen/DeviceEventClassFamilyGen.hpp"

namespace kaa {

class DeviceEventClassFamily : public IEventFamily
{
public:
    class DeviceEventClassFamilyListener
    {
    public:

                virtual void onEvent(const nsDeviceEventClassFamily :: DeviceInfoRequest& event, const std::string& source) = 0;
        virtual void onEvent(const nsDeviceEventClassFamily :: DeviceInfoResponse& event, const std::string& source) = 0;


        virtual ~DeviceEventClassFamilyListener() {}
    };

public:
    DeviceEventClassFamily(IEventManager& manager)
        : eventManager_(manager)
    {
        eventFQNs_ =         {"org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest","org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse"} /* {"fqn1","fqn2","fqn3"} */;
    }

    virtual void onGenericEvent(const std::string& fqn
                              , const std::vector<std::uint8_t>& data
                              , const std::string& source)
    {
        if (fqn.empty() || data.empty()) {
            KAA_LOG_WARN("Failed to process incoming event: bad data");
            return;
        }

                if (fqn == "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest") {
            nsDeviceEventClassFamily :: DeviceInfoRequest event;
            AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoRequest > converter;
            converter.fromByteArray(data.data(), data.size(), event);
            onEvent(event, source);
        }
else         if (fqn == "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse") {
            nsDeviceEventClassFamily :: DeviceInfoResponse event;
            AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoResponse > converter;
            converter.fromByteArray(data.data(), data.size(), event);
            onEvent(event, source);
        }


    }

        void sendEventToAll(const nsDeviceEventClassFamily :: DeviceInfoRequest& event) {
        sendEvent(event);
    }

    void sendEvent(const nsDeviceEventClassFamily :: DeviceInfoRequest& event, const std::string& target = "")
    {
        std::ostringstream stream;
        AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoRequest > converter;
        converter.toByteArray(event, stream);
        const auto& encodedData = stream.str();
        std::vector<std::uint8_t> buffer(encodedData.begin(), encodedData.end());
        static const TransactionIdPtr empty(nullptr);
        eventManager_.produceEvent("org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest", buffer, target, empty);
    }

    void addEventToBlock(TransactionIdPtr trxId, const nsDeviceEventClassFamily :: DeviceInfoRequest& e, const std::string& target = "")
    {
        std::ostringstream stream;
        AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoRequest > converter;
        converter.toByteArray(e, stream);
        const auto& encodedData = stream.str();
        std::vector<std::uint8_t> buffer(encodedData.begin(), encodedData.end());
        eventManager_.produceEvent("org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest", buffer, target, trxId);
    }

    void sendEventToAll(const nsDeviceEventClassFamily :: DeviceInfoResponse& event) {
        sendEvent(event);
    }

    void sendEvent(const nsDeviceEventClassFamily :: DeviceInfoResponse& event, const std::string& target = "")
    {
        std::ostringstream stream;
        AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoResponse > converter;
        converter.toByteArray(event, stream);
        const auto& encodedData = stream.str();
        std::vector<std::uint8_t> buffer(encodedData.begin(), encodedData.end());
        static const TransactionIdPtr empty(nullptr);
        eventManager_.produceEvent("org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse", buffer, target, empty);
    }

    void addEventToBlock(TransactionIdPtr trxId, const nsDeviceEventClassFamily :: DeviceInfoResponse& e, const std::string& target = "")
    {
        std::ostringstream stream;
        AvroByteArrayConverter< nsDeviceEventClassFamily :: DeviceInfoResponse > converter;
        converter.toByteArray(e, stream);
        const auto& encodedData = stream.str();
        std::vector<std::uint8_t> buffer(encodedData.begin(), encodedData.end());
        eventManager_.produceEvent("org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse", buffer, target, trxId);
    }



    virtual const FQNList& getSupportedEventClassFQNs() {
        return eventFQNs_;
    }

    void addEventFamilyListener(DeviceEventClassFamilyListener* listener)
    {
        if (listener != nullptr) {
            listeners_.insert(listener);
        }
    }

    void removeEventFamilyListener(DeviceEventClassFamilyListener* listener)
    {
        if (listener != nullptr) {
            listeners_.erase(listener);
        }
    }

private:
    template<typename EventType>
    void onEvent(const EventType& event, const std::string& source)
    {
        for (auto* listener : listeners_) {
            listener->onEvent(event, source);
        }
    }

private:
    IEventManager&                           eventManager_;
    std::list<std::string>                   eventFQNs_;
    std::set<DeviceEventClassFamilyListener*>   listeners_;
};

}

#endif

#endif /* DeviceEventClassFamily_HPP_ */