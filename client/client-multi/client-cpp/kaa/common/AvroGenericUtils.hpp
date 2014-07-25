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

#ifndef AVROGENERICUTILS_HPP_
#define AVROGENERICUTILS_HPP_

#include <algorithm>
#include <stdexcept>

#include <avro/Generic.hh>

#include "kaa/common/types/ICommonRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/configuration/delta/DeltaHandlerId.hpp"

namespace kaa {

/**
 * Common tools for avro generic objects.
 */
class AvroGenericUtils {
public:
    /**
     * Tells if the given value is UUID (value's schema is "org.kaaproject.configuration.uuidT").
     *
     * @param d object which going to be verified.
     * @return true if the value is UUID, false otherwise.
     *
     */
    static bool isUuid(const avro::GenericDatum &d) {
        if (d.type() == avro::AVRO_FIXED) {
            auto f = d.value<avro::GenericFixed>();
            return f.schema()->name().fullname().compare(AvroGenericUtils::UUIDT) == 0;
        }
        return false;
    }

    /**
     * Tells if the given value is GenericFixed.
     *
     * @param d object which going to be verified.
     * @return true if the value is GenericFixed, false otherwise.
     *
     */
    static bool isFixed(const avro::GenericDatum &d) {
        return (d.type() == avro::AVRO_FIXED);
    }

    /**
     * Tells if the given value is GenericEnum.
     *
     * @param d object which going to be verified.
     * @return true if the value is GenericEnum, false otherwise.
     *
     */
    static bool isEnum(const avro::GenericDatum &d) {
        return (d.type() == avro::AVRO_ENUM);
    }

    /**
     * Tells if the given value is GenericRecord.
     *
     * @param d object which going to be verified.
     * @return true if the value is GenericRecord, false otherwise.
     *
     */
    static bool isRecord(const avro::GenericDatum &d) {
        return (d.type() == avro::AVRO_RECORD);
    }

    /**
     * Tells if the given value is GenericArray.
     *
     * @param d object which going to be verified.
     * @return true if the value is GenericArray, false otherwise.
     *
     */
    static bool isArray(const avro::GenericDatum &d) {
        return (d.type() == avro::AVRO_ARRAY);
    }

    /**
     * Tells if the given value is GenericEnum.
     *
     * @param d object which going to be verified.
     * @return true if the value is GenericEnum, false otherwise.
     *
     */
    static bool isNull(const avro::GenericDatum &d) {
        return d.type() == avro::AVRO_NULL;
    }

    /**
     * Tells if the given value is reset type.
     *
     * @param d object which going to be verified.
     * @return true if the value is reset type, false otherwise.
     *
     */
    static bool isReset(const avro::GenericDatum &d) {
        if (d.type() != avro::AVRO_ENUM) {
            return false;
        }
        const avro::GenericEnum &e = d.value<avro::GenericEnum>();
        return e.schema()->name().fullname().compare(AvroGenericUtils::RESETT) == 0;
    }

    /**
     * Tells if the given value is unchanged.
     *
     * @param d object which going to be verified.
     * @return true if the value is unchanged, false otherwise.
     *
     */
    static bool isUnchanged(const avro::GenericDatum &d) {
        if (d.type() != avro::AVRO_ENUM) {
            return false;
        }
        const avro::GenericEnum &e = d.value<avro::GenericEnum>();
        return e.schema()->name().fullname().compare(AvroGenericUtils::UNCHANGEDT) == 0;
    }

    /**
     * Retrieves UUID from the given GenericDatum object.
     *
     * @return uuid object.
     * @throw KaaException Avro fixed object is not full to create uuid
     */
    static uuid_t getUuidFromDatum(const avro::GenericDatum& datum) {
        uuid_t uuid;
        const avro::GenericFixed& uuidFixed = datum.value<avro::GenericRecord>().
                field("__uuid").value<avro::GenericFixed>();

        if (uuidFixed.value().size() != uuid.size()) {
            throw KaaException("invalid uuid data");
        }

        std::copy(uuidFixed.value().begin(), uuidFixed.value().end(), uuid.begin());
        return uuid;
    }

    /**
     * Convert UUID from the given GenericDatum object to @link DeltaHandlerId @endlink.
     *
     * @return uuid object.
     * @throw KaaException Avro fixed object is not full to create uuid
     */
    static DeltaHandlerId getDeltaIDFromDatum(const avro::GenericDatum& datum) {
        uuid_t uuid;
        const avro::GenericFixed& uuidFixed = datum.value<avro::GenericFixed>();

        if (uuidFixed.value().size() != uuid.size()) {
            throw KaaException("invalid uuid data");
        }

        std::copy(uuidFixed.value().begin(), uuidFixed.value().end(), uuid.begin());

        DeltaHandlerId deltaId(uuid);
        return deltaId;
    }

private:
    static const std::string RESETT;
    static const std::string UNCHANGEDT;
    static const std::string UUIDT;
};

}  // namespace kaa


#endif /* AVROGENERICUTILS_HPP_ */
