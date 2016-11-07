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

#include "kaa/ClientStatus.hpp"

#include <fstream>
#include <iomanip>
#include <string>
#include <sstream>

#include "kaa/logging/Log.hpp"
#include "kaa/common/UuidGenerator.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

enum class ClientParameterT {
    EVENT_SEQUENCE_NUMBER,
    IS_REGISTERED,
    PROFILE_HASH,
    TOPIC_LIST,
    TOPIC_LIST_HASH,
    ATTACHED_ENDPOINTS,
    EP_ACCESS_TOKEN,
    EP_ATTACH_STATUS,
    EP_KEY_HASH,
    PROPERTIES_HASH,
    IS_PROFILE_RESYNC_NEEDED
};

class IPersistentParameter {
public:
    virtual ~IPersistentParameter() {}
    virtual void save(std::ostream &os) = 0;
    virtual void read(const std::string &strValue) = 0;
    virtual boost::any getValue() const = 0;
    virtual void setValue(boost::any v) = 0;
};

static bimap create_bimap()
{
    bimap bi;
    bi.left.insert(bimap::left_value_type(ClientParameterT::EVENT_SEQUENCE_NUMBER,    "app_seq_number"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::IS_REGISTERED,            "is_registered"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::TOPIC_LIST,               "topic_list"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::TOPIC_LIST_HASH,          "topic_list_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::PROFILE_HASH,             "profile_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::ATTACHED_ENDPOINTS,       "attached_endpoints"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_ACCESS_TOKEN,          "access_token"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_ATTACH_STATUS,         "ep_attach_status"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_KEY_HASH,              "ep_key_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::PROPERTIES_HASH,          "properties_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::IS_PROFILE_RESYNC_NEEDED, "is_profile_resync"));
    return bi;
}

const bimap                 ClientStatus::parameterToToken_ =   create_bimap();
const std::int32_t          ClientStatus::eventSeqNumberDefault_        = 0;
const bool                  ClientStatus::isRegisteredDefault_          = false;
const HashDigest            ClientStatus::endpointHashDefault_;
const Topics                ClientStatus::topicListDefault_;
const std::int32_t          ClientStatus::topicListHashDefault_         = 0;
const AttachedEndpoints     ClientStatus::attachedEndpoints_;
const bool                  ClientStatus::endpointDefaultAttachStatus_  = false;
const std::string           ClientStatus::endpointKeyHashDefault_;
const bool                  ClientStatus::isProfileResyncNeededDefault_ = false;

static std::string convertToByteArrayString(const std::string & str)
{
    const std::uint8_t * bytes = reinterpret_cast<const std::uint8_t *>(str.data());
    std::size_t length = str.length();

    std::stringstream ss;
    for (std::size_t i = 0; i < length; ++i) {
        if (i != 0) {
            ss << " ";
        }
        ss << std::setw(2) << std::setfill('0') << std::hex << (int)bytes[i];
    }
    return ss.str();
}

static std::string convertFromByteArrayString(const std::string & str)
{
    std::string input = str;
    input.append(" ");
    std::stringstream output;
    std::size_t bytes_count = input.length() / 3;
    for (std::size_t i = 0; i < bytes_count; ++i) {
        std::string bytestr = input.substr(i * 3, 2);
        output << (char)std::stoi(bytestr, nullptr, 16);
    }
    return output.str();
}

template <typename T>
class ClientParameter : public IPersistentParameter {
public:
    ClientParameter(const std::string& name, const T& v) : attributeName_(name) {
        value_ = v;
    }
    void save(std::ostream &os);
    void read(const std::string &strValue);
    boost::any getValue() const { return value_; }
    void setValue(boost::any v) { value_ = boost::any_cast<const T&>(v); }
private:
    std::string attributeName_;
    T value_;
};

template<>
void ClientParameter<std::int32_t>::save(std::ostream &os)
{
    os << attributeName_ << "=" << value_ << std::endl;
}

template<>
void ClientParameter<bool>::save(std::ostream &os) {
    os << attributeName_ << "=" << value_ << std::endl;
}

template<>
void ClientParameter<std::string>::save(std::ostream &os) {
    if (!value_.empty()) {
        os << attributeName_ << "=" << value_ << std::endl;
    }
}

