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


#ifndef AVRO_HPP_4191886128__H_
#define AVRO_HPP_4191886128__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

enum unchangedT {
    unchanged,
};

struct _configuration_schema_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    _configuration_schema_Union__0__();
};

struct _configuration_schema_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    int32_t get_int() const;
    void set_int(const int32_t& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    _configuration_schema_Union__1__();
};

struct testRecordT {
    typedef _configuration_schema_Union__1__ testField3_t;
    testField3_t testField3;
    boost::array<uint8_t, 16> __uuid;
    testRecordT() :
        testField3(testField3_t()),
        __uuid(boost::array<uint8_t, 16>())
        { }
};

struct _configuration_schema_Union__2__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    testRecordT get_testRecordT() const;
    void set_testRecordT(const testRecordT& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    bool is_null() const {
        return (idx_ == 2);
    }
    void set_null() {
        idx_ = 2;
        value_ = boost::any();
    }
    _configuration_schema_Union__2__();
};

struct _configuration_schema_Union__3__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    bool get_bool() const;
    void set_bool(const bool& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    _configuration_schema_Union__3__();
};

enum strategyT {
    DEFAULT,
    CONCRETE1,
    CONCRETE2,
};

struct _configuration_schema_Union__4__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    strategyT get_strategyT() const;
    void set_strategyT(const strategyT& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    bool is_null() const {
        return (idx_ == 2);
    }
    void set_null() {
        idx_ = 2;
        value_ = boost::any();
    }
    _configuration_schema_Union__4__();
};

struct recordArrayItemT {
    typedef _configuration_schema_Union__3__ enabled_t;
    typedef _configuration_schema_Union__4__ strategy_t;
    boost::array<uint8_t, 16> __uuid;
    enabled_t enabled;
    strategy_t strategy;
    recordArrayItemT() :
        __uuid(boost::array<uint8_t, 16>()),
        enabled(enabled_t()),
        strategy(strategy_t())
        { }
};

struct _configuration_schema_Union__5__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    recordArrayItemT get_recordArrayItemT() const;
    void set_recordArrayItemT(const recordArrayItemT& v);
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    _configuration_schema_Union__5__();
};

enum resetT {
    reset,
};

struct _configuration_schema_Union__6__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::vector<_configuration_schema_Union__5__ > get_array() const;
    void set_array(const std::vector<_configuration_schema_Union__5__ >& v);
    resetT get_resetT() const;
    void set_resetT(const resetT& v);
    _configuration_schema_Union__6__();
};

struct testArrayRecord1T {
    typedef _configuration_schema_Union__6__ testArray1_t;
    boost::array<uint8_t, 16> __uuid;
    testArray1_t testArray1;
    testArrayRecord1T() :
        __uuid(boost::array<uint8_t, 16>()),
        testArray1(testArray1_t())
        { }
};

enum userRoleT {
    ADMIN,
    MODERATOR,
    USER,
    GUEST,
};

struct _configuration_schema_Union__7__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    userRoleT get_userRoleT() const;
    void set_userRoleT(const userRoleT& v);
    unchangedT get_unchangedT() const;
    void set_unchangedT(const unchangedT& v);
    _configuration_schema_Union__7__();
};

struct recordArrayItem2T {
    typedef _configuration_schema_Union__7__ role_t;
    boost::array<uint8_t, 16> __uuid;
    std::string user;
    role_t role;
    recordArrayItem2T() :
        __uuid(boost::array<uint8_t, 16>()),
        user(std::string()),
        role(role_t())
        { }
};

struct _configuration_schema_Union__8__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    recordArrayItem2T get_recordArrayItem2T() const;
    void set_recordArrayItem2T(const recordArrayItem2T& v);
    _configuration_schema_Union__8__();
};

struct _configuration_schema_Union__9__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::vector<_configuration_schema_Union__8__ > get_array() const;
    void set_array(const std::vector<_configuration_schema_Union__8__ >& v);
    resetT get_resetT() const;
    void set_resetT(const resetT& v);
    _configuration_schema_Union__9__();
};

struct testArrayRecord2T {
    typedef _configuration_schema_Union__9__ testArray2_t;
    boost::array<uint8_t, 16> __uuid;
    testArray2_t testArray2;
    testArrayRecord2T() :
        __uuid(boost::array<uint8_t, 16>()),
        testArray2(testArray2_t())
        { }
};

