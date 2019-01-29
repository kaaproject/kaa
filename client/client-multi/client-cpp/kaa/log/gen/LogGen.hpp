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


#ifndef KAA_LOG_GEN_LOGGEN_HPP_2380542215__H_
#define KAA_LOG_GEN_LOGGEN_HPP_2380542215__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_log {
struct KeyValueData {
    std::string propertyName;
    std::string propertyValue;
    KeyValueData() :
        propertyName(std::string()),
        propertyValue(std::string())
        { }
};

struct DeviceData {
    std::vector<KeyValueData > KeyValueProperties;
    DeviceData() :
        KeyValueProperties(std::vector<KeyValueData >())
        { }
};

struct TableMetadata {
    std::string DatabaseName;
    std::string DatabaseTableName;
    std::vector<std::string > TableColumns;
    TableMetadata() :
        DatabaseName(std::string()),
        DatabaseTableName(std::string()),
        TableColumns(std::vector<std::string >())
        { }
};

struct DatabasesMeatdata {
    std::vector<TableMetadata > TablesMetadata;
    DatabasesMeatdata() :
        TablesMetadata(std::vector<TableMetadata >())
        { }
};

struct OpcUaTag {
    int32_t namespace_id;
    std::string path;
    OpcUaTag() :
        namespace_id(int32_t()),
        path(std::string())
        { }
};

struct OpcUaTagInfo {
    std::string host;
    int32_t port;
    std::vector<OpcUaTag > tags;
    OpcUaTagInfo() :
        host(std::string()),
        port(int32_t()),
        tags(std::vector<OpcUaTag >())
        { }
};

struct OpcUaTagsMetadata {
    std::vector<OpcUaTagInfo > OpcUaTagsInfo;
    OpcUaTagsMetadata() :
        OpcUaTagsInfo(std::vector<OpcUaTagInfo >())
        { }
};

struct _log_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    DeviceData get_DeviceData() const;
    void set_DeviceData(const DeviceData& v);
    DatabasesMeatdata get_DatabasesMeatdata() const;
    void set_DatabasesMeatdata(const DatabasesMeatdata& v);
    OpcUaTagsMetadata get_OpcUaTagsMetadata() const;
    void set_OpcUaTagsMetadata(const OpcUaTagsMetadata& v);
    _log_avsc_Union__0__();
};

struct ActorReport {
    typedef _log_avsc_Union__0__ data_t;
    int64_t timestamp;
    data_t data;
    ActorReport() :
        timestamp(int64_t()),
        data(data_t())
        { }
};

inline
DeviceData _log_avsc_Union__0__::get_DeviceData() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceData >(value_);
}

inline
void _log_avsc_Union__0__::set_DeviceData(const DeviceData& v) {
    idx_ = 0;
    value_ = v;
}

inline
DatabasesMeatdata _log_avsc_Union__0__::get_DatabasesMeatdata() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DatabasesMeatdata >(value_);
}

inline
void _log_avsc_Union__0__::set_DatabasesMeatdata(const DatabasesMeatdata& v) {
    idx_ = 1;
    value_ = v;
}

inline
OpcUaTagsMetadata _log_avsc_Union__0__::get_OpcUaTagsMetadata() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<OpcUaTagsMetadata >(value_);
}

inline
void _log_avsc_Union__0__::set_OpcUaTagsMetadata(const OpcUaTagsMetadata& v) {
    idx_ = 2;
    value_ = v;
}

inline _log_avsc_Union__0__::_log_avsc_Union__0__() : idx_(0), value_(DeviceData()) { }
}
namespace avro {
template<> struct codec_traits<kaa_log::KeyValueData> {
    static void encode(Encoder& e, const kaa_log::KeyValueData& v) {
        avro::encode(e, v.propertyName);
        avro::encode(e, v.propertyValue);
    }
    static void decode(Decoder& d, kaa_log::KeyValueData& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.propertyName);
                    break;
                case 1:
                    avro::decode(d, v.propertyValue);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.propertyName);
            avro::decode(d, v.propertyValue);
        }
    }
};