template<>
void ClientParameter<Topics>::save(std::ostream &os)
{
    if (value_.begin() != value_.end()) {
        os << attributeName_ << "=";
        for (auto it = value_.begin(); it != value_.end(); ++it) {
            if (it != value_.begin()) {
                os << ",";
            }

            os << "[" << it->id << ","
                      << convertToByteArrayString(it->name) << ","
                      << (it->subscriptionType == SubscriptionType::MANDATORY_SUBSCRIPTION ? "m" : "v")
                      << "]";
        }
        os << std::endl;
    }
}

template<>
void ClientParameter<AttachedEndpoints>::save(std::ostream &os)
{
    if (value_.begin() != value_.end()) {
        os << attributeName_ << "=";
        for (auto it = value_.begin(); it != value_.end(); ++it) {
            if (it != value_.begin()) {
                os << ",";
            }

            os << "[" << convertToByteArrayString(it->second) << ","
                      << convertToByteArrayString(it->second) << "]";
        }
        os << std::endl;
    }
}

template<>
void ClientParameter<HashDigest>::save(std::ostream &os)
{
    if (!value_.empty()) {
        os << attributeName_ << "=";
        for (auto it = value_.begin(); it != value_.end(); ++it) {
            os << std::setw(2) << std::setfill('0')  << std::hex << (int)*it;
        }
        os << std::dec << std::setw(1) << std::endl;
    }
}

template<typename T>
T convert(const std::string &strValue)
{
    T val;
    std::stringstream stream(strValue);
    stream >> val;
    return val;

}

template<>
void ClientParameter<std::int32_t>::read(const std::string &strValue)
{
    value_ = convert<std::int32_t>(strValue);
}

template<>
void ClientParameter<std::uint64_t>::read(const std::string &strValue)
{
    value_ = convert<std::uint64_t>(strValue);
}

template<>
void ClientParameter<bool>::read(const std::string &strValue)
{
    value_ = convert<bool>(strValue);
}

template<>
void ClientParameter<std::string>::read(const std::string &strValue)
{
    value_ = strValue;
}

template<>
void ClientParameter<Topics>::read(const std::string &strValue)
{
    value_.clear();

    if (!strValue.empty()) {
        std::size_t begin_pos = 0;
        bool parse_more = true;
        do {
            std::size_t open_brace_pos = strValue.find_first_of('[', begin_pos);
            std::size_t close_brace_pos = strValue.find_first_of(']', begin_pos);

            std::size_t comma1pos = strValue.find_first_of(',', open_brace_pos);
            std::size_t comma2pos = strValue.find_first_of(',', comma1pos + 1);

            if (comma1pos == std::string::npos || comma2pos == std::string::npos) {
                break;
            }

            std::string topicId = strValue.substr(open_brace_pos+1, comma1pos - open_brace_pos - 1 );
            std::string topicName = strValue.substr(comma1pos+1, comma2pos - comma1pos - 1) ;
            std::string sType = strValue.substr(comma2pos+1, close_brace_pos - comma2pos - 1);

            std::size_t commapos = strValue.find_first_of(",", close_brace_pos);
            parse_more = commapos != std::string::npos;

            begin_pos = close_brace_pos + 1;

            Topic topic;
            topic.id = convert<std::int64_t>(topicId);
            topic.name = convertFromByteArrayString(topicName);
            topic.subscriptionType = (sType.compare("m") == 0 ? SubscriptionType::MANDATORY_SUBSCRIPTION : SubscriptionType::OPTIONAL_SUBSCRIPTION);

            value_.push_back(topic);
        } while (parse_more);
    }
}

template<>
void ClientParameter<AttachedEndpoints>::read(const std::string &strValue)
{
    value_.clear();

    if (!strValue.empty()) {
        std::size_t begin_pos = 0;
        bool parse_more = true;
        do {
            std::size_t open_brace_pos = strValue.find_first_of('[', begin_pos);
            std::size_t close_brace_pos = strValue.find_first_of(']', begin_pos);

            std::size_t comma1pos = strValue.find_first_of(',', open_brace_pos);

            if (comma1pos == std::string::npos) {
                break;
            }

            std::string token = strValue.substr(open_brace_pos+1, comma1pos - open_brace_pos - 1 );
            std::string hash = strValue.substr(comma1pos+1, close_brace_pos - comma1pos - 1);

            std::size_t commapos = strValue.find_first_of(",", close_brace_pos);
            parse_more = commapos != std::string::npos;
            begin_pos = close_brace_pos + 1;

            value_.insert(std::make_pair(token, hash));
        } while (parse_more);
    }
}

