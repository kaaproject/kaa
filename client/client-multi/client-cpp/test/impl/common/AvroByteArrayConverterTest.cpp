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

#include <boost/test/unit_test.hpp>

#include <memory>
#include <fstream>
#include <sstream>
#include <cstring>
#include <algorithm>

#include <cstdint>

#include <avro/ValidSchema.hh>
#include <avro/Generic.hh>
#include <avro/Compiler.hh>

#include "kaa/common/AvroByteArrayConverter.hpp"

#include "headers/gen/EndpointGen.hpp"

namespace kaa {

#ifdef RESOURCE_DIR
const char * const profile_schema_path = RESOURCE_DIR"/profile.json";
#else
#error "No path to resources defined!"
#endif

template<typename Data>
static void binaryEncodeDataTo(std::ostream& stream, const Data& data)
{
    std::unique_ptr<avro::OutputStream> out = avro::ostreamOutputStream(stream);
    avro::EncoderPtr e = avro::binaryEncoder();
    e->init(*out);
    avro::encode(*e, data);
    e->flush();
}

template<typename Data>
static Data decodeBinaryData(const std::uint8_t* data, const std::uint32_t& dataSize)
{
    std::unique_ptr<avro::InputStream> in = avro::memoryInputStream(data, dataSize);
    avro::DecoderPtr d = avro::binaryDecoder();
    d->init(*in);

    Data decodedData;
    avro::decode(*d, decodedData);

    return decodedData;
}

BOOST_AUTO_TEST_SUITE(AvroByteArrayConverterSuite)

BOOST_AUTO_TEST_CASE(InvalidDecodingData)
{
    avro::GenericDatum datum;
    AvroByteArrayConverter<avro::GenericDatum> converter;

    BOOST_CHECK_NO_THROW(converter.fromByteArray(nullptr, 0));
    BOOST_CHECK_THROW(converter.fromByteArray(nullptr, 5), KaaException);
    const uint8_t *data = reinterpret_cast<const uint8_t *>("1234");
    BOOST_CHECK_THROW(converter.fromByteArray(data, 0, datum), KaaException);
}

BOOST_AUTO_TEST_CASE(AvroBinaryEncodingToBuffer)
{
    BasicEndpointProfile encodingProfile;
    encodingProfile.profileBody = "Test body";

    AvroByteArrayConverter<BasicEndpointProfile> converter;
    SharedDataBuffer encodedData1 = converter.toByteArray(encodingProfile);

    BOOST_CHECK_MESSAGE(encodedData1.first && (encodedData1.second > 0)
                                , "Encoded data is empty or/and its size 0");

    std::ostringstream stream;
    binaryEncodeDataTo(stream, encodingProfile);
    const std::string& encodedData2 = stream.str();

    int res = ::memcmp(encodedData1.first.get(), encodedData2.data(),
            std::min(encodedData1.second, static_cast<std::uint32_t>(encodedData2.length())));

    BOOST_CHECK_MESSAGE (res == 0, "Encoded datas aren't equal");
}

BOOST_AUTO_TEST_CASE(AvroBinaryEncodingToStream)
{
    BasicEndpointProfile encodingProfile;
    encodingProfile.profileBody = "Test body";

    std::ostringstream stream1;
    AvroByteArrayConverter<BasicEndpointProfile> converter;
    converter.toByteArray(encodingProfile, stream1);
    const std::string& encodedData1 = stream1.str();

    BOOST_CHECK_MESSAGE(encodedData1.length() != 0, "Encoded data size is 0");

    std::ostringstream stream2;
    binaryEncodeDataTo(stream2, encodingProfile);
    const std::string& encodedData2 = stream2.str();

    int res = ::memcmp(encodedData1.data(), encodedData2.data(),
            std::min(encodedData1.length(), encodedData2.length()));

    BOOST_CHECK_MESSAGE (res == 0, "Encoded datas aren't equal");
}

BOOST_AUTO_TEST_CASE(SimpleAvroBinaryDecoding)
{
    BasicEndpointProfile encodingProfile;
    encodingProfile.profileBody = "Really big body...";

    AvroByteArrayConverter<BasicEndpointProfile> converter;
    SharedDataBuffer encodedData = converter.toByteArray(encodingProfile);

    BasicEndpointProfile decodedProfile1;
    converter.fromByteArray(encodedData.first.get(), encodedData.second, decodedProfile1);
    BasicEndpointProfile decodedProfile2 = decodeBinaryData<BasicEndpointProfile>(
                                            encodedData.first.get(), encodedData.second);

    BOOST_CHECK_MESSAGE (decodedProfile1.profileBody == decodedProfile2.profileBody, "Body aren't equal");
}

BOOST_AUTO_TEST_CASE(SchemaAvroBinaryDecoding)
{
    BasicEndpointProfile encodingProfile;
    encodingProfile.profileBody = "Really big body...";

    AvroByteArrayConverter<BasicEndpointProfile> encoder;
    SharedDataBuffer encodedData = encoder.toByteArray(encodingProfile);

    try {
        std::ifstream in(profile_schema_path);
        avro::ValidSchema schema;
        avro::compileJsonSchema(in, schema);
        AvroByteArrayConverter<avro::GenericDatum> decoder;
        avro::GenericDatum decodedDatum(schema);

        decoder.fromByteArray(encodedData.first.get(), encodedData.second, decodedDatum);

        const std::string& decodedProfileBody = decodedDatum.value<avro::GenericRecord>()
                                                    .fieldAt(0).value<std::string>();
        BasicEndpointProfile decodedProfile2 = decodeBinaryData<BasicEndpointProfile>(
                                                encodedData.first.get(), encodedData.second);

        BOOST_CHECK_MESSAGE(decodedProfileBody == decodedProfile2.profileBody, "Body aren't equal");
    } catch (std::exception& e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(AvroJSONEncoding)
{
    try {
        std::string jsonData("{\"profileBody\":\"dummy\"}");
        std::ifstream in(profile_schema_path);
        avro::ValidSchema schema;
        avro::compileJsonSchema(in, schema);
        avro::GenericDatum decodedDatum(schema);
        AvroByteArrayConverter<avro::GenericDatum> converter;

        converter.switchToJson(schema);

        converter.fromByteArray(reinterpret_cast<const std::uint8_t*>(jsonData.data())
                                                        , jsonData.length(), decodedDatum);

        const std::string& decodedProfileBody = decodedDatum.value<avro::GenericRecord>()
                                                    .fieldAt(0).value<std::string>();

        BOOST_CHECK_MESSAGE(decodedProfileBody == std::string("dummy"), "Body aren't equal");
    } catch (std::exception& e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_SUITE_END()

};
