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

package org.kaaproject.kaa.client.channel.impl.channels;

import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.endpoint.CommonEpConstans;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;

public class HttpRequestCreator {

  public static final Logger LOG = LoggerFactory //NOSONAR
      .getLogger(HttpRequestCreator.class);

  private HttpRequestCreator() {
  }

  static LinkedHashMap<String, byte[]> createOperationHttpRequest(
          byte[] body, MessageEncoderDecoder messageEncDec) throws GeneralSecurityException {
    return createHttpRequest(body, messageEncDec, true);
  }

  static LinkedHashMap<String, byte[]> createBootstrapHttpRequest(
          byte[] body, MessageEncoderDecoder messageEncDec) throws GeneralSecurityException {
    return createHttpRequest(body, messageEncDec, false);
  }

  static LinkedHashMap<String, byte[]> createHttpRequest(
          byte[] body, MessageEncoderDecoder messageEncDec, boolean sign)
          throws GeneralSecurityException {
    if (body != null && messageEncDec != null) {
      byte[] requestKeyEncoded = messageEncDec.getEncodedSessionKey();
      byte[] requestBodyEncoded = messageEncDec.encodeData(body);
      byte[] signature = null;
      if (sign) {
        signature = messageEncDec.sign(requestKeyEncoded);
      }

      if (LOG.isTraceEnabled()) {
        if (sign) {
          LOG.trace("Signature size: {}", signature.length);
          LOG.trace(MessageEncoderDecoder.bytesToHex(signature));
        }
        LOG.trace("RequestKeyEncoded size: {}", requestKeyEncoded.length);
        LOG.trace(MessageEncoderDecoder.bytesToHex(requestKeyEncoded));
        LOG.trace("RequestBodyEncoded size: {}", requestBodyEncoded.length);
        LOG.trace(MessageEncoderDecoder.bytesToHex(requestBodyEncoded));
      }
      LinkedHashMap<String, byte[]> requestEntity = new LinkedHashMap<String, byte[]>(); //NOSONAR
      if (sign) {
        requestEntity.put(CommonEpConstans.REQUEST_SIGNATURE_ATTR_NAME, signature);
      }
      byte[] nextProtocol = ByteBuffer.allocate(4).putInt(
              Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID_V2).array();
      requestEntity.put(CommonEpConstans.REQUEST_KEY_ATTR_NAME, requestKeyEncoded);
      requestEntity.put(CommonEpConstans.REQUEST_DATA_ATTR_NAME, requestBodyEncoded);
      requestEntity.put(CommonEpConstans.NEXT_PROTOCOL_ATTR_NAME, nextProtocol);

      return requestEntity;
    }
    return null;
  }

}
