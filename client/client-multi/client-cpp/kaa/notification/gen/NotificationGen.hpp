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


#ifndef KAA_NOTIFICATION_GEN_NOTIFICATIONGEN_HPP_2380542215__H_
#define KAA_NOTIFICATION_GEN_NOTIFICATIONGEN_HPP_2380542215__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_notification {
struct SoftwareComponent {
    std::string downloadPath;
    bool needRestart;
    SoftwareComponent() :
        downloadPath(std::string()),
        needRestart(bool())
        { }
};

struct SoftwareUpdates {
    std::string host;
    int32_t port;
    std::string user;
    std::string password;
    std::vector<SoftwareComponent > components;
    SoftwareUpdates() :
        host(std::string()),
        port(int32_t()),
        user(std::string()),
        password(std::string()),
        components(std::vector<SoftwareComponent >())
        { }
};

struct _notification_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    float get_float() const;
    void set_float(const float& v);
    double get_double() const;
    void set_double(const double& v);
    int32_t get_int() const;
    void set_int(const int32_t& v);
    int64_t get_long() const;
    void set_long(const int64_t& v);
    bool get_bool() const;
    void set_bool(const bool& v);
    _notification_avsc_Union__0__();
};

struct update_tag {
    typedef _notification_avsc_Union__0__ value_t;
    int32_t namespace_id;
    std::string path;
    value_t value;
    std::string host;
    int32_t port;
    update_tag() :
        namespace_id(int32_t()),
        path(std::string()),
        value(value_t()),
        host(std::string()),
        port(int32_t())
        { }
};

struct tagUpdates {
    std::vector<update_tag > tag;
    tagUpdates() :
        tag(std::vector<update_tag >())
        { }
};

struct sql_insert {
    std::string statement;
    std::string database_id;
    sql_insert() :
        statement(std::string()),
        database_id(std::string())
        { }
};

struct can_message {
    std::string interface;
    int32_t id;
    std::string payload;
    can_message() :
        interface(std::string()),
        id(int32_t()),
        payload(std::string())
        { }
};

struct node_id {
    int32_t namespace_id;
    std::string node_identifier;
    node_id() :
        namespace_id(int32_t()),
        node_identifier(std::string())
        { }
};

struct _notification_avsc_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    double get_double() const;
    void set_double(const double& v);
    float get_float() const;
    void set_float(const float& v);
    int32_t get_int() const;
    void set_int(const int32_t& v);
    int64_t get_long() const;
    void set_long(const int64_t& v);
    bool get_bool() const;
    void set_bool(const bool& v);
    node_id get_node_id() const;
    void set_node_id(const node_id& v);
    _notification_avsc_Union__1__();
};

struct _notification_avsc_Union__2__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::vector<_notification_avsc_Union__1__ > get_array() const;
    void set_array(const std::vector<_notification_avsc_Union__1__ >& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _notification_avsc_Union__2__();
};

struct opcua_method {
    typedef _notification_avsc_Union__2__ params_t;
    std::string server_id;
    int32_t node_namespace;
    std::string node_id;
    int32_t method_namesapce;
    std::string method_id;
    params_t params;
    opcua_method() :
        server_id(std::string()),
        node_namespace(int32_t()),
        node_id(std::string()),
        method_namesapce(int32_t()),
        method_id(std::string()),
        params(params_t())
        { }
};

struct _notification_avsc_Union__3__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    SoftwareUpdates get_SoftwareUpdates() const;
    void set_SoftwareUpdates(const SoftwareUpdates& v);
    tagUpdates get_tagUpdates() const;
    void set_tagUpdates(const tagUpdates& v);
    sql_insert get_sql_insert() const;
    void set_sql_insert(const sql_insert& v);
    can_message get_can_message() const;
    void set_can_message(const can_message& v);
    opcua_method get_opcua_method() const;
    void set_opcua_method(const opcua_method& v);
    _notification_avsc_Union__3__();
};

struct Notification {
    typedef _notification_avsc_Union__3__ notification_t;
    notification_t notification;
    Notification() :
        notification(notification_t())
        { }
};

inline
std::string _notification_avsc_Union__0__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _notification_avsc_Union__0__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
float _notification_avsc_Union__0__::get_float() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<float >(value_);
}

inline
void _notification_avsc_Union__0__::set_float(const float& v) {
    idx_ = 1;
    value_ = v;
}

inline
double _notification_avsc_Union__0__::get_double() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<double >(value_);
}

inline
void _notification_avsc_Union__0__::set_double(const double& v) {
    idx_ = 2;
    value_ = v;
}

