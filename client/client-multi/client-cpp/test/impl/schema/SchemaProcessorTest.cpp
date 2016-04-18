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

#include "kaa/schema/SchemaProcessor.hpp"
#include "kaa/schema/ISchemaUpdatesReceiver.hpp"
#include <cstdint>
#include <boost/test/unit_test.hpp>

namespace kaa {

class SchemaUpdatesReceiverStub : public ISchemaUpdatesReceiver
{
public:
    SchemaUpdatesReceiverStub() : schema_(nullptr), updateReceived_(nullptr) {}

    void onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema)
    {
        updateReceived_ = true;
        schema_ = schema;
    }

    bool isUpdateReceived() { return updateReceived_; }
    std::shared_ptr<avro::ValidSchema> getSchema() { return schema_; }

private:
    std::shared_ptr<avro::ValidSchema> schema_;
    bool updateReceived_;
};

static const std::size_t len = 31;
static const char * sch = "{\"name\":\"field1\", \"type\":\"int\"}";

BOOST_AUTO_TEST_SUITE(SchemaProcessorSuite)

BOOST_AUTO_TEST_CASE(schemaProcessorTest)
{
    SchemaUpdatesReceiverStub surstub1;
    SchemaProcessor sp;
    sp.subscribeForSchemaUpdates(surstub1);
    sp.loadSchema(reinterpret_cast<const std::uint8_t *>(sch), len);

    BOOST_CHECK(surstub1.isUpdateReceived());
    BOOST_CHECK(surstub1.getSchema().get() != nullptr);

    SchemaUpdatesReceiverStub surstub2, surstub3;
    sp.subscribeForSchemaUpdates(surstub2);
    sp.subscribeForSchemaUpdates(surstub3);

    sp.unsubscribeFromSchemaUpdates(surstub2);

    sp.loadSchema(reinterpret_cast<const std::uint8_t *>(sch), len);
    BOOST_CHECK(!surstub2.isUpdateReceived());
    BOOST_CHECK(surstub3.isUpdateReceived());
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
