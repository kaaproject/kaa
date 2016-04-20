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

#ifndef ICLIENTSTATESTORAGE_HPP_
#define ICLIENTSTATESTORAGE_HPP_

#include <cstdint>
#include <memory>
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/notification/gen/NotificationDefinitions.hpp"

namespace kaa {

typedef struct {
    std::int32_t eventSequenceNumber;
} SequenceNumber;

typedef std::map<std::string, std::string> AttachedEndpoints;
typedef std::map<std::int64_t, std::int32_t> TopicStates;

class IKaaClientStateStorage {
public:
    virtual ~IKaaClientStateStorage() {}

    virtual std::int32_t getEventSequenceNumber() const = 0;
    virtual void setEventSequenceNumber(std::int32_t sequenceNumber) = 0;

    virtual bool isRegistered() const = 0;
    virtual void setRegistered(bool isRegistered) = 0;

    virtual Topics getTopicList() const = 0;
    virtual void setTopicList(const Topics& stateContainer) = 0;

    virtual std::int32_t getTopicListHash() = 0;
    virtual void setTopicListHash(const std::int32_t topicListHash) = 0;

    virtual HashDigest getProfileHash() const = 0;
    virtual void setProfileHash(HashDigest hash) = 0;

    virtual AttachedEndpoints getAttachedEndpoints() const = 0;
    virtual void setAttachedEndpoints(const AttachedEndpoints& endpoints) = 0;

    virtual std::string getEndpointAccessToken() = 0;
    virtual void setEndpointAccessToken(const std::string& token) = 0;
    virtual std::string refreshEndpointAccessToken() = 0;

    virtual bool getEndpointAttachStatus() const = 0;
    virtual void setEndpointAttachStatus(bool isAttached) = 0;

    virtual std::string getEndpointKeyHash() const = 0;
    virtual void setEndpointKeyHash(const std::string& keyHash) = 0;

    virtual bool isSDKPropertiesUpdated() const = 0;

    virtual TopicStates& getTopicStates() = 0;
    virtual void setTopicStates(const TopicStates& states) = 0;

    virtual bool isProfileResyncNeeded() const = 0;
    virtual void setProfileResyncNeeded(bool isNeeded) = 0;

    virtual void read() = 0;
    virtual void save() = 0;
};

typedef std::shared_ptr<IKaaClientStateStorage> IKaaClientStateStoragePtr;

}  // namespace kaa


#endif /* ICLIENTSTATESTORAGE_HPP_ */
