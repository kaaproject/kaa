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


#ifndef _HOME_DYOSICK_PROJECTS_KAA_CLIENT_CLIENT_MULTI_CLIENT_CPP_TEST_HEADERS_GEN_ENDPOINTGEN_HPP_3830932373__H_
#define _HOME_DYOSICK_PROJECTS_KAA_CLIENT_CLIENT_MULTI_CLIENT_CPP_TEST_HEADERS_GEN_ENDPOINTGEN_HPP_3830932373__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa {
struct BasicUserNotification {
    std::string notificationBody;
    int32_t userNotificationParam;
    BasicUserNotification() :
        notificationBody(std::string()),
        userNotificationParam(int32_t())
        { }
};

struct BasicSystemNotification {
    std::string notificationBody;
    int32_t systemNotificationParam1;
    int32_t systemNotificationParam2;
    BasicSystemNotification() :
        notificationBody(std::string()),
        systemNotificationParam1(int32_t()),
        systemNotificationParam2(int32_t())
        { }
};

struct BasicEndpointProfile {
    std::string profileBody;
    BasicEndpointProfile() :
        profileBody(std::string())
        { }
};

struct ExtendedEndpointProfileChild {
    int32_t otherSimpleField;
    std::string stringField;
    std::map<std::string, int64_t > otherMapSimpleField;
    ExtendedEndpointProfileChild() :
        otherSimpleField(int32_t()),
        stringField(std::string()),
        otherMapSimpleField(std::map<std::string, int64_t >())
        { }
};

struct _misc_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    ExtendedEndpointProfileChild get_ExtendedEndpointProfileChild() const;
    void set_ExtendedEndpointProfileChild(const ExtendedEndpointProfileChild& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _misc_avsc_Union__0__();
};

struct ExtendedEndpointProfile {
    typedef _misc_avsc_Union__0__ nullableRecordField_t;
    std::string simpleField;
    ExtendedEndpointProfileChild recordField;
    std::vector<std::string > arraySimpleField;
    std::vector<ExtendedEndpointProfileChild > arrayRecordField;
    std::map<std::string, int64_t > mapSimpleField;
    std::map<std::string, ExtendedEndpointProfileChild > mapRecordField;
    nullableRecordField_t nullableRecordField;
    ExtendedEndpointProfile() :
        simpleField(std::string()),
        recordField(ExtendedEndpointProfileChild()),
        arraySimpleField(std::vector<std::string >()),
        arrayRecordField(std::vector<ExtendedEndpointProfileChild >()),
        mapSimpleField(std::map<std::string, int64_t >()),
        mapRecordField(std::map<std::string, ExtendedEndpointProfileChild >()),
        nullableRecordField(nullableRecordField_t())
        { }
};

struct _misc_avsc_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    BasicUserNotification get_BasicUserNotification() const;
    void set_BasicUserNotification(const BasicUserNotification& v);
    BasicSystemNotification get_BasicSystemNotification() const;
    void set_BasicSystemNotification(const BasicSystemNotification& v);
    BasicEndpointProfile get_BasicEndpointProfile() const;
    void set_BasicEndpointProfile(const BasicEndpointProfile& v);
    ExtendedEndpointProfileChild get_ExtendedEndpointProfileChild() const;
    void set_ExtendedEndpointProfileChild(const ExtendedEndpointProfileChild& v);
    ExtendedEndpointProfile get_ExtendedEndpointProfile() const;
    void set_ExtendedEndpointProfile(const ExtendedEndpointProfile& v);
    _misc_avsc_Union__1__();
};

inline
ExtendedEndpointProfileChild _misc_avsc_Union__0__::get_ExtendedEndpointProfileChild() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<ExtendedEndpointProfileChild >(value_);
}

inline
void _misc_avsc_Union__0__::set_ExtendedEndpointProfileChild(const ExtendedEndpointProfileChild& v) {
    idx_ = 0;
    value_ = v;
}

inline
BasicUserNotification _misc_avsc_Union__1__::get_BasicUserNotification() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<BasicUserNotification >(value_);
}

inline
void _misc_avsc_Union__1__::set_BasicUserNotification(const BasicUserNotification& v) {
    idx_ = 0;
    value_ = v;
}

inline
BasicSystemNotification _misc_avsc_Union__1__::get_BasicSystemNotification() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<BasicSystemNotification >(value_);
}

inline
void _misc_avsc_Union__1__::set_BasicSystemNotification(const BasicSystemNotification& v) {
    idx_ = 1;
    value_ = v;
}

inline
BasicEndpointProfile _misc_avsc_Union__1__::get_BasicEndpointProfile() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<BasicEndpointProfile >(value_);
}

inline
void _misc_avsc_Union__1__::set_BasicEndpointProfile(const BasicEndpointProfile& v) {
    idx_ = 2;
    value_ = v;
}

inline
ExtendedEndpointProfileChild _misc_avsc_Union__1__::get_ExtendedEndpointProfileChild() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<ExtendedEndpointProfileChild >(value_);
}

inline
void _misc_avsc_Union__1__::set_ExtendedEndpointProfileChild(const ExtendedEndpointProfileChild& v) {
    idx_ = 3;
    value_ = v;
}

