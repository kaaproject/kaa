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

#include "kaa/transport/HttpDataProcessor.hpp"

namespace kaa {

void HttpDataProcessor::verifyResponse(const IHttpResponse& response)
{
    if (response.getStatusCode() != 200) {
        throw TransportException(boost::format("Invalid response code %1%") % response.getStatusCode());
    }
    SharedBody rawResponse = response.getBody();
    const std::string& signature = response.getHeaderField("X-SIGNATURE");
    boost::scoped_array<boost::uint8_t> decodedSignature(new boost::uint8_t[signature.length() / 3 * 4]);
    size_t sigLength = Botan::base64_decode(decodedSignature.get(), signature);
    if (!encDec_->verifySignature(rawResponse.first.get(), rawResponse.second, decodedSignature.get(), sigLength)) {
        throw TransportException("Failed to verify signature");
    }
}

boost::shared_ptr<IHttpRequest> HttpDataProcessor::createOperationRequest(const HttpUrl& url, const std::vector<boost::uint8_t>& data)
{
    const Botan::SecureVector<boost::uint8_t>& encodedSessionKey = encDec_->getEncodedSessionKey();
    const std::string& bodyEncoded = encDec_->encodeData(data.data(), data.size());
    const Botan::SecureVector<boost::uint8_t>& clientSignature =
            encDec_->signData(reinterpret_cast<const boost::uint8_t *>(
                    encodedSessionKey.begin()), encodedSessionKey.size());

    boost::shared_ptr<MultipartPostHttpRequest> post(new MultipartPostHttpRequest(url));
    post->setBodyField("signature",
            std::vector<boost::uint8_t>(
                    reinterpret_cast<const boost::uint8_t *>(clientSignature.begin()),
                    reinterpret_cast<const boost::uint8_t *>(clientSignature.end())));
    post->setBodyField("requestKey", std::vector<boost::uint8_t>(encodedSessionKey.begin(), encodedSessionKey.end()));
    post->setBodyField("requestData", std::vector<boost::uint8_t>(bodyEncoded.begin(), bodyEncoded.end()));
    return post;
}

std::string HttpDataProcessor::retrieveOperationResponse(const IHttpResponse& response)
{
    SharedBody rawResponse = response.getBody();
    return encDec_->decodeData(rawResponse.first.get(), rawResponse.second);
}

boost::shared_ptr<IHttpRequest> HttpDataProcessor::createBootstrapRequest(const HttpUrl& url, const std::vector<boost::uint8_t>& data)
{
    boost::shared_ptr<MultipartPostHttpRequest> post(new MultipartPostHttpRequest(url));
    post->setBodyField("Application-Token", data);
    return post;
}

std::string HttpDataProcessor::retrieveBootstrapResponse(const IHttpResponse& response)
{
    verifyResponse(response);
    SharedBody body = response.getBody();
    return std::string(reinterpret_cast<const char *>(body.first.get()), body.second);
}

}


