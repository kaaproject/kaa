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

/**
 * @file
 * @brief External interface for client keys manipulations.
 */

#ifndef EXT_KEY_UTILS_H_
#define EXT_KEY_UTILS_H_

#include <kaa/kaa_error.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Performs initialization of the RSA keypair (if required).
 *
 */
kaa_error_t kaa_init_rsa_keypair(void);

/**
 * Performs deinitialization of the RSA keypair.
 *
 */
void kaa_deinit_rsa_keypair(void);

/**
 * Returns the endpoint public key.
 *
 * @param[out]  buffer              Pointer which will be set to internal buffer containing the key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 *
 */
void ext_get_endpoint_public_key(const uint8_t **buffer, size_t *buffer_size);

/**
 * Return sha1 of the endpoint public key.
 *
 * @param [out] sha1   The pointer *sha1 will be initialized with SHA1 address
 *                     of the RSA public key.
 * @param [out] length The length of the SHA1.
 */
void ext_get_sha1_public(uint8_t **sha1, size_t *length);

/**
 * Return sha1 of the endpoint public key in base64 format.
 *
 * @param [out] sha1   The pointer *sha1 will be initialized with SHA1 address
 *                     of the RSA public key in base64 encoding
 * @param [out] length The length of the SHA1.
 */
kaa_error_t ext_get_sha1_base64_public(const uint8_t **sha1, size_t *length);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_KEY_UTILS_H_ */
