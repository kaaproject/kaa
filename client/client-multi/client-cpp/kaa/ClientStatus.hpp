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

#ifndef CLIENTSTATUS_HPP_
#define CLIENTSTATUS_HPP_

#include <string>
#include <map>
#include <cstdint>
#include <memory>
#include <boost/bimap.hpp>

#include "kaa/KaaThread.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

/* Fwd declarations */
enum class ClientParameterT;
class IPersistentParameter;

typedef boost::bimaps::bimap<
          boost::bimaps::set_of<ClientParameterT>   /* Client parameter type */
        , boost::bimaps::set_of<std::string>        /* String token for mapping in status file */
        , boost::bimaps::left_based
> bimap;

class ClientStatus : public IKaaClientStateStorage {
public:
    ClientStatus(IKaaClientContext& context);
    ~ClientStatus() { }

    std::int32_t getEventSequenceNumber() const;
    void setEventSequenceNumber(std::int32_t sequenceNumber);

    bool isRegistered() const;
    void setRegistered(bool isRegistered);

    Topics getTopicList() const;
    void setTopicList(const Topics& topicList);

    HashDigest getProfileHash() const;
    void setProfileHash(HashDigest hash);

    AttachedEndpoints getAttachedEndpoints() const;
    void setAttachedEndpoints(const AttachedEndpoints& endpoints);

    std::string getEndpointAccessToken();
    void setEndpointAccessToken(const std::string& token);
    std::string refreshEndpointAccessToken();

    bool getEndpointAttachStatus() const;
    void setEndpointAttachStatus(bool isAttached);

    std::string getEndpointKeyHash() const;
    void setEndpointKeyHash(const std::string& keyHash);

    void setTopicListHash(const std::int32_t topicListHash);
    std::int32_t getTopicListHash();

    void setTopicStates(const TopicStates& subscriptions);
    TopicStates& getTopicStates();

    virtual bool isSDKPropertiesUpdated() const { return isSDKPropertiesForUpdated_; }

    virtual bool isProfileResyncNeeded() const;
    virtual void setProfileResyncNeeded(bool isNeeded);

    void read();
    void save();

private:
    void checkSDKPropertiesForUpdates();
    /* Helpers */
    template< ClientParameterT Type, class ParameterData >
    void setParameterData(const ParameterData& data);

    template< ClientParameterT Type, class ParameterData >
    void setParameterDataWithEqualCheck(const ParameterData& data);

    template< ClientParameterT Type, class ParameterData >
    ParameterData getParameterData(const ParameterData& defaultValue) const;

private:
    const std::string filename_;
    std::map<ClientParameterT, std::shared_ptr<IPersistentParameter> > parameters_;

    bool            isSDKPropertiesForUpdated_;
    bool            hasUpdate_;
    TopicStates    topicStates_;

    IKaaClientContext &context_;


    static const bimap                      parameterToToken_;
    static const std::int32_t               eventSeqNumberDefault_;
    static const bool                       isRegisteredDefault_;
    static const HashDigest                 endpointHashDefault_;
    static const Topics                     topicListDefault_;
    static const std::int32_t               topicListHashDefault_;
    static const AttachedEndpoints          attachedEndpoints_;
    static const bool                       endpointDefaultAttachStatus_;
    static const std::string                endpointKeyHashDefault_;
    static const bool                       isProfileResyncNeededDefault_;
};

}


#endif /* CLIENTSTATUS_HPP_ */
