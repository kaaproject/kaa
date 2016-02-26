/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
    virtual void setNotificationsSubscriptions(std::map<std::int64_t, std::int32_t>& subscriptions) {}
    virtual std::map<std::int64_t, std::int32_t> &getNotificationsSubscriptions() { return map; }

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

    virtual Topics getTopicList() const {
        static Topics states;
        return states;
    }

    virtual void setTopicList(const Topics& stateContainer) {}

    virtual void setTopicStates(std::map<std::int64_t, std::int32_t>& subscriptions) {}
    virtual std::map<std::int64_t, std::int32_t> &getTopicStates() { return map; }

    virtual HashDigest getProfileHash() const {
        static HashDigest profileHash;
        return profileHash;
    }
    virtual void setProfileHash(HashDigest) {}

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

    virtual bool isSDKPropertiesUpdated() const {
        return false;
    }

    virtual std::int32_t getTopicListHash() { return 0; }
    virtual void setTopicListHash(std::int32_t hash) {}

    virtual void read() {}
    virtual void save() {}
 private:
    std::map<std::int64_t, std::int32_t> map;
};

}

#endif /* KAACLIENTSTATESTORAGEMOCK_HPP_ */
