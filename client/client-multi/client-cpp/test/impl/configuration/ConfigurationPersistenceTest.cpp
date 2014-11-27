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

#include "kaa/configuration/storage/ConfigurationPersistenceManager.hpp"
#include "kaa/common/types/CommonRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/CommonTypesFactory.hpp"

#include <avro/Compiler.hh>

#include <boost/test/unit_test.hpp>

namespace kaa {

class ConfigurationProcessorStub : public IConfigurationProcessor
{
public:
    ConfigurationProcessorStub() : processConfigurationCalled_(false), schemaUpdatedCalled_(false) {}
    void processConfigurationData(const std::uint8_t *data, std::size_t dataLength, bool fullResync)
    {
        processConfigurationCalled_ = true;
    }
    void onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema)
    {
        schemaUpdatedCalled_ = true;
    }
    void subscribeForUpdates(IGenericDeltaReceiver &receiver) {}
    void unsubscribeFromUpdates(IGenericDeltaReceiver &receiver) {}
    void addOnProcessedObserver(IConfigurationProcessedObserver &observer) {}
    void removeOnProcessedObserver(IConfigurationProcessedObserver &observer) {}

    bool isProcessConfigurationCalled() { return processConfigurationCalled_; }
    bool isSchemaUpdateCalled() { return schemaUpdatedCalled_; }

private:
    bool processConfigurationCalled_;
    bool schemaUpdatedCalled_;
};

class CoonfigurationStorageStub : public IConfigurationStorage {
public:
    CoonfigurationStorageStub() : configurationSaveCalled_(false), configurationLoadCalled_(false) {
        configuration_.push_back('0');
    }
    void saveConfiguration(const std::vector<std::uint8_t> &bytes)
    {
        configurationSaveCalled_ = true;
        configuration_ = bytes;
    }
    std::vector<std::uint8_t> loadConfiguration()
    {
        configurationLoadCalled_ = true;
        return configuration_;
    }

    bool isSaveCalled() { return configurationSaveCalled_; }
    bool isLoadCalled() { return configurationLoadCalled_; }
private:
    bool configurationSaveCalled_;
    bool configurationLoadCalled_;
    std::vector<std::uint8_t> configuration_;
};


static const std::string  root_sch = "{ \"type\": \"array\",\"items\": {\"type\": \"record\",\"namespace\":"
        "\"org.kaaproject.configuration\",\"name\": \"deltaT\",\"fields\": [{\"name\": \"delta\",\"type\": [{"
                        "\"type\": \"record\","
                        "\"namespace\": \"org.kaa.config\","
                        "\"name\": \"testT\","
                        "\"fields\": ["
                        "{\"name\": \"testField1\",\"type\": \"string\"},"
                        "{\"name\": \"__uuid\",\"type\": {"
                        "\"type\": \"fixed\","
                        "\"size\": 16,"
                        "\"namespace\": \"org.kaaproject.configuration\","
                        "\"name\": \"uuidT\"}}]} ]}]}}";


static const std::string  sch = "{\"type\": \"record\","
                        "\"namespace\": \"org.kaa.config\","
                        "\"name\": \"testT\","
                        "\"fields\": ["
                        "{\"name\": \"testField1\",\"type\": \"string\"},"
                        "{\"name\": \"__uuid\",\"type\": {"
                        "\"type\": \"fixed\","
                        "\"size\": 16,"
                        "\"namespace\": \"org.kaaproject.configuration\","
                        "\"name\": \"uuidT\"}}]}";

BOOST_AUTO_TEST_SUITE(ConfigurationPersistenceSuite)

BOOST_AUTO_TEST_CASE(checkSchemaSetup)
{
    ConfigurationPersistenceManager cpm;

    std::shared_ptr<avro::ValidSchema> schema;
    BOOST_REQUIRE_THROW(cpm.onSchemaUpdated(schema), KaaException);

    schema.reset(new avro::ValidSchema());
    BOOST_REQUIRE_NO_THROW(cpm.onSchemaUpdated(schema));
}

