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

#ifndef CLIENTSTATUS_HPP_
#define CLIENTSTATUS_HPP_

#include <string>
#include <map>
#include <boost/cstdint.hpp>
#include <boost/bimap.hpp>
#include <boost/thread/mutex.hpp>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/IKaaClientStateStorage.hpp"

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
    ClientStatus(const std::string& filename);
    ~ClientStatus() { }

    boost::int32_t getEventSequenceNumber() const;
    void setEventSequenceNumber(boost::int32_t sequenceNumber);

    boost::int32_t getConfigurationSequenceNumber() const;
    void setConfigurationSequenceNumber(boost::int32_t sequenceNumber);

    boost::int32_t getNotificationSequenceNumber() const;
    void setNotificationSequenceNumber(boost::int32_t sequenceNumber);

    SequenceNumber getAppSeqNumber() const;
    void setAppSeqNumber(SequenceNumber appSeqNumber);

    bool isRegistered() const;
    void setRegistered(bool isRegistered);

    DetailedTopicStates getTopicStates() const;
    void setTopicStates(const DetailedTopicStates& stateContainer);

    SharedDataBuffer getProfileHash() const;
    void setProfileHash(SharedDataBuffer hash);

    AttachedEndpoints getAttachedEndpoints() const;
    void setAttachedEndpoints(const AttachedEndpoints& endpoints);

    std::string getEndpointAccessToken() const;
    void setEndpointAccessToken(const std::string& token);

    bool getEndpointAttachStatus() const;
    void setEndpointAttachStatus(bool isAttached);

    std::string getEndpointKeyHash() const;
    void setEndpointKeyHash(const std::string& keyHash);

    void read();
    void save();

private:
    typedef boost::mutex                            mutex_type;
    typedef boost::unique_lock<mutex_type>          lock_type;

    std::string filename_;
    std::map<ClientParameterT, boost::shared_ptr<IPersistentParameter> > parameters_;

    mutable mutex_type                      sequenceNumberGuard_;

    static const bimap                      parameterToToken_;
    static const SequenceNumber             appSeqNumberDefault_;
    static const bool                       isRegisteredDefault_;
    static const SharedDataBuffer           endpointHashDefault_;
    static const DetailedTopicStates        topicStatesDefault_;
    static const AttachedEndpoints          attachedEndpoints_;
    static const std::string                endpointAccessToken_;
    static const bool                       endpointDefaultAttachStatus_;
    static const std::string                endpointKeyHashDefault_;
};

}


#endif /* CLIENTSTATUS_HPP_ */
