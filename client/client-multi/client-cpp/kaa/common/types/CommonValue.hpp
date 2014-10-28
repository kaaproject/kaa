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

#ifndef COMMONVALUE_HPP_
#define COMMONVALUE_HPP_

#include "kaa/common/types/ICommonValue.hpp"

#include <boost/cstdint.hpp>
#include <boost/ref.hpp>
#include <iomanip>
#include <sstream>

namespace kaa {

template <typename T, CommonValueType CVT>
class CommonValue : public ICommonValue {
public:
    typedef T                       value_type;
    typedef T *                     value_ptr;
    typedef T &                     value_ref;
    typedef const T &               value_cref;

    typedef boost::shared_ptr<T>    shared_ptr;

    CommonValue(value_cref value, size_t len = 0);
    CommonValue(const CommonValue<T, CVT> &r);
    ~CommonValue();

    const boost::any    getValue()  const   { return boost::cref(value_).get(); }
    avro::GenericDatum  toAvro()    const;
    std::string         toString()  const;
private:
    T value_;
    size_t valLength_;
};

template <>
CommonValue<boost::uint8_t *, CommonValueType::COMMON_BYTES>::~CommonValue()
{
    delete[] value_;
}

template <typename T, CommonValueType CVT>
CommonValue<T, CVT>::CommonValue(value_cref value, size_t len) : ICommonValue(CVT)
{
    value_ = value;
}

template <typename T, CommonValueType CVT>
CommonValue<T, CVT>::CommonValue(const CommonValue<T, CVT> &r) : ICommonValue(CVT)
{
    value_ = r.value_;
}

template <typename T, CommonValueType CVT>
CommonValue<T, CVT>::~CommonValue()
{

}

template <typename T, CommonValueType CVT>
std::string CommonValue<T, CVT>::toString() const
{
    std::stringstream ss;
    ss << value_;
    return ss.str();
}

template <>
std::string CommonValue<std::string, CommonValueType::COMMON_STRING>::toString() const
{
    std::stringstream ss;
    ss << "\"" << value_ << "\"";
    return ss.str();
}

template <>
std::string CommonValue<std::vector<boost::uint8_t>, CommonValueType::COMMON_BYTES>::toString() const
{
    std::stringstream ss;
    for (auto it = value_.begin(); it != value_.end();) {
        ss << std::setw(2) << std::setfill('0') << std::hex << (int)*it << std::dec;
        if (++it != value_.end()) {
            ss << "-";
        }
    }
    return ss.str();
}

template <typename T, CommonValueType CVT>
avro::GenericDatum CommonValue<T, CVT>::toAvro() const
{
    avro::GenericDatum datum(value_);
    return datum;
}

template <>
CommonValue<boost::uint8_t *, CommonValueType::COMMON_BYTES>::CommonValue(value_cref value, size_t len) : ICommonValue(CommonValueType::COMMON_BYTES)
{
    valLength_ = len;
    value_ = new boost::uint8_t[valLength_];
    std::copy(value, value + len, value_);
}

template <>
CommonValue<boost::uint8_t *, CommonValueType::COMMON_BYTES>::CommonValue(const CommonValue<boost::uint8_t *, CommonValueType::COMMON_BYTES> &r) : ICommonValue(CommonValueType::COMMON_BYTES)
{
    valLength_ = r.valLength_;
    value_ = new boost::uint8_t[valLength_];
    std::copy(r.value_, r.value_ + valLength_, value_);
}

template <>
std::string CommonValue<boost::uint8_t *, CommonValueType::COMMON_BYTES>::toString() const
{
    std::stringstream ss;
    for (size_t i = 0; i < valLength_; ) {
        ss << std::setw(2) << std::setfill('0') << std::hex << (int)value_[i] << std::dec;
        if (++i != valLength_) {
            ss << "-";
        }
    }
    return ss.str();
}

}  // namespace kaa

#endif /* COMMONVALUE_HPP_ */