template<>
void ClientParameter<HashDigest>::read(const std::string &strValue)
{
    size_t size = strValue.length() / 2;
    value_ = HashDigest(size);
    for (size_t i = 0; i < size; ++i) {
        value_[i] = std::stoi(strValue.substr(2*i, 2), nullptr, 16);
    }
}

  ClientStatus::ClientStatus(IKaaClientContext& context)
      : filename_(context.getProperties().getStateFileName()),
        isSDKPropertiesForUpdated_(false),
        hasUpdate_(false),
        context_(context)
{
    auto eventSeqNumberTokenParamToken = parameterToToken_.left.find(ClientParameterT::EVENT_SEQUENCE_NUMBER);
    if (eventSeqNumberTokenParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> eventSeqNumberParam(
                new ClientParameter<std::int32_t>(eventSeqNumberTokenParamToken->second, eventSeqNumberDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::EVENT_SEQUENCE_NUMBER, eventSeqNumberParam));
    }
    auto isRegisteredParamToken = parameterToToken_.left.find(ClientParameterT::IS_REGISTERED);
    if (isRegisteredParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> isRegisteredParam(new ClientParameter<bool>(
                isRegisteredParamToken->second, isRegisteredDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::IS_REGISTERED, isRegisteredParam));
    }
    auto topicListParamToken = parameterToToken_.left.find(ClientParameterT::TOPIC_LIST);
    if (topicListParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> topicListParam(new ClientParameter<Topics>(
                topicListParamToken->second, topicListDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::TOPIC_LIST, topicListParam));
    }
    auto topicListHashParamToken = parameterToToken_.left.find(ClientParameterT::TOPIC_LIST_HASH);
    if (topicListHashParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> topicListHashParam(new ClientParameter<std::int32_t>(
                topicListHashParamToken->second, topicListHashDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::TOPIC_LIST_HASH, topicListHashParam));
    }
    auto endpointHashParamToken = parameterToToken_.left.find(ClientParameterT::PROFILE_HASH);
    if (endpointHashParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> profileHashParam(new ClientParameter<HashDigest>(
                endpointHashParamToken->second, endpointHashDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::PROFILE_HASH, profileHashParam));
    }
    auto attachedEndpointsParamToken = parameterToToken_.left.find(ClientParameterT::ATTACHED_ENDPOINTS);
    if (attachedEndpointsParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> attachedEndpointsParam(new ClientParameter<AttachedEndpoints>(
                attachedEndpointsParamToken->second, attachedEndpoints_));
        parameters_.insert(std::make_pair(ClientParameterT::ATTACHED_ENDPOINTS, attachedEndpointsParam));
    }
    auto endpointAccessTokenParamToken = parameterToToken_.left.find(ClientParameterT::EP_ACCESS_TOKEN);
    if (endpointAccessTokenParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> endpointAccessTokenParam(
                new ClientParameter<std::string>(endpointAccessTokenParamToken->second, ""));
        parameters_.insert(std::make_pair(ClientParameterT::EP_ACCESS_TOKEN, endpointAccessTokenParam));
    }
    auto endpointAttachStatusParamToken = parameterToToken_.left.find(ClientParameterT::EP_ATTACH_STATUS);
    if (endpointAttachStatusParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> isEndpointAttachedParam(new ClientParameter<bool>(
                endpointAttachStatusParamToken->second, endpointDefaultAttachStatus_));
        parameters_.insert(std::make_pair(ClientParameterT::EP_ATTACH_STATUS, isEndpointAttachedParam));
    }
    auto endpointKeyHashParamToken = parameterToToken_.left.find(ClientParameterT::EP_KEY_HASH);
    if (endpointKeyHashParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> endpointKeyHashParam(new ClientParameter<std::string>(
                endpointKeyHashParamToken->second, endpointKeyHashDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::EP_KEY_HASH, endpointKeyHashParam));
    }
    auto propertiesHashParamToken = parameterToToken_.left.find(ClientParameterT::PROPERTIES_HASH);
    if (propertiesHashParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> propertiesHashParam(new ClientParameter<HashDigest>(
                propertiesHashParamToken->second, endpointHashDefault_/*It's OK*/));
        parameters_.insert(std::make_pair(ClientParameterT::PROPERTIES_HASH, propertiesHashParam));
    }
    auto isProfileResyncNeededParamToken = parameterToToken_.left.find(ClientParameterT::IS_PROFILE_RESYNC_NEEDED);
    if (isProfileResyncNeededParamToken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> isProfileResyncNeededParam(new ClientParameter<bool>(
                isProfileResyncNeededParamToken->second, isProfileResyncNeededDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::IS_PROFILE_RESYNC_NEEDED, isProfileResyncNeededParam));
    }

    this->read();

    checkSDKPropertiesForUpdates();
}

