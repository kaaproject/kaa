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

package org.kaaproject.kaa.common;

/**
 * Common Kaa project Constants.
 */
public interface Constants { //NOSONAR

    String URI_DELIM = "/"; //NOSONAR

    /**
     * HTTP response content-type.
     */
    String RESPONSE_CONTENT_TYPE = "\"application/x-kaa\""; //NOSONAR

    /**
     * HTTP response custom header for set RSA Signature encoded in base64
     */
    String SIGNATURE_HEADER_NAME = "X-SIGNATURE"; //NOSONAR


    //The identifier for the Avro platform protocol
    int KAA_PLATFORM_PROTOCOL_AVRO_ID = 0xf291f2d4;

    //cvc32 of AvroEncDecUseRawChema
    int KAA_PLATFORM_PROTOCOL_AVRO_ID_V2 = 0xe0c0c178;


    //The identifier for the Binary platform protocol
    int KAA_PLATFORM_PROTOCOL_BINARY_ID = 0x3553c66f;

    //cvc32 of BinaryEncDecUseRawSchema
    int KAA_PLATFORM_PROTOCOL_BINARY_ID_V2 = 0x0231ad61;


    int SDK_TOKEN_SIZE = 27;

    int APP_TOKEN_SIZE = 20;

    int USER_VERIFIER_TOKEN_SIZE = 20;

}
