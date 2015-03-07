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


#ifndef KAA_EVENT_GEN_DEVICEEVENTCLASSFAMILYGEN_HPP_753220369__H_
#define KAA_EVENT_GEN_DEVICEEVENTCLASSFAMILYGEN_HPP_753220369__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace nsDeviceEventClassFamily {
struct DeviceInfoRequest {
    DeviceInfoRequest()
        { }
};

enum DeviceType {
    THERMOSTAT,
    TV,
    SOUND_SYSTEM,
    LAMP,
};

struct _DeviceEventClassFamily_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    DeviceType get_DeviceType() const;
    void set_DeviceType(const DeviceType& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _DeviceEventClassFamily_avsc_Union__0__();
};

struct _DeviceEventClassFamily_avsc_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _DeviceEventClassFamily_avsc_Union__1__();
};

struct _DeviceEventClassFamily_avsc_Union__2__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _DeviceEventClassFamily_avsc_Union__2__();
};

struct DeviceInfo {
    typedef _DeviceEventClassFamily_avsc_Union__0__ deviceType_t;
    typedef _DeviceEventClassFamily_avsc_Union__1__ model_t;
    typedef _DeviceEventClassFamily_avsc_Union__2__ manufacturer_t;
    deviceType_t deviceType;
    model_t model;
    manufacturer_t manufacturer;
    DeviceInfo() :
        deviceType(deviceType_t()),
        model(model_t()),
        manufacturer(manufacturer_t())
        { }
};

struct _DeviceEventClassFamily_avsc_Union__3__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    DeviceInfo get_DeviceInfo() const;
    void set_DeviceInfo(const DeviceInfo& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _DeviceEventClassFamily_avsc_Union__3__();
};

struct DeviceInfoResponse {
    typedef _DeviceEventClassFamily_avsc_Union__3__ deviceInfo_t;
    deviceInfo_t deviceInfo;
    DeviceInfoResponse() :
        deviceInfo(deviceInfo_t())
        { }
};

struct _DeviceEventClassFamily_avsc_Union__4__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    DeviceInfoRequest get_DeviceInfoRequest() const;
    void set_DeviceInfoRequest(const DeviceInfoRequest& v);
    DeviceType get_DeviceType() const;
    void set_DeviceType(const DeviceType& v);
    DeviceInfo get_DeviceInfo() const;
    void set_DeviceInfo(const DeviceInfo& v);
    DeviceInfoResponse get_DeviceInfoResponse() const;
    void set_DeviceInfoResponse(const DeviceInfoResponse& v);
    _DeviceEventClassFamily_avsc_Union__4__();
};

inline
DeviceType _DeviceEventClassFamily_avsc_Union__0__::get_DeviceType() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceType >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__0__::set_DeviceType(const DeviceType& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::string _DeviceEventClassFamily_avsc_Union__1__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__1__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::string _DeviceEventClassFamily_avsc_Union__2__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__2__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
DeviceInfo _DeviceEventClassFamily_avsc_Union__3__::get_DeviceInfo() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceInfo >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__3__::set_DeviceInfo(const DeviceInfo& v) {
    idx_ = 0;
    value_ = v;
}

inline
DeviceInfoRequest _DeviceEventClassFamily_avsc_Union__4__::get_DeviceInfoRequest() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceInfoRequest >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__4__::set_DeviceInfoRequest(const DeviceInfoRequest& v) {
    idx_ = 0;
    value_ = v;
}

inline
DeviceType _DeviceEventClassFamily_avsc_Union__4__::get_DeviceType() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceType >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__4__::set_DeviceType(const DeviceType& v) {
    idx_ = 1;
    value_ = v;
}

inline
DeviceInfo _DeviceEventClassFamily_avsc_Union__4__::get_DeviceInfo() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceInfo >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__4__::set_DeviceInfo(const DeviceInfo& v) {
    idx_ = 2;
    value_ = v;
}

inline
DeviceInfoResponse _DeviceEventClassFamily_avsc_Union__4__::get_DeviceInfoResponse() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<DeviceInfoResponse >(value_);
}

