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

#ifndef FIELDPROCESSOR_HPP_
#define FIELDPROCESSOR_HPP_

#include "kaa/configuration/manager/AbstractStrategy.hpp"

namespace kaa {

/**
 * Processes a field in record using concrete processing strategy.
 */
class FieldProcessor {
public:
    FieldProcessor(std::shared_ptr<ICommonRecord> parent, const std::string &field)
    : strategy_(nullptr), parentRecord_(parent), field_(field) {}
    ~FieldProcessor() { if (strategy_) delete strategy_;}

    /**
     * Sets a strategy for processing.
     *
     * @param strategy  Pointer to a strategy. Memory will be freed when this destroyed.
     */
    void setStrategy(AbstractStrategy *strategy) { strategy_ = strategy; }
    void process(const avro::GenericDatum &d) {
        strategy_->run(parentRecord_, field_, d);
    }

private:
    AbstractStrategy * strategy_;
    std::shared_ptr<ICommonRecord> parentRecord_;
    const std::string &field_;
};

}  // namespace kaa

#endif /* FIELDPROCESSOR_HPP_ */
