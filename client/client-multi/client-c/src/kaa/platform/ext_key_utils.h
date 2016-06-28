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
 * @file ext_key_utils.h
 * @brief External interface for client keys manipulations.
 */

#ifndef EXT_KEY_UTILS_H_
#define EXT_KEY_UTILS_H_

#include "../kaa/kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Called to get the endpoint public key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with public key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 *
 */
void ext_get_endpoint_public_key(uint8_t **buffer, size_t *buffer_size);

/**
 * @brief Performs initialization of the keys.
 *
 * In case if kaa channel has encryption, this function has to be
 * called to perform keys initialization.
 *
 * @return KAA_ERR_NONE in case of successful initialization. The other value means failure.
 */
kaa_error_t kaa_init_keys(void);

/**
 * @brief Performs deinitialization of hte  keys.
 *
 * In case if kaa channel has encryption, this function has to be
 * called to perform keys deinitialization.
 *
 */
void  kaa_deinit_keys(void);

/**
 * @brief Called to get endpoint session key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled encrypted session key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 */
void ext_get_endpoint_session_key(uint8_t **buffer, size_t *buffer_size);

/**
 * @brief Perform encryption.
 *
 * @param[in] input        The pointer to data to be encrypted.
 * @param[in] input_size   The size of the input buffer.
 * @param[out] output      The pointer which will initialized with buffer containing
 *                         enctypted data.
 */
kaa_error_t ext_encrypt_data(uint8_t *input, size_t input_size, uint8_t *output);

/**
 * @brief Perform decryption.
 *
 * @param[in]  input                The pointer to data to be decrypted.
 * @param[in]  input_size           The size of the input buffer.
 * @param[out] output_payload_size  The size of the payload data.
 * @param[out] output               The buffer which contains dectypted data will be filled in.
 *
 * @return error code.
 */
kaa_error_t ext_decrypt_data(uint8_t *input, size_t input_size,
                             uint8_t *output, size_t *output_payload_size);

/**
 * @brief Called to get endpoint session key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled encrypted session key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[in]   remote_key          Remote public key which will be used to encrypt session key.
 * @param[in]   remote_key_size     Remote public key's size.
 *
 * @return error code.
 */
kaa_error_t ext_get_encrypted_session_key(uint8_t **buffer, size_t *buffer_size,
                                          uint8_t *remote_key, size_t remote_key_size);
/**
 * @brief Calculates the size of the encrypted data.
 *
 * @param[in]  input_size The size of the plain text.
 * @param[out] output_size The size of the encrypted data.
 */
void ext_get_encrypted_data_size(size_t input_size, size_t *output_size);

/**
 * @brief Signes the encrypted session key.
 *
 * @param [in]  input            The input data to be signed.
 * @param [in]  input_size       The size of the input data.
 * @param [out] output           The pointer which will be initialized with signed key.
 * @param [out] output_length    The length of signed key.
 *
 */
kaa_error_t ext_get_signature(uint8_t *input, size_t input_size,
                              uint8_t **output, size_t *output_size);

/**
 * @brief Return sha1 of the RSA public key.
 *
 * @param [out] sha1   The pointer *sha1 will be initialized with SHA1 address
 *                     of the RSA public key.
 * @param [out] length The length of the SHA1.
 */
void ext_get_sha1_public(uint8_t **sha1, size_t *length);

/**
 * @brief Return sha1 of the RSA public key in base64 format.
 *
 * @param [out] sha1   The pointer *sha1 will be initialized with SHA1 address
 *                     of the RSA public key in base64 encoding
 * @param [out] length The length of the SHA1.
 */
void ext_get_sha1_base64_public(uint8_t **sha1, size_t *length);
#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_KEY_UTILS_H_ */