inline
void _DeviceEventClassFamily_avsc_Union__4__::set_DeviceInfoResponse(const DeviceInfoResponse& v) {
    idx_ = 3;
    value_ = v;
}

inline _DeviceEventClassFamily_avsc_Union__0__::_DeviceEventClassFamily_avsc_Union__0__() : idx_(0), value_(DeviceType()) { }
inline _DeviceEventClassFamily_avsc_Union__1__::_DeviceEventClassFamily_avsc_Union__1__() : idx_(0), value_(std::string()) { }
inline _DeviceEventClassFamily_avsc_Union__2__::_DeviceEventClassFamily_avsc_Union__2__() : idx_(0), value_(std::string()) { }
inline _DeviceEventClassFamily_avsc_Union__3__::_DeviceEventClassFamily_avsc_Union__3__() : idx_(0), value_(DeviceInfo()) { }
inline _DeviceEventClassFamily_avsc_Union__4__::_DeviceEventClassFamily_avsc_Union__4__() : idx_(0), value_(DeviceInfoRequest()) { }
}
namespace avro {
template<> struct codec_traits<nsDeviceEventClassFamily::DeviceInfoRequest> {
    static void encode(Encoder& e, const nsDeviceEventClassFamily::DeviceInfoRequest& v) {
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::DeviceInfoRequest& v) {
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::DeviceType> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::DeviceType v) {
		if (v < nsDeviceEventClassFamily::THERMOSTAT || v > nsDeviceEventClassFamily::LAMP)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for nsDeviceEventClassFamily::DeviceType and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::DeviceType& v) {
		size_t index = d.decodeEnum();
		if (index < nsDeviceEventClassFamily::THERMOSTAT || index > nsDeviceEventClassFamily::LAMP)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for nsDeviceEventClassFamily::DeviceType and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<nsDeviceEventClassFamily::DeviceType>(index);
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__0__> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_DeviceType());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                nsDeviceEventClassFamily::DeviceType vv;
                avro::decode(d, vv);
                v.set_DeviceType(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__1__> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__2__> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__2__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__2__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::DeviceInfo> {
    static void encode(Encoder& e, const nsDeviceEventClassFamily::DeviceInfo& v) {
        avro::encode(e, v.deviceType);
        avro::encode(e, v.model);
        avro::encode(e, v.manufacturer);
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::DeviceInfo& v) {
        avro::decode(d, v.deviceType);
        avro::decode(d, v.model);
        avro::decode(d, v.manufacturer);
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__3__> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__3__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_DeviceInfo());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__3__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                nsDeviceEventClassFamily::DeviceInfo vv;
                avro::decode(d, vv);
                v.set_DeviceInfo(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::DeviceInfoResponse> {
    static void encode(Encoder& e, const nsDeviceEventClassFamily::DeviceInfoResponse& v) {
        avro::encode(e, v.deviceInfo);
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::DeviceInfoResponse& v) {
        avro::decode(d, v.deviceInfo);
    }
};

template<> struct codec_traits<nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__4__> {
    static void encode(Encoder& e, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__4__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_DeviceInfoRequest());
            break;
        case 1:
            avro::encode(e, v.get_DeviceType());
            break;
        case 2:
            avro::encode(e, v.get_DeviceInfo());
            break;
        case 3:
            avro::encode(e, v.get_DeviceInfoResponse());
            break;
        }
    }
    static void decode(Decoder& d, nsDeviceEventClassFamily::_DeviceEventClassFamily_avsc_Union__4__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 4) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                nsDeviceEventClassFamily::DeviceInfoRequest vv;
                avro::decode(d, vv);
                v.set_DeviceInfoRequest(vv);
            }
            break;
        case 1:
            {
                nsDeviceEventClassFamily::DeviceType vv;
                avro::decode(d, vv);
                v.set_DeviceType(vv);
            }
            break;
        case 2:
            {
                nsDeviceEventClassFamily::DeviceInfo vv;
                avro::decode(d, vv);
                v.set_DeviceInfo(vv);
            }
            break;
        case 3:
            {
                nsDeviceEventClassFamily::DeviceInfoResponse vv;
                avro::decode(d, vv);
                v.set_DeviceInfoResponse(vv);
            }
            break;
        }
    }
};

}
#endif
