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

#include "kaa/transport/HttpDataProcessor.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

namespace kaa {

void HttpDataProcessor::verifyResponse(const IHttpResponse& response)
{
    if (response.getStatusCode() != 200) {
        throw TransportException(boost::format("Invalid response code %1%") % response.getStatusCode());
    }
    SharedBody rawResponse = response.getBody();
    const std::string& signature = response.getHeaderField("X-SIGNATURE");
    boost::scoped_array<std::uint8_t> decodedSignature(new std::uint8_t[signature.length() / 3 * 4]);
    size_t sigLength = Botan::base64_decode(decodedSignature.get(), signature);
    if (!encDec_->verifySignature(rawResponse.first.get(), rawResponse.second, decodedSignature.get(), sigLength)) {
        throw TransportException("Failed to verify signature");
    }
}

std::shared_ptr<IHttpRequest> HttpDataProcessor::createOperationRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data)
{
    return createHttpRequest(url, data, true);
}

std::string HttpDataProcessor::retrieveOperationResponse(const IHttpResponse& response)
{
    SharedBody rawResponse = response.getBody();
    return encDec_->decodeData(rawResponse.first.get(), rawResponse.second);
}

std::shared_ptr<IHttpRequest> HttpDataProcessor::createBootstrapRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data)
{
    return createHttpRequest(url, data, false);
}

std::string HttpDataProcessor::retrieveBootstrapResponse(const IHttpResponse& response)
{
    return retrieveOperationResponse(response);
}

std::shared_ptr<IHttpRequest> HttpDataProcessor::createHttpRequest(const HttpUrl& url, const std::vector<std::uint8_t>& data, bool sign)
{
    std::shared_ptr<MultipartPostHttpRequest> post(new MultipartPostHttpRequest(url, context_));
    const EncodedSessionKey& encodedSessionKey = encDec_->getEncodedSessionKey();
    const std::string& bodyEncoded = encDec_->encodeData(data.data(), data.size());

    if (sign) {
        const Signature& clientSignature =
                encDec_->signData(reinterpret_cast<const std::uint8_t *>(
                        encodedSessionKey.data()), encodedSessionKey.size());

        post->setBodyField("signature", std::vector<std::uint8_t>(
                                        clientSignature.data(),
                                        clientSignature.data()+clientSignature.size()));
    }

    post->setBodyField("requestKey", std::vector<std::uint8_t>(encodedSessionKey.begin(), encodedSessionKey.end()));
    post->setBodyField("requestData", std::vector<std::uint8_t>(bodyEncoded.begin(), bodyEncoded.end()));

    return post;
}

}

#endif