void ClientStatus::checkSDKPropertiesForUpdates()
{
    HashDigest currentPropertiesHash = getPropertiesHash();

    auto parameter_it = parameters_.find(ClientParameterT::PROPERTIES_HASH);

    auto storedPropertiesHash = boost::any_cast<HashDigest>(parameter_it->second->getValue());

    if (storedPropertiesHash == endpointHashDefault_) {
        parameter_it->second->setValue(currentPropertiesHash);
        hasUpdate_ = true;
        KAA_LOG_INFO("SDK properties are up to date");
    } else {
        if (currentPropertiesHash != storedPropertiesHash) {
            parameter_it->second->setValue(currentPropertiesHash);

            setRegistered(false);
            isSDKPropertiesForUpdated_ = true;
            hasUpdate_ = true;
            KAA_LOG_INFO("SDK properties were updated");
        } else {
            KAA_LOG_INFO("SDK properties are up to date");
        }
    }
}

template< ClientParameterT Type, class ParameterData >
void ClientStatus::setParameterData(const ParameterData& data)
{
    auto parameter_it = parameters_.find(Type);
    if (parameter_it != parameters_.end()) {
        parameter_it->second->setValue(data);
        hasUpdate_ = true;
    }
}

template< ClientParameterT Type, class ParameterData >
void ClientStatus::setParameterDataWithEqualCheck(const ParameterData& data)
{
    auto parameter_it = parameters_.find(Type);
    if (parameter_it != parameters_.end() && data != boost::any_cast<ParameterData>(parameter_it->second->getValue())) {
        parameter_it->second->setValue(data);
        hasUpdate_ = true;
    }
}

template< ClientParameterT Type, class ParameterData >
ParameterData ClientStatus::getParameterData(const ParameterData& defaultValue) const
{
    auto parameter_it = parameters_.find(Type);
    if (parameter_it != parameters_.end()) {
        return boost::any_cast<ParameterData>(parameter_it->second->getValue());
    }

    return defaultValue;
}

bool ClientStatus::isRegistered() const
{
    return getParameterData<ClientParameterT::IS_REGISTERED>(isRegisteredDefault_);
}

void ClientStatus::setRegistered(bool isRegisteredP)
{
    setParameterDataWithEqualCheck<ClientParameterT::IS_REGISTERED>(isRegisteredP);
}

bool ClientStatus::isProfileResyncNeeded() const
{
    return getParameterData<ClientParameterT::IS_PROFILE_RESYNC_NEEDED>(isProfileResyncNeededDefault_);
}

void ClientStatus::setProfileResyncNeeded(bool isNeeded)
{
    setParameterDataWithEqualCheck<ClientParameterT::IS_PROFILE_RESYNC_NEEDED>(isNeeded);
}

std::string ClientStatus::getEndpointAccessToken()
{
    std::string token;
    auto found = getParameterData<ClientParameterT::EP_ACCESS_TOKEN>(token);

    if (!found.empty()) {
        return found;
    }

    return refreshEndpointAccessToken();
}

void ClientStatus::setEndpointAccessToken(const std::string& token)
{
    setParameterDataWithEqualCheck<ClientParameterT::EP_ACCESS_TOKEN>(token);
    setProfileResyncNeeded(true);
}

std::string ClientStatus::refreshEndpointAccessToken()
{
    std::string token(UuidGenerator::generateUuid());
    setEndpointAccessToken(token);
    setProfileResyncNeeded(false);
    return token;
}

Topics ClientStatus::getTopicList() const
{
    return getParameterData<ClientParameterT::TOPIC_LIST>(topicListDefault_);
}

