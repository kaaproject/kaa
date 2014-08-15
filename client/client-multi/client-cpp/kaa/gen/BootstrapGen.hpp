/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef __KAA_GEN_BOOTSTRAPGEN_HPP_3726062528__H_
#define __KAA_GEN_BOOTSTRAPGEN_HPP_3726062528__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa {
struct Resolve {
    std::string Application_Token;
    Resolve() :
        Application_Token(std::string())
        { }
};

enum ChannelType {
    HTTP,
    HTTP_LP,
    BOOTSTRAP,
    KAATCP,
};

struct HTTPLPComunicationParameters {
    std::string hostName;
    int32_t port;
    HTTPLPComunicationParameters() :
        hostName(std::string()),
        port(int32_t())
        { }
};

struct HTTPComunicationParameters {
    std::string hostName;
    int32_t port;
    HTTPComunicationParameters() :
        hostName(std::string()),
        port(int32_t())
        { }
};

struct KaaTCPComunicationParameters {
    std::string hostName;
    int32_t port;
    KaaTCPComunicationParameters() :
        hostName(std::string()),
        port(int32_t())
        { }
};

struct _bootstrap_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    HTTPComunicationParameters get_HTTPComunicationParameters() const;
    void set_HTTPComunicationParameters(const HTTPComunicationParameters& v);
    HTTPLPComunicationParameters get_HTTPLPComunicationParameters() const;
    void set_HTTPLPComunicationParameters(const HTTPLPComunicationParameters& v);
    KaaTCPComunicationParameters get_KaaTCPComunicationParameters() const;
    void set_KaaTCPComunicationParameters(const KaaTCPComunicationParameters& v);
    _bootstrap_avsc_Union__0__();
};

struct SupportedChannel {
    typedef _bootstrap_avsc_Union__0__ communicationParameters_t;
    ChannelType channelType;
    communicationParameters_t communicationParameters;
    SupportedChannel() :
        channelType(ChannelType()),
        communicationParameters(communicationParameters_t())
        { }
};

struct OperationsServer {
    std::string name;
    int32_t priority;
    std::vector<uint8_t> publicKey;
    std::vector<SupportedChannel > supportedChannelsArray;
    OperationsServer() :
        name(std::string()),
        priority(int32_t()),
        publicKey(std::vector<uint8_t>()),
        supportedChannelsArray(std::vector<SupportedChannel >())
        { }
};

struct OperationsServerList {
    std::vector<OperationsServer > operationsServerArray;
    OperationsServerList() :
        operationsServerArray(std::vector<OperationsServer >())
        { }
};

struct _bootstrap_avsc_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    Resolve get_Resolve() const;
    void set_Resolve(const Resolve& v);
    ChannelType get_ChannelType() const;
    void set_ChannelType(const ChannelType& v);
    HTTPLPComunicationParameters get_HTTPLPComunicationParameters() const;
    void set_HTTPLPComunicationParameters(const HTTPLPComunicationParameters& v);
    HTTPComunicationParameters get_HTTPComunicationParameters() const;
    void set_HTTPComunicationParameters(const HTTPComunicationParameters& v);
    KaaTCPComunicationParameters get_KaaTCPComunicationParameters() const;
    void set_KaaTCPComunicationParameters(const KaaTCPComunicationParameters& v);
    OperationsServerList get_OperationsServerList() const;
    void set_OperationsServerList(const OperationsServerList& v);
    _bootstrap_avsc_Union__1__();
};

inline
HTTPComunicationParameters _bootstrap_avsc_Union__0__::get_HTTPComunicationParameters() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<HTTPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__0__::set_HTTPComunicationParameters(const HTTPComunicationParameters& v) {
    idx_ = 0;
    value_ = v;
}

inline
HTTPLPComunicationParameters _bootstrap_avsc_Union__0__::get_HTTPLPComunicationParameters() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<HTTPLPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__0__::set_HTTPLPComunicationParameters(const HTTPLPComunicationParameters& v) {
    idx_ = 1;
    value_ = v;
}