template<> struct codec_traits<kaa_log::DeviceData> {
    static void encode(Encoder& e, const kaa_log::DeviceData& v) {
        avro::encode(e, v.KeyValueProperties);
    }
    static void decode(Decoder& d, kaa_log::DeviceData& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.KeyValueProperties);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.KeyValueProperties);
        }
    }
};

template<> struct codec_traits<kaa_log::TableMetadata> {
    static void encode(Encoder& e, const kaa_log::TableMetadata& v) {
        avro::encode(e, v.DatabaseName);
        avro::encode(e, v.DatabaseTableName);
        avro::encode(e, v.TableColumns);
    }
    static void decode(Decoder& d, kaa_log::TableMetadata& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.DatabaseName);
                    break;
                case 1:
                    avro::decode(d, v.DatabaseTableName);
                    break;
                case 2:
                    avro::decode(d, v.TableColumns);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.DatabaseName);
            avro::decode(d, v.DatabaseTableName);
            avro::decode(d, v.TableColumns);
        }
    }
};

template<> struct codec_traits<kaa_log::DatabasesMeatdata> {
    static void encode(Encoder& e, const kaa_log::DatabasesMeatdata& v) {
        avro::encode(e, v.TablesMetadata);
    }
    static void decode(Decoder& d, kaa_log::DatabasesMeatdata& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.TablesMetadata);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.TablesMetadata);
        }
    }
};

template<> struct codec_traits<kaa_log::OpcUaTag> {
    static void encode(Encoder& e, const kaa_log::OpcUaTag& v) {
        avro::encode(e, v.namespace_id);
        avro::encode(e, v.path);
    }
    static void decode(Decoder& d, kaa_log::OpcUaTag& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.namespace_id);
                    break;
                case 1:
                    avro::decode(d, v.path);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.namespace_id);
            avro::decode(d, v.path);
        }
    }
};

template<> struct codec_traits<kaa_log::OpcUaTagInfo> {
    static void encode(Encoder& e, const kaa_log::OpcUaTagInfo& v) {
        avro::encode(e, v.host);
        avro::encode(e, v.port);
        avro::encode(e, v.tags);
    }
    static void decode(Decoder& d, kaa_log::OpcUaTagInfo& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.host);
                    break;
                case 1:
                    avro::decode(d, v.port);
                    break;
                case 2:
                    avro::decode(d, v.tags);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.host);
            avro::decode(d, v.port);
            avro::decode(d, v.tags);
        }
    }
};

template<> struct codec_traits<kaa_log::OpcUaTagsMetadata> {
    static void encode(Encoder& e, const kaa_log::OpcUaTagsMetadata& v) {
        avro::encode(e, v.OpcUaTagsInfo);
    }
    static void decode(Decoder& d, kaa_log::OpcUaTagsMetadata& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.OpcUaTagsInfo);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.OpcUaTagsInfo);
        }
    }
};

template<> struct codec_traits<kaa_log::_log_avsc_Union__0__> {
    static void encode(Encoder& e, kaa_log::_log_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_DeviceData());
            break;
        case 1:
            avro::encode(e, v.get_DatabasesMeatdata());
            break;
        case 2:
            avro::encode(e, v.get_OpcUaTagsMetadata());
            break;
        }
    }
    static void decode(Decoder& d, kaa_log::_log_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 3) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_log::DeviceData vv;
                avro::decode(d, vv);
                v.set_DeviceData(vv);
            }
            break;
        case 1:
            {
                kaa_log::DatabasesMeatdata vv;
                avro::decode(d, vv);
                v.set_DatabasesMeatdata(vv);
            }
            break;
        case 2:
            {
                kaa_log::OpcUaTagsMetadata vv;
                avro::decode(d, vv);
                v.set_OpcUaTagsMetadata(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa_log::ActorReport> {
    static void encode(Encoder& e, const kaa_log::ActorReport& v) {
        avro::encode(e, v.timestamp);
        avro::encode(e, v.data);
    }
    static void decode(Decoder& d, kaa_log::ActorReport& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.timestamp);
                    break;
                case 1:
                    avro::decode(d, v.data);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.timestamp);
            avro::decode(d, v.data);
        }
    }
};

}
#endif