struct testT {
    typedef _configuration_schema_Union__0__ testField1_t;
    typedef _configuration_schema_Union__2__ testField2_t;
    testField1_t testField1;
    testField2_t testField2;
    testArrayRecord1T testArrayRecord1;
    testArrayRecord2T testArrayRecord2;
    boost::array<uint8_t, 16> __uuid;
    testT() :
        testField1(testField1_t()),
        testField2(testField2_t()),
        testArrayRecord1(testArrayRecord1T()),
        testArrayRecord2(testArrayRecord2T()),
        __uuid(boost::array<uint8_t, 16>())
        { }
};

struct _configuration_schema_Union__10__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    testT get_testT() const;
    void set_testT(const testT& v);
    testRecordT get_testRecordT() const;
    void set_testRecordT(const testRecordT& v);
    recordArrayItemT get_recordArrayItemT() const;
    void set_recordArrayItemT(const recordArrayItemT& v);
    recordArrayItem2T get_recordArrayItem2T() const;
    void set_recordArrayItem2T(const recordArrayItem2T& v);
    testArrayRecord1T get_testArrayRecord1T() const;
    void set_testArrayRecord1T(const testArrayRecord1T& v);
    testArrayRecord2T get_testArrayRecord2T() const;
    void set_testArrayRecord2T(const testArrayRecord2T& v);
    _configuration_schema_Union__10__();
};

struct deltaT {
    typedef _configuration_schema_Union__10__ delta_t;
    delta_t delta;
    deltaT() :
        delta(delta_t())
        { }
};

inline
std::string _configuration_schema_Union__0__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _configuration_schema_Union__0__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__0__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__0__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
int32_t _configuration_schema_Union__1__::get_int() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int32_t >(value_);
}

inline
void _configuration_schema_Union__1__::set_int(const int32_t& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__1__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__1__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
testRecordT _configuration_schema_Union__2__::get_testRecordT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<testRecordT >(value_);
}

inline
void _configuration_schema_Union__2__::set_testRecordT(const testRecordT& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__2__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__2__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
bool _configuration_schema_Union__3__::get_bool() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<bool >(value_);
}

inline
void _configuration_schema_Union__3__::set_bool(const bool& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__3__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__3__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
strategyT _configuration_schema_Union__4__::get_strategyT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<strategyT >(value_);
}

inline
void _configuration_schema_Union__4__::set_strategyT(const strategyT& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__4__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__4__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
recordArrayItemT _configuration_schema_Union__5__::get_recordArrayItemT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<recordArrayItemT >(value_);
}

inline
void _configuration_schema_Union__5__::set_recordArrayItemT(const recordArrayItemT& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_schema_Union__5__::get_uuidT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_schema_Union__5__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 1;
    value_ = v;
}

inline
std::vector<_configuration_schema_Union__5__ > _configuration_schema_Union__6__::get_array() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::vector<_configuration_schema_Union__5__ > >(value_);
}

inline
void _configuration_schema_Union__6__::set_array(const std::vector<_configuration_schema_Union__5__ >& v) {
    idx_ = 0;
    value_ = v;
}

inline
resetT _configuration_schema_Union__6__::get_resetT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<resetT >(value_);
}

inline
void _configuration_schema_Union__6__::set_resetT(const resetT& v) {
    idx_ = 1;
    value_ = v;
}

inline
userRoleT _configuration_schema_Union__7__::get_userRoleT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<userRoleT >(value_);
}

inline
void _configuration_schema_Union__7__::set_userRoleT(const userRoleT& v) {
    idx_ = 0;
    value_ = v;
}

inline
unchangedT _configuration_schema_Union__7__::get_unchangedT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<unchangedT >(value_);
}

inline
void _configuration_schema_Union__7__::set_unchangedT(const unchangedT& v) {
    idx_ = 1;
    value_ = v;
}

inline
std::string _configuration_schema_Union__8__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _configuration_schema_Union__8__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
recordArrayItem2T _configuration_schema_Union__8__::get_recordArrayItem2T() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<recordArrayItem2T >(value_);
}

