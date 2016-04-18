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

#include "kaa/schema/ISchemaProcessor.hpp"
#include "kaa/schema/storage/SchemaPersistenceManager.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <avro/Compiler.hh>

#include <boost/test/unit_test.hpp>

namespace kaa {

static const std::string  root_sch = "{ \"type\": \"array\",\"items\": {\"type\": \"record\",\"namespace\":"
        "\"config.system\",\"name\": \"deltaT\",\"fields\": [{\"name\": \"delta\",\"type\": [{"
                        "\"type\": \"record\","
                        "\"namespace\": \"org.kaa.config\","
                        "\"name\": \"testT\","
                        "\"fields\": ["
                        "{\"name\": \"testField1\",\"type\": \"string\"},"
                        "{\"name\": \"uuid\",\"type\": {"
                        "\"type\": \"fixed\","
                        "\"size\": 16,"
                        "\"namespace\": \"config.system\","
                        "\"name\": \"uuidT\"}}]} ]}]}}";

class SchemaProcessorStub : public ISchemaProcessor
{
public:
    SchemaProcessorStub() : loadSchemaCalled_(false) {}
    void loadSchema(const std::uint8_t * buffer, std::size_t size)
    {
        loadSchemaCalled_ = true;
    }
    void subscribeForSchemaUpdates(ISchemaUpdatesReceiver &receiver) {}
    void unsubscribeFromSchemaUpdates(ISchemaUpdatesReceiver &receiver) {}

    bool isLoadSchemaCalled() { return loadSchemaCalled_; }

    SchemaPtr getSchema() const { return SchemaPtr(); }

private:
    bool loadSchemaCalled_;
};

class SchemaStorageStub : public ISchemaStorage
{
public:
    SchemaStorageStub() : saveSchemaCalled_(false), loadSchemaCalled_(false) {
        data_.push_back('0');
    }
    void saveSchema(const byte_buffer &data)
    {
        saveSchemaCalled_ = true;
        data_ = data;
    }

    byte_buffer loadSchema()
    {
        loadSchemaCalled_ = true;
        return data_;
    }

    bool isLoadSchemaCalled() { return loadSchemaCalled_; }
    bool isSaveSchemaCalled() { return saveSchemaCalled_; }

private:
    byte_buffer data_;
    bool saveSchemaCalled_;
    bool loadSchemaCalled_;
};

BOOST_AUTO_TEST_SUITE(SchemaPersistenceSuite)

BOOST_AUTO_TEST_CASE(testEmptySchemaSet)
{
    SchemaPersistenceManager spm;

    std::shared_ptr<avro::ValidSchema> root_schema;
    BOOST_REQUIRE_THROW(spm.onSchemaUpdated(root_schema), KaaException);
}

BOOST_AUTO_TEST_CASE(testSchemaPersistence)
{
    SchemaPersistenceManager spm;

    try {
        SchemaProcessorStub spstub;
        SchemaStorageStub ssstub;
        spm.setSchemaProcessor(&spstub);
        BOOST_CHECK(!spstub.isLoadSchemaCalled());

        spm.setSchemaStorage(&ssstub);
        BOOST_CHECK(ssstub.isLoadSchemaCalled());
        BOOST_CHECK(!ssstub.isSaveSchemaCalled());
        BOOST_CHECK(spstub.isLoadSchemaCalled());

        std::shared_ptr<avro::ValidSchema> root_schema(new avro::ValidSchema(
                        avro::compileJsonSchemaFromMemory(reinterpret_cast<const std::uint8_t *>(root_sch.c_str()), root_sch.length())));
        // here one schema update should be ignored as it has to be called when non-empty schema is read from storage
        spm.onSchemaUpdated(root_schema);
        BOOST_CHECK(!ssstub.isSaveSchemaCalled());

        spm.onSchemaUpdated(root_schema);
        BOOST_CHECK(ssstub.isSaveSchemaCalled());

    } catch (...) {
        BOOST_CHECK(false);
    }
}


BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