void ClientStatus::setTopicList(const Topics& topicList)
{
    setParameterData<ClientParameterT::TOPIC_LIST>(topicList);
}

AttachedEndpoints ClientStatus::getAttachedEndpoints() const
{
    return getParameterData<ClientParameterT::ATTACHED_ENDPOINTS>(attachedEndpoints_);
}

void ClientStatus::setAttachedEndpoints(const AttachedEndpoints& endpoints)
{
    setParameterData<ClientParameterT::ATTACHED_ENDPOINTS>(endpoints);
}

HashDigest ClientStatus::getProfileHash() const
{
    return getParameterData<ClientParameterT::PROFILE_HASH>(endpointHashDefault_);
}

void ClientStatus::setProfileHash(HashDigest hash)
{
    setParameterDataWithEqualCheck<ClientParameterT::PROFILE_HASH>(hash);
}

bool ClientStatus::getEndpointAttachStatus() const
{
    return getParameterData<ClientParameterT::EP_ATTACH_STATUS>(endpointDefaultAttachStatus_);
}

void ClientStatus::setEndpointAttachStatus(bool isAttached)
{
    setParameterDataWithEqualCheck<ClientParameterT::EP_ATTACH_STATUS>(isAttached);
}

void ClientStatus::read()
{
    std::ifstream stateFile(filename_);
    std::string value;
    std::string token;

    /* First read topicList hash */
    if (stateFile.good()) {
        std::getline(stateFile, token, '=');
        std::getline(stateFile, value);

        std::stringstream stream(value);
        std::int64_t topicId;
        std::int32_t sqn;
        while ((stream >> topicId) && (stream >> sqn)) {
            topicStates_.insert(std::make_pair(topicId, sqn));
        }
    }

    while (stateFile.good()) {
        std::getline(stateFile, token, '=');
        auto par_t_it = parameterToToken_.right.find(token);
        if (par_t_it != parameterToToken_.right.end()) {
            ClientParameterT par_type = par_t_it->second;
            auto it = parameters_.find(par_type);
            if (it != parameters_.end()) {
                std::getline(stateFile, value);
                it->second->read(value);
            }
        }
    }

    KAA_LOG_DEBUG(boost::format("Read topic list hash: %1%") % getTopicListHash());

    stateFile.close();
}

void ClientStatus::save()
{
    if (!hasUpdate_) {
        return;
    }

    std::ofstream stateFile(filename_);

    /* Save topic list hash */
    if (stateFile.good()) {
        KAA_LOG_DEBUG("Persisting topic states");
        stateFile << "topic_states=";
        for (auto &subscription : topicStates_) {
             stateFile << subscription.first << ' ' << subscription.second << ' ';
        }
        stateFile << std::endl;
    }

    /* Save other parameters */
    for (auto parameter : parameters_) {
        parameter.second->save(stateFile);
    }

    stateFile.close();

    hasUpdate_ = false;
}

std::int32_t ClientStatus::getEventSequenceNumber() const
{
    return getParameterData<ClientParameterT::EVENT_SEQUENCE_NUMBER>(eventSeqNumberDefault_);
}

void ClientStatus::setEventSequenceNumber(std::int32_t sequenceNumber)
{
    setParameterDataWithEqualCheck<ClientParameterT::EVENT_SEQUENCE_NUMBER>(sequenceNumber);
}

std::string ClientStatus::getEndpointKeyHash() const
{
    return getParameterData<ClientParameterT::EP_KEY_HASH>(endpointKeyHashDefault_);
}

void ClientStatus::setEndpointKeyHash(const std::string& keyHash)
{
    setParameterDataWithEqualCheck<ClientParameterT::EP_KEY_HASH>(keyHash);
}

void ClientStatus::setTopicListHash(const std::int32_t topicListHash)
{
    setParameterDataWithEqualCheck<ClientParameterT::TOPIC_LIST_HASH>(topicListHash);
}

std::int32_t ClientStatus::getTopicListHash()
{
    return getParameterData<ClientParameterT::TOPIC_LIST_HASH>(topicListHashDefault_);
}

void ClientStatus::setTopicStates(const TopicStates& subscriptions)
{
    topicStates_ = subscriptions;
    hasUpdate_ = true;
}

std::map<std::int64_t, std::int32_t>& ClientStatus::getTopicStates()
{
    return topicStates_;
}

}