inline
ExtendedEndpointProfile _misc_avsc_Union__1__::get_ExtendedEndpointProfile() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<ExtendedEndpointProfile >(value_);
}

inline
void _misc_avsc_Union__1__::set_ExtendedEndpointProfile(const ExtendedEndpointProfile& v) {
    idx_ = 4;
    value_ = v;
}

inline _misc_avsc_Union__0__::_misc_avsc_Union__0__() : idx_(0), value_(ExtendedEndpointProfileChild()) { }
inline _misc_avsc_Union__1__::_misc_avsc_Union__1__() : idx_(0), value_(BasicUserNotification()) { }
}
namespace avro {
template<> struct codec_traits<kaa::BasicUserNotification> {
    static void encode(Encoder& e, const kaa::BasicUserNotification& v) {
        avro::encode(e, v.notificationBody);
        avro::encode(e, v.userNotificationParam);
    }
    static void decode(Decoder& d, kaa::BasicUserNotification& v) {
        avro::decode(d, v.notificationBody);
        avro::decode(d, v.userNotificationParam);
    }
};

template<> struct codec_traits<kaa::BasicSystemNotification> {
    static void encode(Encoder& e, const kaa::BasicSystemNotification& v) {
        avro::encode(e, v.notificationBody);
        avro::encode(e, v.systemNotificationParam1);
        avro::encode(e, v.systemNotificationParam2);
    }
    static void decode(Decoder& d, kaa::BasicSystemNotification& v) {
        avro::decode(d, v.notificationBody);
        avro::decode(d, v.systemNotificationParam1);
        avro::decode(d, v.systemNotificationParam2);
    }
};

template<> struct codec_traits<kaa::BasicEndpointProfile> {
    static void encode(Encoder& e, const kaa::BasicEndpointProfile& v) {
        avro::encode(e, v.profileBody);
    }
    static void decode(Decoder& d, kaa::BasicEndpointProfile& v) {
        avro::decode(d, v.profileBody);
    }
};

template<> struct codec_traits<kaa::ExtendedEndpointProfileChild> {
    static void encode(Encoder& e, const kaa::ExtendedEndpointProfileChild& v) {
        avro::encode(e, v.otherSimpleField);
        avro::encode(e, v.stringField);
        avro::encode(e, v.otherMapSimpleField);
    }
    static void decode(Decoder& d, kaa::ExtendedEndpointProfileChild& v) {
        avro::decode(d, v.otherSimpleField);
        avro::decode(d, v.stringField);
        avro::decode(d, v.otherMapSimpleField);
    }
};

template<> struct codec_traits<kaa::_misc_avsc_Union__0__> {
    static void encode(Encoder& e, kaa::_misc_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_ExtendedEndpointProfileChild());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa::_misc_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa::ExtendedEndpointProfileChild vv;
                avro::decode(d, vv);
                v.set_ExtendedEndpointProfileChild(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa::ExtendedEndpointProfile> {
    static void encode(Encoder& e, const kaa::ExtendedEndpointProfile& v) {
        avro::encode(e, v.simpleField);
        avro::encode(e, v.recordField);
        avro::encode(e, v.arraySimpleField);
        avro::encode(e, v.arrayRecordField);
        avro::encode(e, v.mapSimpleField);
        avro::encode(e, v.mapRecordField);
        avro::encode(e, v.nullableRecordField);
    }
    static void decode(Decoder& d, kaa::ExtendedEndpointProfile& v) {
        avro::decode(d, v.simpleField);
        avro::decode(d, v.recordField);
        avro::decode(d, v.arraySimpleField);
        avro::decode(d, v.arrayRecordField);
        avro::decode(d, v.mapSimpleField);
        avro::decode(d, v.mapRecordField);
        avro::decode(d, v.nullableRecordField);
    }
};

template<> struct codec_traits<kaa::_misc_avsc_Union__1__> {
    static void encode(Encoder& e, kaa::_misc_avsc_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_BasicUserNotification());
            break;
        case 1:
            avro::encode(e, v.get_BasicSystemNotification());
            break;
        case 2:
            avro::encode(e, v.get_BasicEndpointProfile());
            break;
        case 3:
            avro::encode(e, v.get_ExtendedEndpointProfileChild());
            break;
        case 4:
            avro::encode(e, v.get_ExtendedEndpointProfile());
            break;
        }
    }
    static void decode(Decoder& d, kaa::_misc_avsc_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 5) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa::BasicUserNotification vv;
                avro::decode(d, vv);
                v.set_BasicUserNotification(vv);
            }
            break;
        case 1:
            {
                kaa::BasicSystemNotification vv;
                avro::decode(d, vv);
                v.set_BasicSystemNotification(vv);
            }
            break;
        case 2:
            {
                kaa::BasicEndpointProfile vv;
                avro::decode(d, vv);
                v.set_BasicEndpointProfile(vv);
            }
            break;
        case 3:
            {
                kaa::ExtendedEndpointProfileChild vv;
                avro::decode(d, vv);
                v.set_ExtendedEndpointProfileChild(vv);
            }
            break;
        case 4:
            {
                kaa::ExtendedEndpointProfile vv;
                avro::decode(d, vv);
                v.set_ExtendedEndpointProfile(vv);
            }
            break;
        }
    }
};

}
#endif
