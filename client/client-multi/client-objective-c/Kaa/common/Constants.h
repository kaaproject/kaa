/*
 * Copyright 2014-2015 CyberVision, Inc.
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

/**
 * Common Kaa project Constants.
 */

#ifndef Kaa_Constants_h
#define Kaa_Constants_h

/**
 * Used URI delimiter.
 */
#define URI_DELIM @"/"

/**
 * HTTP response content-type.
 */
#define RESPONSE_CONTENT_TYPE @"\"application/x-kaa\""

/**
 * HTTP response custom header for set RSA Signature encoded in base64
 */
#define SIGNATURE_HEADER_NAME @"X-SIGNATURE"

/**
 * The identifier for the Avro platform protocol
 */
#define KAA_PLATFORM_PROTOCOL_AVRO_ID (0xf291f2d4)

/**
 * The identifier for the Binary platform protocol
 */
#define KAA_PLATFORM_PROTOCOL_BINARY_ID (0x3553c66f)

/**
 * The size of sdk token
 */
#define SDK_TOKEN_SIZE 28

/**
 * The size of application token
 */
#define APP_TOKEN_SIZE 20

/**
 * The size of user verifier token
 */
#define USER_VERIFIER_TOKEN_SIZE 20

#endif