inline
int32_t _notification_avsc_Union__0__::get_int() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int32_t >(value_);
}

inline
void _notification_avsc_Union__0__::set_int(const int32_t& v) {
    idx_ = 3;
    value_ = v;
}

inline
int64_t _notification_avsc_Union__0__::get_long() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int64_t >(value_);
}

inline
void _notification_avsc_Union__0__::set_long(const int64_t& v) {
    idx_ = 4;
    value_ = v;
}

inline
bool _notification_avsc_Union__0__::get_bool() const {
    if (idx_ != 5) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<bool >(value_);
}

inline
void _notification_avsc_Union__0__::set_bool(const bool& v) {
    idx_ = 5;
    value_ = v;
}

inline
std::string _notification_avsc_Union__1__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _notification_avsc_Union__1__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
double _notification_avsc_Union__1__::get_double() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<double >(value_);
}

inline
void _notification_avsc_Union__1__::set_double(const double& v) {
    idx_ = 1;
    value_ = v;
}

inline
float _notification_avsc_Union__1__::get_float() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<float >(value_);
}

inline
void _notification_avsc_Union__1__::set_float(const float& v) {
    idx_ = 2;
    value_ = v;
}

inline
int32_t _notification_avsc_Union__1__::get_int() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int32_t >(value_);
}

inline
void _notification_avsc_Union__1__::set_int(const int32_t& v) {
    idx_ = 3;
    value_ = v;
}

inline
int64_t _notification_avsc_Union__1__::get_long() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int64_t >(value_);
}

inline
void _notification_avsc_Union__1__::set_long(const int64_t& v) {
    idx_ = 4;
    value_ = v;
}

inline
bool _notification_avsc_Union__1__::get_bool() const {
    if (idx_ != 5) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<bool >(value_);
}

inline
void _notification_avsc_Union__1__::set_bool(const bool& v) {
    idx_ = 5;
    value_ = v;
}

inline
node_id _notification_avsc_Union__1__::get_node_id() const {
    if (idx_ != 6) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<node_id >(value_);
}

inline
void _notification_avsc_Union__1__::set_node_id(const node_id& v) {
    idx_ = 6;
    value_ = v;
}

inline
std::vector<_notification_avsc_Union__1__ > _notification_avsc_Union__2__::get_array() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::vector<_notification_avsc_Union__1__ > >(value_);
}

inline
void _notification_avsc_Union__2__::set_array(const std::vector<_notification_avsc_Union__1__ >& v) {
    idx_ = 0;
    value_ = v;
}

inline
SoftwareUpdates _notification_avsc_Union__3__::get_SoftwareUpdates() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<SoftwareUpdates >(value_);
}

inline
void _notification_avsc_Union__3__::set_SoftwareUpdates(const SoftwareUpdates& v) {
    idx_ = 0;
    value_ = v;
}

inline
tagUpdates _notification_avsc_Union__3__::get_tagUpdates() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<tagUpdates >(value_);
}

inline
void _notification_avsc_Union__3__::set_tagUpdates(const tagUpdates& v) {
    idx_ = 1;
    value_ = v;
}

inline
sql_insert _notification_avsc_Union__3__::get_sql_insert() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<sql_insert >(value_);
}

inline
void _notification_avsc_Union__3__::set_sql_insert(const sql_insert& v) {
    idx_ = 2;
    value_ = v;
}

inline
can_message _notification_avsc_Union__3__::get_can_message() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<can_message >(value_);
}

inline
void _notification_avsc_Union__3__::set_can_message(const can_message& v) {
    idx_ = 3;
    value_ = v;
}

inline
opcua_method _notification_avsc_Union__3__::get_opcua_method() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<opcua_method >(value_);
}

inline
void _notification_avsc_Union__3__::set_opcua_method(const opcua_method& v) {
    idx_ = 4;
    value_ = v;
}

inline _notification_avsc_Union__0__::_notification_avsc_Union__0__() : idx_(0), value_(std::string()) { }
inline _notification_avsc_Union__1__::_notification_avsc_Union__1__() : idx_(0), value_(std::string()) { }
inline _notification_avsc_Union__2__::_notification_avsc_Union__2__() : idx_(0), value_(std::vector<_notification_avsc_Union__1__ >()) { }
inline _notification_avsc_Union__3__::_notification_avsc_Union__3__() : idx_(0), value_(SoftwareUpdates()) { }
}
namespace avro {
template<> struct codec_traits<kaa_notification::SoftwareComponent> {
    static void encode(Encoder& e, const kaa_notification::SoftwareComponent& v) {
        avro::encode(e, v.downloadPath);
        avro::encode(e, v.needRestart);
    }
    static void decode(Decoder& d, kaa_notification::SoftwareComponent& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.downloadPath);
                    break;
                case 1:
                    avro::decode(d, v.needRestart);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.downloadPath);
            avro::decode(d, v.needRestart);
        }
    }
};