inline
void _configuration_schema_Union__8__::set_recordArrayItem2T(const recordArrayItem2T& v) {
    idx_ = 1;
    value_ = v;
}

inline
std::vector<_configuration_schema_Union__8__ > _configuration_schema_Union__9__::get_array() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::vector<_configuration_schema_Union__8__ > >(value_);
}

inline
void _configuration_schema_Union__9__::set_array(const std::vector<_configuration_schema_Union__8__ >& v) {
    idx_ = 0;
    value_ = v;
}

inline
resetT _configuration_schema_Union__9__::get_resetT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<resetT >(value_);
}

inline
void _configuration_schema_Union__9__::set_resetT(const resetT& v) {
    idx_ = 1;
    value_ = v;
}

inline
testT _configuration_schema_Union__10__::get_testT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<testT >(value_);
}

inline
void _configuration_schema_Union__10__::set_testT(const testT& v) {
    idx_ = 0;
    value_ = v;
}

inline
testRecordT _configuration_schema_Union__10__::get_testRecordT() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<testRecordT >(value_);
}

inline
void _configuration_schema_Union__10__::set_testRecordT(const testRecordT& v) {
    idx_ = 1;
    value_ = v;
}

inline
recordArrayItemT _configuration_schema_Union__10__::get_recordArrayItemT() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<recordArrayItemT >(value_);
}

inline
void _configuration_schema_Union__10__::set_recordArrayItemT(const recordArrayItemT& v) {
    idx_ = 2;
    value_ = v;
}

inline
recordArrayItem2T _configuration_schema_Union__10__::get_recordArrayItem2T() const {
    if (idx_ != 3) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<recordArrayItem2T >(value_);
}

inline
void _configuration_schema_Union__10__::set_recordArrayItem2T(const recordArrayItem2T& v) {
    idx_ = 3;
    value_ = v;
}

inline
testArrayRecord1T _configuration_schema_Union__10__::get_testArrayRecord1T() const {
    if (idx_ != 4) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<testArrayRecord1T >(value_);
}

inline
void _configuration_schema_Union__10__::set_testArrayRecord1T(const testArrayRecord1T& v) {
    idx_ = 4;
    value_ = v;
}

inline
testArrayRecord2T _configuration_schema_Union__10__::get_testArrayRecord2T() const {
    if (idx_ != 5) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<testArrayRecord2T >(value_);
}

inline
void _configuration_schema_Union__10__::set_testArrayRecord2T(const testArrayRecord2T& v) {
    idx_ = 5;
    value_ = v;
}

