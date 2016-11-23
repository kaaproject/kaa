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

package org.kaaproject.kaa.client.transport;

import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;

public abstract class AbstractHttpClient {

  protected final String url;
  private final MessageEncoderDecoder messageEncDec;
  private boolean verificationEnabled = true;

  public AbstractHttpClient(String url, PrivateKey privateKey,
                            PublicKey publicKey, PublicKey remotePublicKey) {
    this.url = url;
    this.messageEncDec = new MessageEncoderDecoder(privateKey, publicKey, remotePublicKey);
  }

  protected void disableVerification() {
    this.verificationEnabled = false;
  }

  protected byte[] verifyResponse(byte[] body, byte[] signature) throws GeneralSecurityException {
    if (!verificationEnabled || messageEncDec.verify(body, signature)) {
      return body;
    } else {
      throw new SecurityException("message can't be verified");
    }
  }

  public MessageEncoderDecoder getEncoderDecoder() {
    return messageEncDec;
  }

  public abstract byte[] executeHttpRequest(String uri, LinkedHashMap<String, byte[]> entity,
                                            boolean verifyResponse) throws Exception; //NOSONAR

  public abstract void close() throws IOException;

  public abstract void abort();

  public abstract boolean canAbort();

}