template<> struct codec_traits<kaa_notification::SoftwareUpdates> {
    static void encode(Encoder& e, const kaa_notification::SoftwareUpdates& v) {
        avro::encode(e, v.host);
        avro::encode(e, v.port);
        avro::encode(e, v.user);
        avro::encode(e, v.password);
        avro::encode(e, v.components);
    }
    static void decode(Decoder& d, kaa_notification::SoftwareUpdates& v) {
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
                    avro::decode(d, v.user);
                    break;
                case 3:
                    avro::decode(d, v.password);
                    break;
                case 4:
                    avro::decode(d, v.components);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.host);
            avro::decode(d, v.port);
            avro::decode(d, v.user);
            avro::decode(d, v.password);
            avro::decode(d, v.components);
        }
    }
};

template<> struct codec_traits<kaa_notification::_notification_avsc_Union__0__> {
    static void encode(Encoder& e, kaa_notification::_notification_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            avro::encode(e, v.get_float());
            break;
        case 2:
            avro::encode(e, v.get_double());
            break;
        case 3:
            avro::encode(e, v.get_int());
            break;
        case 4:
            avro::encode(e, v.get_long());
            break;
        case 5:
            avro::encode(e, v.get_bool());
            break;
        }
    }
    static void decode(Decoder& d, kaa_notification::_notification_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 6) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            {
                float vv;
                avro::decode(d, vv);
                v.set_float(vv);
            }
            break;
        case 2:
            {
                double vv;
                avro::decode(d, vv);
                v.set_double(vv);
            }
            break;
        case 3:
            {
                int32_t vv;
                avro::decode(d, vv);
                v.set_int(vv);
            }
            break;
        case 4:
            {
                int64_t vv;
                avro::decode(d, vv);
                v.set_long(vv);
            }
            break;
        case 5:
            {
                bool vv;
                avro::decode(d, vv);
                v.set_bool(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa_notification::update_tag> {
    static void encode(Encoder& e, const kaa_notification::update_tag& v) {
        avro::encode(e, v.namespace_id);
        avro::encode(e, v.path);
        avro::encode(e, v.value);
        avro::encode(e, v.host);
        avro::encode(e, v.port);
    }
    static void decode(Decoder& d, kaa_notification::update_tag& v) {
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
                case 2:
                    avro::decode(d, v.value);
                    break;
                case 3:
                    avro::decode(d, v.host);
                    break;
                case 4:
                    avro::decode(d, v.port);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.namespace_id);
            avro::decode(d, v.path);
            avro::decode(d, v.value);
            avro::decode(d, v.host);
            avro::decode(d, v.port);
        }
    }
};

template<> struct codec_traits<kaa_notification::tagUpdates> {
    static void encode(Encoder& e, const kaa_notification::tagUpdates& v) {
        avro::encode(e, v.tag);
    }
    static void decode(Decoder& d, kaa_notification::tagUpdates& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.tag);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.tag);
        }
    }
};

template<> struct codec_traits<kaa_notification::sql_insert> {
    static void encode(Encoder& e, const kaa_notification::sql_insert& v) {
        avro::encode(e, v.statement);
        avro::encode(e, v.database_id);
    }
    static void decode(Decoder& d, kaa_notification::sql_insert& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.statement);
                    break;
                case 1:
                    avro::decode(d, v.database_id);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.statement);
            avro::decode(d, v.database_id);
        }
    }
};

template<> struct codec_traits<kaa_notification::can_message> {
    static void encode(Encoder& e, const kaa_notification::can_message& v) {
        avro::encode(e, v.interface);
        avro::encode(e, v.id);
        avro::encode(e, v.payload);
    }
    static void decode(Decoder& d, kaa_notification::can_message& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.interface);
                    break;
                case 1:
                    avro::decode(d, v.id);
                    break;
                case 2:
                    avro::decode(d, v.payload);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.interface);
            avro::decode(d, v.id);
            avro::decode(d, v.payload);
        }
    }
};

template<> struct codec_traits<kaa_notification::node_id> {
    static void encode(Encoder& e, const kaa_notification::node_id& v) {
        avro::encode(e, v.namespace_id);
        avro::encode(e, v.node_identifier);
    }
    static void decode(Decoder& d, kaa_notification::node_id& v) {
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
                    avro::decode(d, v.node_identifier);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.namespace_id);
            avro::decode(d, v.node_identifier);
        }
    }
};

