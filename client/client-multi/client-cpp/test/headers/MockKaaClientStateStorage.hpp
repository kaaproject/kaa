/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#include <cstddef>

#include <kaa/IKaaClientStateStorage.hpp>

namespace kaa {

class MockKaaClientStateStorage : public IKaaClientStateStorage
{
public:
    virtual std::int32_t getEventSequenceNumber() const {
        return eventSequenceNumber_;
    }
    virtual void setEventSequenceNumber(std::int32_t sequenceNumber) {}

    virtual bool isRegistered() const {
        return isRegistered_;
    }
    virtual void setRegistered(bool isRegistered) {}

    virtual Topics getTopicList() const {
        return topics_;
    }
    virtual void setTopicList(const Topics& stateContainer) {}

    virtual std::int32_t getTopicListHash() {
        return topicListHash_;
    }
    virtual void setTopicListHash(const std::int32_t topicListHash) {}

    virtual HashDigest getProfileHash() const {
        return profileHash_;
    }
    virtual void setProfileHash(HashDigest hash) {}

    virtual AttachedEndpoints getAttachedEndpoints() const {
        return attachedEndpoints_;
    }
    virtual void setAttachedEndpoints(const AttachedEndpoints& endpoints) {}

    virtual std::string getEndpointAccessToken() {
        return endpointAccessToken_;
    }
    virtual void setEndpointAccessToken(const std::string& token) {}
    virtual std::string refreshEndpointAccessToken() {
        return endpointAccessToken_;
    }

    virtual bool getEndpointAttachStatus() const {
        return endpointAttachStatus_;
    }
    virtual void setEndpointAttachStatus(bool isAttached) {}

    virtual std::string getEndpointKeyHash() const {
        return endpointKeyHash_;
    }
    virtual void setEndpointKeyHash(const std::string& keyHash) {}

    virtual bool isSDKPropertiesUpdated() const {
        return isSDKPropertiesUpdated_;
    }

    virtual TopicStates& getTopicStates() {
        return topicStates_;
    }
    virtual void setTopicStates(const TopicStates& states) {}

    virtual bool isProfileResyncNeeded() const {
        return isProfileResyncNeeded_;
    }
    virtual void setProfileResyncNeeded(bool isNeeded) {
        ++onSetProfileResyncNeeded_;
        isProfileResyncNeeded_ = isNeeded;
    }

    virtual void read() {}
    virtual void save() {}

public:
    std::int32_t eventSequenceNumber_ = 0;
    bool isRegistered_                = false;
    Topics topics_;
    std::int32_t topicListHash_       = 0;
    HashDigest profileHash_;
    AttachedEndpoints attachedEndpoints_;
    std::string endpointAccessToken_;
    bool endpointAttachStatus_       = false;
    std::string endpointKeyHash_;
    bool isSDKPropertiesUpdated_     = false;
    TopicStates topicStates_;

    bool isProfileResyncNeeded_      = false;
    std::size_t onSetProfileResyncNeeded_ = 0;
};

}

#endif /* KAACLIENTSTATESTORAGEMOCK_HPP_ */