inline
KaaTCPComunicationParameters _bootstrap_avsc_Union__0__::get_KaaTCPComunicationParameters() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<KaaTCPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__0__::set_KaaTCPComunicationParameters(const KaaTCPComunicationParameters& v) {
    idx_ = 2;
    value_ = v;
}

inline
Resolve _bootstrap_avsc_Union__1__::get_Resolve() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<Resolve >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_Resolve(const Resolve& v) {
    idx_ = 0;
    value_ = v;
}

inline
ChannelType _bootstrap_avsc_Union__1__::get_ChannelType() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<ChannelType >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_ChannelType(const ChannelType& v) {
    idx_ = 1;
    value_ = v;
}

inline
HTTPLPComunicationParameters _bootstrap_avsc_Union__1__::get_HTTPLPComunicationParameters() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<HTTPLPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_HTTPLPComunicationParameters(const HTTPLPComunicationParameters& v) {
    idx_ = 2;
    value_ = v;
}

inline
HTTPComunicationParameters _bootstrap_avsc_Union__1__::get_HTTPComunicationParameters() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<HTTPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_HTTPComunicationParameters(const HTTPComunicationParameters& v) {
    idx_ = 3;
    value_ = v;
}

inline
KaaTCPComunicationParameters _bootstrap_avsc_Union__1__::get_KaaTCPComunicationParameters() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<KaaTCPComunicationParameters >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_KaaTCPComunicationParameters(const KaaTCPComunicationParameters& v) {
    idx_ = 4;
    value_ = v;
}

inline
OperationsServerList _bootstrap_avsc_Union__1__::get_OperationsServerList() const {
    if (idx_ != 5) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<OperationsServerList >(value_);
}

inline
void _bootstrap_avsc_Union__1__::set_OperationsServerList(const OperationsServerList& v) {
    idx_ = 5;
    value_ = v;
}

