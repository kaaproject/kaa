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

#ifndef GENERICSCHEMACONVERTER_HPP_
#define GENERICSCHEMACONVERTER_HPP_

#include <avro/ValidSchema.hh>
#include <cstdint>
#include <memory>

#include <kaa/common/exception/KaaException.hpp>

namespace kaa {

class GenericAvroConverter {
public:
    typedef avro::ValidSchema Schema;

    GenericAvroConverter(Schema schema) { schema_.reset(new Schema(schema)); }
    GenericAvroConverter(std::shared_ptr<Schema> schema) { schema_ = schema; }
    ~GenericAvroConverter() { schema_.reset(); }

    template <typename T>
    T decodeBinary(const std::uint8_t *data, std::size_t data_length) throw (KaaException);

    template <typename T>
    void decodeBinary(const std::uint8_t *data, std::size_t data_length, T& result) throw (KaaException);

    template <typename T>
    T decodeJson(const std::uint8_t *data, std::size_t data_length) throw (KaaException);

    template <typename T>
    size_t encode(T &value, std::ostream &os) throw (KaaException);

private:
    std::shared_ptr<Schema> schema_;
};

}  // namespace kaa


#endif /* GENERICSCHEMACONVERTER_HPP_ */