inline _configuration_schema_Union__0__::_configuration_schema_Union__0__() : idx_(0), value_(std::string()) { }
inline _configuration_schema_Union__1__::_configuration_schema_Union__1__() : idx_(0), value_(int32_t()) { }
inline _configuration_schema_Union__2__::_configuration_schema_Union__2__() : idx_(0), value_(testRecordT()) { }
inline _configuration_schema_Union__3__::_configuration_schema_Union__3__() : idx_(0), value_(bool()) { }
inline _configuration_schema_Union__4__::_configuration_schema_Union__4__() : idx_(0), value_(strategyT()) { }
inline _configuration_schema_Union__5__::_configuration_schema_Union__5__() : idx_(0), value_(recordArrayItemT()) { }
inline _configuration_schema_Union__6__::_configuration_schema_Union__6__() : idx_(0), value_(std::vector<_configuration_schema_Union__5__ >()) { }
inline _configuration_schema_Union__7__::_configuration_schema_Union__7__() : idx_(0), value_(userRoleT()) { }
inline _configuration_schema_Union__8__::_configuration_schema_Union__8__() : idx_(0), value_(std::string()) { }
inline _configuration_schema_Union__9__::_configuration_schema_Union__9__() : idx_(0), value_(std::vector<_configuration_schema_Union__8__ >()) { }
inline _configuration_schema_Union__10__::_configuration_schema_Union__10__() : idx_(0), value_(testT()) { }
namespace avro {
template<> struct codec_traits<unchangedT> {
    static void encode(Encoder& e, unchangedT v) {
		if (v < unchanged || v > unchanged)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for unchangedT and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, unchangedT& v) {
		size_t index = d.decodeEnum();
		if (index < unchanged || index > unchanged)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for unchangedT and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<unchangedT>(index);
    }
};

template<> struct codec_traits<_configuration_schema_Union__0__> {
    static void encode(Encoder& e, _configuration_schema_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__0__& v) {
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
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<_configuration_schema_Union__1__> {
    static void encode(Encoder& e, _configuration_schema_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_int());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                int32_t vv;
                avro::decode(d, vv);
                v.set_int(vv);
            }
            break;
        case 1:
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<testRecordT> {
    static void encode(Encoder& e, const testRecordT& v) {
        avro::encode(e, v.testField3);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, testRecordT& v) {
        avro::decode(d, v.testField3);
        avro::decode(d, v.__uuid);
    }
};

template<> struct codec_traits<_configuration_schema_Union__2__> {
    static void encode(Encoder& e, _configuration_schema_Union__2__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_testRecordT());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        case 2:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__2__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 3) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                testRecordT vv;
                avro::decode(d, vv);
                v.set_testRecordT(vv);
            }
            break;
        case 1:
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        case 2:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<_configuration_schema_Union__3__> {
    static void encode(Encoder& e, _configuration_schema_Union__3__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_bool());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__3__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                bool vv;
                avro::decode(d, vv);
                v.set_bool(vv);
            }
            break;
        case 1:
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<strategyT> {
    static void encode(Encoder& e, strategyT v) {
		if (v < DEFAULT || v > CONCRETE2)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for strategyT and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, strategyT& v) {
		size_t index = d.decodeEnum();
		if (index < DEFAULT || index > CONCRETE2)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for strategyT and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<strategyT>(index);
    }
};

template<> struct codec_traits<_configuration_schema_Union__4__> {
    static void encode(Encoder& e, _configuration_schema_Union__4__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_strategyT());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        case 2:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__4__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 3) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                strategyT vv;
                avro::decode(d, vv);
                v.set_strategyT(vv);
            }
            break;
        case 1:
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        case 2:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<recordArrayItemT> {
    static void encode(Encoder& e, const recordArrayItemT& v) {
        avro::encode(e, v.__uuid);
        avro::encode(e, v.enabled);
        avro::encode(e, v.strategy);
    }
    static void decode(Decoder& d, recordArrayItemT& v) {
        avro::decode(d, v.__uuid);
        avro::decode(d, v.enabled);
        avro::decode(d, v.strategy);
    }
};

template<> struct codec_traits<_configuration_schema_Union__5__> {
    static void encode(Encoder& e, _configuration_schema_Union__5__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_recordArrayItemT());
            break;
        case 1:
            avro::encode(e, v.get_uuidT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__5__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                recordArrayItemT vv;
                avro::decode(d, vv);
                v.set_recordArrayItemT(vv);
            }
            break;
        case 1:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<resetT> {
    static void encode(Encoder& e, resetT v) {
		if (v < reset || v > reset)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for resetT and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, resetT& v) {
		size_t index = d.decodeEnum();
		if (index < reset || index > reset)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for resetT and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<resetT>(index);
    }
};

template<> struct codec_traits<_configuration_schema_Union__6__> {
    static void encode(Encoder& e, _configuration_schema_Union__6__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_array());
            break;
        case 1:
            avro::encode(e, v.get_resetT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__6__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::vector<_configuration_schema_Union__5__ > vv;
                avro::decode(d, vv);
                v.set_array(vv);
            }
            break;
        case 1:
            {
                resetT vv;
                avro::decode(d, vv);
                v.set_resetT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<testArrayRecord1T> {
    static void encode(Encoder& e, const testArrayRecord1T& v) {
        avro::encode(e, v.__uuid);
        avro::encode(e, v.testArray1);
    }
    static void decode(Decoder& d, testArrayRecord1T& v) {
        avro::decode(d, v.__uuid);
        avro::decode(d, v.testArray1);
    }
};

template<> struct codec_traits<userRoleT> {
    static void encode(Encoder& e, userRoleT v) {
		if (v < ADMIN || v > GUEST)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for userRoleT and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, userRoleT& v) {
		size_t index = d.decodeEnum();
		if (index < ADMIN || index > GUEST)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for userRoleT and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<userRoleT>(index);
    }
};

template<> struct codec_traits<_configuration_schema_Union__7__> {
    static void encode(Encoder& e, _configuration_schema_Union__7__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_userRoleT());
            break;
        case 1:
            avro::encode(e, v.get_unchangedT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__7__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                userRoleT vv;
                avro::decode(d, vv);
                v.set_userRoleT(vv);
            }
            break;
        case 1:
            {
                unchangedT vv;
                avro::decode(d, vv);
                v.set_unchangedT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<recordArrayItem2T> {
    static void encode(Encoder& e, const recordArrayItem2T& v) {
        avro::encode(e, v.__uuid);
        avro::encode(e, v.user);
        avro::encode(e, v.role);
    }
    static void decode(Decoder& d, recordArrayItem2T& v) {
        avro::decode(d, v.__uuid);
        avro::decode(d, v.user);
        avro::decode(d, v.role);
    }
};

template<> struct codec_traits<_configuration_schema_Union__8__> {
    static void encode(Encoder& e, _configuration_schema_Union__8__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            avro::encode(e, v.get_recordArrayItem2T());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__8__& v) {
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
            {
                recordArrayItem2T vv;
                avro::decode(d, vv);
                v.set_recordArrayItem2T(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<_configuration_schema_Union__9__> {
    static void encode(Encoder& e, _configuration_schema_Union__9__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_array());
            break;
        case 1:
            avro::encode(e, v.get_resetT());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__9__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::vector<_configuration_schema_Union__8__ > vv;
                avro::decode(d, vv);
                v.set_array(vv);
            }
            break;
        case 1:
            {
                resetT vv;
                avro::decode(d, vv);
                v.set_resetT(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<testArrayRecord2T> {
    static void encode(Encoder& e, const testArrayRecord2T& v) {
        avro::encode(e, v.__uuid);
        avro::encode(e, v.testArray2);
    }
    static void decode(Decoder& d, testArrayRecord2T& v) {
        avro::decode(d, v.__uuid);
        avro::decode(d, v.testArray2);
    }
};

template<> struct codec_traits<testT> {
    static void encode(Encoder& e, const testT& v) {
        avro::encode(e, v.testField1);
        avro::encode(e, v.testField2);
        avro::encode(e, v.testArrayRecord1);
        avro::encode(e, v.testArrayRecord2);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, testT& v) {
        avro::decode(d, v.testField1);
        avro::decode(d, v.testField2);
        avro::decode(d, v.testArrayRecord1);
        avro::decode(d, v.testArrayRecord2);
        avro::decode(d, v.__uuid);
    }
};

template<> struct codec_traits<_configuration_schema_Union__10__> {
    static void encode(Encoder& e, _configuration_schema_Union__10__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_testT());
            break;
        case 1:
            avro::encode(e, v.get_testRecordT());
            break;
        case 2:
            avro::encode(e, v.get_recordArrayItemT());
            break;
        case 3:
            avro::encode(e, v.get_recordArrayItem2T());
            break;
        case 4:
            avro::encode(e, v.get_testArrayRecord1T());
            break;
        case 5:
            avro::encode(e, v.get_testArrayRecord2T());
            break;
        }
    }
    static void decode(Decoder& d, _configuration_schema_Union__10__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 6) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                testT vv;
                avro::decode(d, vv);
                v.set_testT(vv);
            }
            break;
        case 1:
            {
                testRecordT vv;
                avro::decode(d, vv);
                v.set_testRecordT(vv);
            }
            break;
        case 2:
            {
                recordArrayItemT vv;
                avro::decode(d, vv);
                v.set_recordArrayItemT(vv);
            }
            break;
        case 3:
            {
                recordArrayItem2T vv;
                avro::decode(d, vv);
                v.set_recordArrayItem2T(vv);
            }
            break;
        case 4:
            {
                testArrayRecord1T vv;
                avro::decode(d, vv);
                v.set_testArrayRecord1T(vv);
            }
            break;
        case 5:
            {
                testArrayRecord2T vv;
                avro::decode(d, vv);
                v.set_testArrayRecord2T(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<deltaT> {
    static void encode(Encoder& e, const deltaT& v) {
        avro::encode(e, v.delta);
    }
    static void decode(Decoder& d, deltaT& v) {
        avro::decode(d, v.delta);
    }
};

}
#endif
