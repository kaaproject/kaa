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

#ifndef ABSTRACTCONFIGURATIONDELTACREATOR_HPP_
#define ABSTRACTCONFIGURATIONDELTACREATOR_HPP_

#include <boost/cstdint.hpp>
#include <avro/Encoder.hh>
#include <avro/Stream.hh>
#include <vector>

#include "resources/AvroAutoGen.hpp"

namespace kaa {

class AbstractConfigurationDeltaCreator
{
public:
    virtual ~AbstractConfigurationDeltaCreator() {}
    virtual std::vector<deltaT> fillDelta() = 0;

    void createDelta(boost::uint8_t *&data, size_t &len)
    {
        avro::EncoderPtr encoder = avro::binaryEncoder();
        std::ostringstream os;

        std::unique_ptr<avro::OutputStream> aos = avro::ostreamOutputStream(os);
        encoder->init(*aos);

        avro::encode(*encoder, fillDelta());
        encoder->flush();

        std::string sdata = os.str();
        len = sdata.length();

        data = new boost::uint8_t[len];
        for (size_t i =0; i<len; ++i) {
            data[i] = (unsigned char)sdata.at(i);
        }
    }
};

}  // namespace kaa


#endif /* ABSTRACTCONFIGURATIONDELTACREATOR_HPP_ */
