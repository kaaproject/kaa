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

#ifndef EVENTFAMILYFACTORY_HPP_
#define EVENTFAMILYFACTORY_HPP_

#include "boost/shared_ptr.hpp"

#include "kaa/event/IEventManager.hpp"

#include "kaa/event/gen/BasicEventFamily.hpp"

namespace kaa {

class EventFamilyFactory {
public:
    EventFamilyFactory(IEventManager& manager)
        : eventManager_(manager) {}

    BasicEventFamily& getBasicEventFamily()
    {
        if(!concreteEventFamily_){
            concreteEventFamily_.reset(new BasicEventFamily(eventManager_));
            addEventFamilyByName("concreteEventFamily", concreteEventFamily_);
        }

        return *concreteEventFamily_;
    }

private:
    IEventManager& eventManager_;
    std::set<std::string> efcNames_;
    std::map<std::string, boost::shared_ptr<IEventFamily> > eventFamilies_;
    std::map<std::string, FQNList > supportedFQNLists_;

    boost::shared_ptr<IEventFamily> getEventFamilyByName(const std::string& efcName) {
        auto it = eventFamilies_.find(efcName);
        if (it != eventFamilies_.end()) {
            return it->second;
        }
        return boost::shared_ptr<IEventFamily>();
    }

    void addEventFamilyByName(const std::string& efcName, boost::shared_ptr<IEventFamily> eventFamily) {
        eventManager_.registerEventFamily(eventFamily.get());
        eventFamilies_[efcName] = eventFamily;
    }

    const FQNList& getSupportedFQNsByFamilyName(const std::string& efcName) {
        auto it = supportedFQNLists_.find(efcName);
        if (it != supportedFQNLists_.end()) {
            return it->second;
        }
        static const FQNList empty;
        return empty;
    }

    const std::set<std::string> &getEventFamilyClassNames() {
        return efcNames_;
    }

    boost::shared_ptr<BasicEventFamily> concreteEventFamily_;
};

} /* namespace kaa */

#endif /* EVENTFAMILYFACTORY_HPP_ */
