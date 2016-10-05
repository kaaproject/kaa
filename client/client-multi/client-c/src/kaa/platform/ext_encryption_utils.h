/*
 * You may obtain a copy of the License at
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
 * @brief External interface for encryption.
 */

#ifndef EXT_ENCRYPTION_UTILS_H_
#define EXT_ENCRYPTION_UTILS_H_

#include <kaa/kaa_error.h>

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Performs initialization of the AES session key.
 *
 * This key is required in case encryption is enabled.
 *
 */
kaa_error_t kaa_init_session_key(void);

/**
 * Performs deinitialization of the AES session key.
 *
 */
void kaa_deinit_session_key(void);

/**
 * Returns endpoint session key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with session key.
 * @param[out]  buffer_size         Pointer to buffer size, which will be initialized with key size.
 */
void ext_get_endpoint_session_key(const uint8_t **buffer, size_t *buffer_size);

/**
 * Perform encryption.
 *
 * @param[in] input        The pointer to data to be encrypted.
 * @param[in] input_size   The size of the input buffer.
 * @param[out] output      The pointer which will be initialized with buffer containing
 *                         enctypted data.
 */
kaa_error_t ext_encrypt_data(const uint8_t *input, size_t input_size, uint8_t *output);

/**
 * Calculates the size of the encrypted data.
 *
 * @param[in]  input_size The size of the plaintext.
 *
 */
size_t ext_get_encrypted_data_size(size_t input_size);

/**
 * Perform decryption.
 *
 * @param[in]  input                The pointer to data to be decrypted.
 * @param[in]  input_size           The size of the input buffer.
 * @param[out] output_payload_size  The size of the payload data.
 * @param[out] output               The buffer which contains decrypted data will be filled in.
 *
 * @note The output buffer is assumed to be at least as long as @p input_size.
 *
 */
kaa_error_t ext_decrypt_data(const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_payload_size);

/**
 * Returns encrypted endpoint session key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with encrypted session key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[in]   remote_key          Remote public key which will be used to encrypt session key.
 * @param[in]   remote_key_size     Remote public key's size.
 *
 */
kaa_error_t ext_get_encrypted_session_key(uint8_t **buffer, size_t *buffer_size,
        const uint8_t *remote_key, size_t remote_key_size);

/**
 * Signs the encrypted session key.
 *
 * @param [in]  input            The input data to be signed.
 * @param [in]  input_size       The size of the input data.
 * @param [out] output           The pointer which will be initialized with signed key.
 * @param [out] output_size      The length of signed key.
 *
 */
kaa_error_t ext_get_signature(const uint8_t *input, size_t input_size,
                              uint8_t **output, size_t *output_size);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_ENCRYPTION_UTILS_H_ */
