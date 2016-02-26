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
    APPSEQUENCENUMBER,
    ISREGISTERED,
    PROFILEHASH,
    TOPICLIST,
    ATTACHED_ENDPOINTS,
    EP_ACCESS_TOKEN,
    EP_ATTACH_STATUS,
    EP_KEY_HASH,
    PROPERTIES_HASH
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
    bi.left.insert(bimap::left_value_type(ClientParameterT::APPSEQUENCENUMBER,     "app_seq_number"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::ISREGISTERED,          "is_registered"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::TOPICLIST,             "topic_list"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::PROFILEHASH,           "profile_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::ATTACHED_ENDPOINTS,    "attached_endpoints"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_ACCESS_TOKEN,       "access_token"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_ATTACH_STATUS,      "ep_attach_status"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::EP_KEY_HASH,           "ep_key_hash"));
    bi.left.insert(bimap::left_value_type(ClientParameterT::PROPERTIES_HASH,       "properties_hash"));
    return bi;
}

const bimap                 ClientStatus::parameterToToken_ =   create_bimap();
const SequenceNumber        ClientStatus::appSeqNumberDefault_ =         { 0 };
const bool                  ClientStatus::isRegisteredDefault_ =         false;
const HashDigest            ClientStatus::endpointHashDefault_;
const Topics                ClientStatus::topicList_;
const AttachedEndpoints     ClientStatus::attachedEndpoints_;
const bool                  ClientStatus::endpointDefaultAttachStatus_ = false;
const std::string           ClientStatus::endpointKeyHashDefault_;

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
void ClientParameter<SequenceNumber>::save(std::ostream &os)
{
    std::stringstream ss;
    ss << value_.eventSequenceNumber;
    os << attributeName_ << "=" << ss.str() << std::endl;
}

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

template<>
void ClientParameter<SequenceNumber>::read(const std::string &strValue)
{
    value_ = SequenceNumber();

    if (!strValue.empty()) {
        value_.eventSequenceNumber = std::stoi(strValue, nullptr, 10);
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
            topic.id = std::stoll(topicId);
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

  ClientStatus::ClientStatus(IKaaClientContext& context) : filename_(context.getProperties().getStateFileName()), isSDKPropertiesForUpdated_(false),
                                                           hasUpdate_(false), context_(context), topicListHash_(0)
{
    auto appseqntoken = parameterToToken_.left.find(ClientParameterT::APPSEQUENCENUMBER);
    if (appseqntoken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> appSeqNumber(
                new ClientParameter<SequenceNumber>(appseqntoken->second, appSeqNumberDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::APPSEQUENCENUMBER, appSeqNumber));
    }
    auto isregisteredtoken = parameterToToken_.left.find(ClientParameterT::ISREGISTERED);
    if (isregisteredtoken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> isRegistered(new ClientParameter<bool>(
                isregisteredtoken->second, isRegisteredDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::ISREGISTERED, isRegistered));
    }
    auto topicstatestoken = parameterToToken_.left.find(ClientParameterT::TOPICLIST);
    if (topicstatestoken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> topicList(new ClientParameter<Topics>(
                topicstatestoken->second, topicList_));
        parameters_.insert(std::make_pair(ClientParameterT::TOPICLIST, topicList));
    }
    auto endpointhashtoken = parameterToToken_.left.find(ClientParameterT::PROFILEHASH);
    if (endpointhashtoken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> endpointHash(new ClientParameter<HashDigest>(
                endpointhashtoken->second, endpointHashDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::PROFILEHASH, endpointHash));
    }
    auto attachedendpoints = parameterToToken_.left.find(ClientParameterT::ATTACHED_ENDPOINTS);
    if (attachedendpoints != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> attachedEndpoints(new ClientParameter<AttachedEndpoints>(
                attachedendpoints->second, attachedEndpoints_));
        parameters_.insert(std::make_pair(ClientParameterT::ATTACHED_ENDPOINTS, attachedEndpoints));
    }
    auto endpointaccesstoken = parameterToToken_.left.find(ClientParameterT::EP_ACCESS_TOKEN);
    if (endpointaccesstoken != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> endpointAccessToken(
                new ClientParameter<std::string>(endpointaccesstoken->second, ""));
        parameters_.insert(std::make_pair(ClientParameterT::EP_ACCESS_TOKEN, endpointAccessToken));
    }
    auto endpointattachstatus = parameterToToken_.left.find(ClientParameterT::EP_ATTACH_STATUS);
    if (endpointattachstatus != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> isEndpointAttached(new ClientParameter<bool>(
                endpointattachstatus->second, endpointDefaultAttachStatus_));
        parameters_.insert(std::make_pair(ClientParameterT::EP_ATTACH_STATUS, isEndpointAttached));
    }
    auto endpointkeyhash = parameterToToken_.left.find(ClientParameterT::EP_KEY_HASH);
    if (endpointkeyhash != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> endpointKeyHash(new ClientParameter<std::string>(
                endpointkeyhash->second, endpointKeyHashDefault_));
        parameters_.insert(std::make_pair(ClientParameterT::EP_KEY_HASH, endpointKeyHash));
    }

    auto propertieshash = parameterToToken_.left.find(ClientParameterT::PROPERTIES_HASH);
    if (propertieshash != parameterToToken_.left.end()) {
        std::shared_ptr<IPersistentParameter> propertiesHash(new ClientParameter<HashDigest>(
                propertieshash->second, endpointHashDefault_/*It's OK*/));
        parameters_.insert(std::make_pair(ClientParameterT::PROPERTIES_HASH, propertiesHash));
    }

    this->read();

    checkSDKPropertiesForUpdates();
}

void ClientStatus::checkSDKPropertiesForUpdates()
{
    HashDigest truePropertiesHash = getPropertiesHash();
    HashDigest storedPropertiesHash;

    auto parameter_it = parameters_.find(ClientParameterT::PROPERTIES_HASH);
    if (parameter_it != parameters_.end()) {
        storedPropertiesHash = boost::any_cast<HashDigest>(parameter_it->second->getValue());
    }

    if (truePropertiesHash != storedPropertiesHash) {
        setRegistered(false);
        auto it = parameters_.find(ClientParameterT::PROPERTIES_HASH);
        if (it != parameters_.end()) {
            it->second->setValue(truePropertiesHash);
        }

        isSDKPropertiesForUpdated_ = true;
        KAA_LOG_INFO("SDK properties were updated");
    } else {
        KAA_LOG_INFO("SDK properties are up to date");
    }
}

SequenceNumber ClientStatus::getAppSeqNumber() const
{
    auto parameter_it = parameters_.find(ClientParameterT::APPSEQUENCENUMBER);
    if (parameter_it != parameters_.end()) {
        return boost::any_cast<SequenceNumber>(parameter_it->second->getValue());
    }
    return appSeqNumberDefault_;
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
ParameterData ClientStatus::getParameterData(const ParameterData& defaultValue) const
{
    auto parameter_it = parameters_.find(Type);
    if (parameter_it != parameters_.end()) {
        return boost::any_cast<ParameterData>(parameter_it->second->getValue());
    }

    return defaultValue;
}

void ClientStatus::setAppSeqNumber(SequenceNumber appSeqNumber)
{
    setParameterData<ClientParameterT::APPSEQUENCENUMBER>(appSeqNumber);
}

bool ClientStatus::isRegistered() const
{
    return getParameterData<ClientParameterT::ISREGISTERED>(isRegisteredDefault_);
}

void ClientStatus::setRegistered(bool isRegisteredP)
{
    if (isRegistered() !=  isRegisteredP) {
        setParameterData<ClientParameterT::ISREGISTERED>(isRegisteredP);
    }
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
    setParameterData<ClientParameterT::EP_ACCESS_TOKEN>(token);
}

std::string ClientStatus::refreshEndpointAccessToken()
{
    std::string token(UuidGenerator::generateUuid());
    setEndpointAccessToken(token);
    return token;
}

Topics ClientStatus::getTopicList() const
{
    return getParameterData<ClientParameterT::TOPICLIST>(topicList_);
}

void ClientStatus::setTopicList(const Topics& topicList)
{
    setParameterData<ClientParameterT::TOPICLIST>(topicList);
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
    return getParameterData<ClientParameterT::PROFILEHASH>(endpointHashDefault_);
}

void ClientStatus::setProfileHash(HashDigest hash)
{
    setParameterData<ClientParameterT::PROFILEHASH>(hash);
}

bool ClientStatus::getEndpointAttachStatus() const
{
    return getParameterData<ClientParameterT::EP_ATTACH_STATUS>(endpointDefaultAttachStatus_);
}

void ClientStatus::setEndpointAttachStatus(bool isAttached)
{
    setParameterData<ClientParameterT::EP_ATTACH_STATUS>(isAttached);
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
        topicListHash_ = std::stoi(value);
        KAA_LOG_DEBUG(boost::format("Read topic list hash: %1%") % topicListHash_);
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
        KAA_LOG_DEBUG(boost::format("Persisting topic list hash: %1%") % topicListHash_);
        stateFile << "topic_list_hash=" << topicListHash_ << std::endl;
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
    KAA_MUTEX_UNIQUE_DECLARE(lock, sequenceNumberGuard_);
    return getAppSeqNumber().eventSequenceNumber;
}

void ClientStatus::setEventSequenceNumber(std::int32_t sequenceNumber)
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, sequenceNumberGuard_);
    SequenceNumber sn = getAppSeqNumber();
    sn.eventSequenceNumber = sequenceNumber;
    setAppSeqNumber(sn);
}

std::string ClientStatus::getEndpointKeyHash() const
{
    return getParameterData<ClientParameterT::EP_KEY_HASH>(endpointKeyHashDefault_);
}

void ClientStatus::setEndpointKeyHash(const std::string& keyHash)
{
    setParameterData<ClientParameterT::EP_KEY_HASH>(keyHash);
}

void ClientStatus::setTopicListHash(const std::int32_t topicListHash)
{
    topicListHash_ = topicListHash;
    hasUpdate_ = true;
}

std::int32_t ClientStatus::getTopicListHash()
{
    return topicListHash_;
}

void ClientStatus::setTopicStates(std::map<std::int64_t, std::int32_t>& subscriptions)
{
	topicStates_ = subscriptions;
	hasUpdate_ = true;
}

std::map<std::int64_t, std::int32_t>& ClientStatus::getTopicStates()
{
    return topicStates_;
}

}
