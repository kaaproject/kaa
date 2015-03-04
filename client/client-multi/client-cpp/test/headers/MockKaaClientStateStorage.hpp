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

#ifndef KAACLIENTSTATESTORAGEMOCK_HPP_
#define KAACLIENTSTATESTORAGEMOCK_HPP_

#include <kaa/IKaaClientStateStorage.hpp>

namespace kaa {

class MockKaaClientStateStorage : public IKaaClientStateStorage
{
public:
    virtual std::int32_t getEventSequenceNumber() const {
        return 0;
    }

    virtual void setEventSequenceNumber(std::int32_t) {}

    virtual std::int32_t getConfigurationSequenceNumber() const  {
        return 0;
    }

    virtual void setConfigurationSequenceNumber(std::int32_t) {}

    virtual std::int32_t getNotificationSequenceNumber() const  {
        return 0;
    }

    virtual void setNotificationSequenceNumber(std::int32_t) {}

    virtual SequenceNumber getAppSeqNumber() const  {
        static SequenceNumber sn;
        return sn;
    }
    virtual void setAppSeqNumber(SequenceNumber) {}

    virtual bool isRegistered() const {
        return true;
    }

    virtual void setRegistered(bool) {}

    virtual DetailedTopicStates getTopicStates() const {
        static DetailedTopicStates states;
        return states;
    }
    virtual void setTopicStates(const DetailedTopicStates&) {}

    virtual SharedDataBuffer getProfileHash() const {
        static SharedDataBuffer profileHash;
        return profileHash;
    }
    virtual void setProfileHash(SharedDataBuffer) {}

    virtual AttachedEndpoints getAttachedEndpoints() const {
        static AttachedEndpoints endpoints;
        return endpoints;
    }

    virtual void setAttachedEndpoints(const AttachedEndpoints&) {}

    virtual std::string getEndpointAccessToken() {
        static std::string token("token");
        return token;
    }

    std::string refreshEndpointAccessToken() {
        static std::string token("token");
        return token;
    }

    virtual void setEndpointAccessToken(const std::string&) {}

    virtual bool getEndpointAttachStatus() const {
        return false;
    }
    virtual void setEndpointAttachStatus(bool) {}

    virtual std::string getEndpointKeyHash() const {
        static std::string hash("hash");
        return hash;
    }
    virtual void setEndpointKeyHash(const std::string& ) {}

    virtual bool isConfigurationVersionUpdated() const {
        return false;
    }

    virtual void read() {}
    virtual void save() {}
};

}

#endif /* KAACLIENTSTATESTORAGEMOCK_HPP_ */
