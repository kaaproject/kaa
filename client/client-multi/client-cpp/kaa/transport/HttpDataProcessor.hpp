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

#ifndef HTTPDATAPROCESSOR_HPP_
#define HTTPDATAPROCESSOR_HPP_

#include "kaa/KaaDefaults.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

#include <vector>
#include <memory>
#include <cstdint>

#include <boost/noncopyable.hpp>

#include <botan/base64.h>

#include "kaa/http/IHttpRequest.hpp"
#include "kaa/http/IHttpResponse.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/http/MultipartPostHttpRequest.hpp"
#include "kaa/transport/TransportException.hpp"


namespace kaa {

class HttpDataProcessor : boost::noncopyable {
public:
    HttpDataProcessor(const PublicKey& pubKey,
            const PrivateKey& privKey,
            const PublicKey& remoteKey) :
            encDec_(new RsaEncoderDecoder(pubKey, privKey, remoteKey)) { }
    HttpDataProcessor() { }

    std::shared_ptr<IHttpRequest> createOperationRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data);
    std::string retrieveOperationResponse(const IHttpResponse& response);

    std::shared_ptr<IHttpRequest> createBootstrapRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data);
    std::string retrieveBootstrapResponse(const IHttpResponse& response);

    void setEncoderDecoder(std::shared_ptr<IEncoderDecoder> encoderDecoder) { encDec_ = encoderDecoder; }

private:
    std::shared_ptr<IHttpRequest> createHttpRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data, bool sign);

    void verifyResponse(const IHttpResponse& response);

private:
    std::shared_ptr<IEncoderDecoder> encDec_;

};

typedef std::shared_ptr<HttpDataProcessor> HttpDataProcessorPtr;

}

#endif

#endif /* HTTPDATAPROCESSOR_HPP_ */