BOOST_AUTO_TEST_CASE(checkConfigurationLoad)
{
    std::shared_ptr<avro::ValidSchema> schema(new avro::ValidSchema(
             avro::compileJsonSchemaFromMemory(reinterpret_cast<const std::uint8_t *>(sch.c_str()), sch.length())));
    uuid_t uuid = {{0,1,2,3,4}};
    std::vector<std::uint8_t> uuid_vec = {0,1,2,3,4};
    CommonRecord rec(uuid, schema->root());
    std::string testField1("string");
    avro::GenericDatum sd(testField1);
    rec.setField("testField1", CommonTypesFactory::createCommon<avro::AVRO_STRING>(sd));
    size_t uuid_index;
    schema->root()->nameIndex("__uuid", uuid_index);
    avro::GenericDatum ud(schema->root()->leafAt(uuid_index));
    avro::GenericFixed uuid_fixed_field(schema->root()->leafAt(uuid_index));
    uuid_fixed_field.value() = uuid_vec;
    ud.value<avro::GenericFixed>() = uuid_fixed_field;
    rec.setField("__uuid", CommonTypesFactory::createCommon<avro::AVRO_FIXED>(ud));

    ConfigurationPersistenceManager cpm;
    BOOST_REQUIRE_THROW(cpm.onConfigurationUpdated(rec), KaaException);

    std::shared_ptr<avro::ValidSchema> root_schema(new avro::ValidSchema(
            avro::compileJsonSchemaFromMemory(reinterpret_cast<const std::uint8_t *>(root_sch.c_str()), root_sch.length())));
    cpm.onSchemaUpdated(root_schema);
    try {
        ConfigurationProcessorStub cpstub;

        CoonfigurationStorageStub csstub;
        cpm.setConfigurationProcessor(&cpstub);
        cpm.setConfigurationStorage(&csstub);

        BOOST_CHECK(csstub.isLoadCalled());
        BOOST_CHECK(!csstub.isSaveCalled());
        BOOST_CHECK(cpstub.isProcessConfigurationCalled());

        cpm.onConfigurationUpdated(rec);
        BOOST_CHECK(!csstub.isSaveCalled());

        cpm.onConfigurationUpdated(rec);
        BOOST_CHECK(csstub.isSaveCalled());

        BOOST_CHECK(cpm.getConfigurationHash().getHash().first.get() != nullptr);
        BOOST_CHECK(cpm.getConfigurationHash().getHash().second > 0);

    } catch (...) {
        BOOST_CHECK(false);
    }
}

BOOST_AUTO_TEST_CASE(checkConfigurationLoadWithoutSchema)
{
    std::shared_ptr<avro::ValidSchema> schema(new avro::ValidSchema(
             avro::compileJsonSchemaFromMemory(reinterpret_cast<const std::uint8_t *>(sch.c_str()), sch.length())));
    uuid_t uuid = {{0,1,2,3,4}};
    std::vector<std::uint8_t> uuid_vec = {0,1,2,3,4};
    CommonRecord rec(uuid, schema->root());
    std::string testField1("string");
    avro::GenericDatum sd(testField1);
    rec.setField("testField1", CommonTypesFactory::createCommon<avro::AVRO_STRING>(sd));
    size_t uuid_index;
    schema->root()->nameIndex("__uuid", uuid_index);
    avro::GenericDatum ud(schema->root()->leafAt(uuid_index));
    avro::GenericFixed uuid_fixed_field(schema->root()->leafAt(uuid_index));
    uuid_fixed_field.value() = uuid_vec;
    ud.value<avro::GenericFixed>() = uuid_fixed_field;
    rec.setField("__uuid", CommonTypesFactory::createCommon<avro::AVRO_FIXED>(ud));

    ConfigurationPersistenceManager cpm;
    BOOST_REQUIRE_THROW(cpm.onConfigurationUpdated(rec), KaaException);

    std::shared_ptr<avro::ValidSchema> root_schema(new avro::ValidSchema(
            avro::compileJsonSchemaFromMemory(reinterpret_cast<const std::uint8_t *>(root_sch.c_str()), root_sch.length())));
    ConfigurationProcessorStub cpstub;

    CoonfigurationStorageStub csstub;
    cpm.setConfigurationProcessor(&cpstub);
    cpm.setConfigurationStorage(&csstub);
    cpm.onSchemaUpdated(root_schema);

    BOOST_CHECK(csstub.isLoadCalled());
    BOOST_CHECK(!csstub.isSaveCalled());
    BOOST_CHECK(cpstub.isProcessConfigurationCalled());

    cpm.onConfigurationUpdated(rec);
    BOOST_CHECK(!csstub.isSaveCalled());

    cpm.onConfigurationUpdated(rec);
    BOOST_CHECK(csstub.isSaveCalled());

    BOOST_CHECK(cpm.getConfigurationHash().getHash().first.get() != nullptr);
    BOOST_CHECK(cpm.getConfigurationHash().getHash().second > 0);

}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
