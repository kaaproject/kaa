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

#ifndef AVROBYTEARRAYCONVERTER_HPP_
#define AVROBYTEARRAYCONVERTER_HPP_

#include <string>
#include <memory>
#include <sstream>
#include <cstdint>

#include <avro/Compiler.hh>
#include <avro/Specific.hh>
#include <avro/Stream.hh>
#include <avro/Encoder.hh>
#include <avro/Decoder.hh>

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

/**
 * Used to convert predefined avro objects to/from bytes.
 * NOT Thread safe.
 * @param <T> predefined avro object.
 */
template<typename T>
class AvroByteArrayConverter
{
public:
    /**
     * Instantiates a new avro byte array converter based on <T>.
     */
    AvroByteArrayConverter();


    /*
     * Copy operator

     */

    /**
     * Creates avro object from byte array
     * Throws \ref KaaException when invalid data was passed (zero-sized or null buffer)
     * @param data the data
     * @param dataSize size of data
     * @return the result of conversion
     */
    T fromByteArray(const std::uint8_t* data, const std::uint32_t& dataSize);

    /**
     * Creates avro object from byte array
     * Throws \ref KaaException when invalid data was passed (zero-sized or null buffer)
     * @param data the data
     * @param dataSize size of data
     * @param the result of conversion
     */
    void fromByteArray(const std::uint8_t* data, const std::uint32_t& dataSize, T& datum);

    /**
     * Converts object to byte array
     * @param datum the encoding avro object
     * @return SharedDataBuffer result of a conversion
     */
    SharedDataBuffer toByteArray(const T& datum);

    /**
     * Converts object to byte array
     * @param datum the encoding avro object
     * @param dest the buffer that encoded data will be put in
     * @return serialized bytes
     */
    void toByteArray(const T& datum, std::vector<std::uint8_t>& dest);

    /**
     * Converts object to stream
     * @param datum the encoding avro object
     * @param stream the output stream that encoded data will be put in
     */
    void toByteArray(const T& datum, std::ostream& stream);

    /**
     * Used for debug purpose
     */
    void switchToJson(const avro::ValidSchema &schema) {
        encoder_ = avro::jsonEncoder(schema);
        decoder_ = avro::jsonDecoder(schema);
    }

    void switchToBinary() {
        encoder_ = avro::binaryEncoder();
        decoder_ = avro::binaryDecoder();
    }

private:
    avro::EncoderPtr   encoder_;
    avro::DecoderPtr   decoder_;
};

template<typename T>
AvroByteArrayConverter<T>::AvroByteArrayConverter()
{
    switchToBinary();
}

template<typename T>
T AvroByteArrayConverter<T>::fromByteArray(const std::uint8_t* data, const std::uint32_t& dataSize)
{
    if ((data && !dataSize) || (dataSize && !data)) {
        throw KaaException("invalid data to decode");
    }

    T datum;
    std::unique_ptr<avro::InputStream> in = avro::memoryInputStream(data, dataSize);

    decoder_->init(*in);
    avro::decode(*decoder_, datum);

    return datum;
}

template<typename T>
void AvroByteArrayConverter<T>::fromByteArray(const std::uint8_t* data, const std::uint32_t& dataSize, T& datum)
{
    if ((data && !dataSize) || (dataSize && !data)) {
        throw KaaException("invalid data to decode");
    }

    std::unique_ptr<avro::InputStream> in = avro::memoryInputStream(data, dataSize);

    decoder_->init(*in);
    avro::decode(*decoder_, datum);
}

template<typename T>
SharedDataBuffer AvroByteArrayConverter<T>::toByteArray(const T& datum)
{
    std::stringstream ostream;
    std::unique_ptr<avro::OutputStream> out = avro::ostreamOutputStream(ostream);

    encoder_->init(*out);
    avro::encode(*encoder_, datum);
    encoder_->flush();

    SharedDataBuffer buffer;

    std::streampos beg = ostream.tellg();
    ostream.seekg(0, std::ios_base::end);

    std::streampos end = ostream.tellg();
    ostream.seekg(0, std::ios_base::beg);

    buffer.second = end - beg;
    buffer.first.reset(new uint8_t[buffer.second]);
    std::copy(std::istreambuf_iterator<char>(ostream), std::istreambuf_iterator<char>(), buffer.first.get());

    return buffer;
}

template<typename T>
void AvroByteArrayConverter<T>::toByteArray(const T& datum, std::vector<std::uint8_t>& dest)
{
    std::stringstream ostream;
    std::unique_ptr<avro::OutputStream> out = avro::ostreamOutputStream(ostream);

    encoder_->init(*out);
    avro::encode(*encoder_, datum);
    encoder_->flush();

    std::streampos beg = ostream.tellg();
    ostream.seekg(0, std::ios_base::end);

    std::streampos end = ostream.tellg();
    ostream.seekg(0, std::ios_base::beg);

    dest.reserve(end - beg);
    dest.assign(std::istreambuf_iterator<char>(ostream), std::istreambuf_iterator<char>());
}

template<typename T>
void AvroByteArrayConverter<T>::toByteArray(const T& datum, std::ostream& stream)
{
    std::unique_ptr<avro::OutputStream> out = avro::ostreamOutputStream(stream);

    encoder_->init(*out);
    avro::encode(*encoder_, datum);
    encoder_->flush();
}

} /* namespace kaa */

#endif /* AVROBYTEARRAYCONVERTER_HPP_ */