inline _bootstrap_avsc_Union__0__::_bootstrap_avsc_Union__0__() : idx_(0), value_(HTTPComunicationParameters()) { }
inline _bootstrap_avsc_Union__1__::_bootstrap_avsc_Union__1__() : idx_(0), value_(Resolve()) { }
}
namespace avro {
template<> struct codec_traits<kaa::Resolve> {
    static void encode(Encoder& e, const kaa::Resolve& v) {
        avro::encode(e, v.Application_Token);
    }
    static void decode(Decoder& d, kaa::Resolve& v) {
        avro::decode(d, v.Application_Token);
    }
};

template<> struct codec_traits<kaa::ChannelType> {
    static void encode(Encoder& e, kaa::ChannelType v) {
		if (v < kaa::HTTP || v > kaa::KAATCP)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa::ChannelType and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa::ChannelType& v) {
		size_t index = d.decodeEnum();
		if (index < kaa::HTTP || index > kaa::KAATCP)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa::ChannelType and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa::ChannelType>(index);
    }
};

template<> struct codec_traits<kaa::HTTPLPComunicationParameters> {
    static void encode(Encoder& e, const kaa::HTTPLPComunicationParameters& v) {
        avro::encode(e, v.hostName);
        avro::encode(e, v.port);
    }
    static void decode(Decoder& d, kaa::HTTPLPComunicationParameters& v) {
        avro::decode(d, v.hostName);
        avro::decode(d, v.port);
    }
};

template<> struct codec_traits<kaa::HTTPComunicationParameters> {
    static void encode(Encoder& e, const kaa::HTTPComunicationParameters& v) {
        avro::encode(e, v.hostName);
        avro::encode(e, v.port);
    }
    static void decode(Decoder& d, kaa::HTTPComunicationParameters& v) {
        avro::decode(d, v.hostName);
        avro::decode(d, v.port);
    }
};

template<> struct codec_traits<kaa::KaaTCPComunicationParameters> {
    static void encode(Encoder& e, const kaa::KaaTCPComunicationParameters& v) {
        avro::encode(e, v.hostName);
        avro::encode(e, v.port);
    }
    static void decode(Decoder& d, kaa::KaaTCPComunicationParameters& v) {
        avro::decode(d, v.hostName);
        avro::decode(d, v.port);
    }
};

template<> struct codec_traits<kaa::_bootstrap_avsc_Union__0__> {
    static void encode(Encoder& e, kaa::_bootstrap_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_HTTPComunicationParameters());
            break;
        case 1:
            avro::encode(e, v.get_HTTPLPComunicationParameters());
            break;
        case 2:
            avro::encode(e, v.get_KaaTCPComunicationParameters());
            break;
        }
    }
    static void decode(Decoder& d, kaa::_bootstrap_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 3) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa::HTTPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_HTTPComunicationParameters(vv);
            }
            break;
        case 1:
            {
                kaa::HTTPLPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_HTTPLPComunicationParameters(vv);
            }
            break;
        case 2:
            {
                kaa::KaaTCPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_KaaTCPComunicationParameters(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa::SupportedChannel> {
    static void encode(Encoder& e, const kaa::SupportedChannel& v) {
        avro::encode(e, v.channelType);
        avro::encode(e, v.communicationParameters);
    }
    static void decode(Decoder& d, kaa::SupportedChannel& v) {
        avro::decode(d, v.channelType);
        avro::decode(d, v.communicationParameters);
    }
};

template<> struct codec_traits<kaa::OperationsServer> {
    static void encode(Encoder& e, const kaa::OperationsServer& v) {
        avro::encode(e, v.name);
        avro::encode(e, v.priority);
        avro::encode(e, v.publicKey);
        avro::encode(e, v.supportedChannelsArray);
    }
    static void decode(Decoder& d, kaa::OperationsServer& v) {
        avro::decode(d, v.name);
        avro::decode(d, v.priority);
        avro::decode(d, v.publicKey);
        avro::decode(d, v.supportedChannelsArray);
    }
};

template<> struct codec_traits<kaa::OperationsServerList> {
    static void encode(Encoder& e, const kaa::OperationsServerList& v) {
        avro::encode(e, v.operationsServerArray);
    }
    static void decode(Decoder& d, kaa::OperationsServerList& v) {
        avro::decode(d, v.operationsServerArray);
    }
};

template<> struct codec_traits<kaa::_bootstrap_avsc_Union__1__> {
    static void encode(Encoder& e, kaa::_bootstrap_avsc_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_Resolve());
            break;
        case 1:
            avro::encode(e, v.get_ChannelType());
            break;
        case 2:
            avro::encode(e, v.get_HTTPLPComunicationParameters());
            break;
        case 3:
            avro::encode(e, v.get_HTTPComunicationParameters());
            break;
        case 4:
            avro::encode(e, v.get_KaaTCPComunicationParameters());
            break;
        case 5:
            avro::encode(e, v.get_OperationsServerList());
            break;
        }
    }
    static void decode(Decoder& d, kaa::_bootstrap_avsc_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 6) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa::Resolve vv;
                avro::decode(d, vv);
                v.set_Resolve(vv);
            }
            break;
        case 1:
            {
                kaa::ChannelType vv;
                avro::decode(d, vv);
                v.set_ChannelType(vv);
            }
            break;
        case 2:
            {
                kaa::HTTPLPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_HTTPLPComunicationParameters(vv);
            }
            break;
        case 3:
            {
                kaa::HTTPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_HTTPComunicationParameters(vv);
            }
            break;
        case 4:
            {
                kaa::KaaTCPComunicationParameters vv;
                avro::decode(d, vv);
                v.set_KaaTCPComunicationParameters(vv);
            }
            break;
        case 5:
            {
                kaa::OperationsServerList vv;
                avro::decode(d, vv);
                v.set_OperationsServerList(vv);
            }
            break;
        }
    }
};

}
#endif
