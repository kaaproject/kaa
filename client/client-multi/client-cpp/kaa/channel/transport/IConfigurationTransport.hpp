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

#ifndef ICONFIGURATIONTRANSPORT_HPP_
#define ICONFIGURATIONTRANSPORT_HPP_

#include <memory>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

class IConfigurationProcessor;
class IConfigurationHashContainer;

/**
 * Updates the Configuration manager state.
 */
class IConfigurationTransport {
public:

    /**
     * Creates the configuration request.
     *
     * @return the configuration request object.
     * @see ConfigurationSyncRequest
     */
    virtual std::shared_ptr<ConfigurationSyncRequest> createConfigurationRequest() = 0;

    /**
     * Updates the state of the Configuration manager according to the given response.
     *
     * @param response the configuration response.
     * @see ConfigurationSyncResponse
     */
    virtual void onConfigurationResponse(const ConfigurationSyncResponse &response) = 0;

    /**
     * Sets the configuration hash container.
     *
     * @param container the container to be set.
     * @see IConfigurationHashContainer
     */
    virtual void setConfigurationHashContainer(IConfigurationHashContainer* container) = 0;

    /**
     * Sets the configuration processor.
     *
     * @param processor the processor to be set.
     * @see IConfigurationProcessor
     */
    virtual void setConfigurationProcessor(IConfigurationProcessor* processor) = 0;

    virtual ~IConfigurationTransport() = default;
};

}  // namespace kaa


#endif /* ICONFIGURATIONTRANSPORT_HPP_ */
