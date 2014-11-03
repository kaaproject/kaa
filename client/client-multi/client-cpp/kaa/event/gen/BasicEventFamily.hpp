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

#ifndef CONCRETEEVENTFAMILY_HPP_
#define CONCRETEEVENTFAMILY_HPP_

#include "kaa/KaaDefaults.hpp"

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

namespace kaa {

class BasicEventFamily : public IEventFamily
{
public:
    class BasicEventFamilyListener
    {
    public:
        virtual void onEvent(const Topic& event, const std::string& source) = 0;
        virtual ~BasicEventFamilyListener() {}
    };

public:
    BasicEventFamily(IEventManager& manager)
        : eventManager_(manager)
    {
        eventNames_ = {"{event.event_name}"}; /* format: {"name1","name2","name3"} */
    }

    virtual void onGenericEvent(const std::string& fqn
                              , const std::vector<std::uint8_t>& data
                              , const std::string& source)
    {
        if (fqn.empty() || data.empty()) {
            KAA_LOG_WARN("Failed to process incoming event: bad data");
            return;
        }

        if (fqn == "{event.event_name}") {
            Topic event;
            AvroByteArrayConverter<Topic> converter;
            converter.fromByteArray(data.data(), data.size(), event);
            onEvent(event, source);
        }
    }

    void sendEvent(const Topic& e, const std::string& target)
    {
        std::ostringstream stream;
        AvroByteArrayConverter<Topic> converter;
        converter.toByteArray(e, stream);
        const auto& encodedData = stream.str();
        std::vector<std::uint8_t> buffer(encodedData.begin(), encodedData.end());
        eventManager_.produceEvent("{event.event_name}", buffer, target);
    }

    virtual const FQNList& getSupportedEventClassFQNs() {
        return eventNames_;
    }

    void addEventFamilyListener(BasicEventFamilyListener* listener)
    {
        if (listener != nullptr) {
            listeners_.insert(listener);
        }
    }

    void removeEventFamilyListener(BasicEventFamilyListener* listener)
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
    IEventManager&                        eventManager_;
    std::list<std::string>                eventNames_;
    std::set<BasicEventFamilyListener*>   listeners_;
};

}

#endif

#endif /* CONCRETEEVENTFAMILY_HPP_ */
