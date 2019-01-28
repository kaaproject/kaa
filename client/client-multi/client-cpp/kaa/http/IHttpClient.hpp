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

#ifndef IHTTPCLIENT_HPP_
#define IHTTPCLIENT_HPP_

#include "kaa/KaaDefaults.hpp"

#include <memory>
#include "kaa/http/IHttpResponse.hpp"
#include "kaa/http/IHttpRequest.hpp"

#include "kaa/channel/IKaaChannelManager.hpp"

namespace kaa {

class IHttpClient
{
public:
    /**
     * Send HTTP request.
     *
     * @param[in] request    the http request which will be sent.
     * @param[in] connection the structure which is filled in case of successful connection establishment.
     *
     * @return response to the request.
     */
    virtual std::shared_ptr<IHttpResponse> sendRequest(const IHttpRequest& request, EndpointConnectionInfo *connection) = 0;

    /**
     * Close HTTP connection.
     */
    virtual void closeConnection() = 0;

    virtual ~IHttpClient() = default;
};

}

#endif /* IHTTPCLIENT_HPP_ */