template<> struct codec_traits<kaa_notification::_notification_avsc_Union__1__> {
    static void encode(Encoder& e, kaa_notification::_notification_avsc_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            avro::encode(e, v.get_double());
            break;
        case 2:
            avro::encode(e, v.get_float());
            break;
        case 3:
            avro::encode(e, v.get_int());
            break;
        case 4:
            avro::encode(e, v.get_long());
            break;
        case 5:
            avro::encode(e, v.get_bool());
            break;
        case 6:
            avro::encode(e, v.get_node_id());
            break;
        }
    }
    static void decode(Decoder& d, kaa_notification::_notification_avsc_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 7) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            {
                double vv;
                avro::decode(d, vv);
                v.set_double(vv);
            }
            break;
        case 2:
            {
                float vv;
                avro::decode(d, vv);
                v.set_float(vv);
            }
            break;
        case 3:
            {
                int32_t vv;
                avro::decode(d, vv);
                v.set_int(vv);
            }
            break;
        case 4:
            {
                int64_t vv;
                avro::decode(d, vv);
                v.set_long(vv);
            }
            break;
        case 5:
            {
                bool vv;
                avro::decode(d, vv);
                v.set_bool(vv);
            }
            break;
        case 6:
            {
                kaa_notification::node_id vv;
                avro::decode(d, vv);
                v.set_node_id(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa_notification::_notification_avsc_Union__2__> {
    static void encode(Encoder& e, kaa_notification::_notification_avsc_Union__2__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_array());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_notification::_notification_avsc_Union__2__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::vector<kaa_notification::_notification_avsc_Union__1__ > vv;
                avro::decode(d, vv);
                v.set_array(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_notification::opcua_method> {
    static void encode(Encoder& e, const kaa_notification::opcua_method& v) {
        avro::encode(e, v.server_id);
        avro::encode(e, v.node_namespace);
        avro::encode(e, v.node_id);
        avro::encode(e, v.method_namesapce);
        avro::encode(e, v.method_id);
        avro::encode(e, v.params);
    }
    static void decode(Decoder& d, kaa_notification::opcua_method& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.server_id);
                    break;
                case 1:
                    avro::decode(d, v.node_namespace);
                    break;
                case 2:
                    avro::decode(d, v.node_id);
                    break;
                case 3:
                    avro::decode(d, v.method_namesapce);
                    break;
                case 4:
                    avro::decode(d, v.method_id);
                    break;
                case 5:
                    avro::decode(d, v.params);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.server_id);
            avro::decode(d, v.node_namespace);
            avro::decode(d, v.node_id);
            avro::decode(d, v.method_namesapce);
            avro::decode(d, v.method_id);
            avro::decode(d, v.params);
        }
    }
};

template<> struct codec_traits<kaa_notification::_notification_avsc_Union__3__> {
    static void encode(Encoder& e, kaa_notification::_notification_avsc_Union__3__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_SoftwareUpdates());
            break;
        case 1:
            avro::encode(e, v.get_tagUpdates());
            break;
        case 2:
            avro::encode(e, v.get_sql_insert());
            break;
        case 3:
            avro::encode(e, v.get_can_message());
            break;
        case 4:
            avro::encode(e, v.get_opcua_method());
            break;
        }
    }
    static void decode(Decoder& d, kaa_notification::_notification_avsc_Union__3__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 5) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_notification::SoftwareUpdates vv;
                avro::decode(d, vv);
                v.set_SoftwareUpdates(vv);
            }
            break;
        case 1:
            {
                kaa_notification::tagUpdates vv;
                avro::decode(d, vv);
                v.set_tagUpdates(vv);
            }
            break;
        case 2:
            {
                kaa_notification::sql_insert vv;
                avro::decode(d, vv);
                v.set_sql_insert(vv);
            }
            break;
        case 3:
            {
                kaa_notification::can_message vv;
                avro::decode(d, vv);
                v.set_can_message(vv);
            }
            break;
        case 4:
            {
                kaa_notification::opcua_method vv;
                avro::decode(d, vv);
                v.set_opcua_method(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa_notification::Notification> {
    static void encode(Encoder& e, const kaa_notification::Notification& v) {
        avro::encode(e, v.notification);
    }
    static void decode(Decoder& d, kaa_notification::Notification& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.notification);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.notification);
        }
    }
};

}
#endif
