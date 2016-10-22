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

#ifndef KAA_AES_RSA_H_
#define KAA_AES_RSA_H_

#include <kaa_error.h>

#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>
#include <kaa_error.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#define KAA_SESSION_KEY_LENGTH 16

/**
 * Generate AES key
 *
 * @param[out] key Generated AES key
 * @param[in] bytes Size of the AES key
 *
 * @return The error code.
 * @retval 0 Success.
 */
int init_aes_key(unsigned char *key, size_t bytes);

/**
 * Encrypt or decrypt with AES key
 *
 * @param[in]   mode          Mode of encryption
 * @param[in]   input         Pointer to input data
 * @param[in]   input_size    Size of input data
 * @param[out]  output        Pointer to output data
 * @param[in]   key           Pointer to AES key
 */
kaa_error_t aes_encrypt_decrypt(int mode, const uint8_t *input, size_t input_size,
        uint8_t *output, const uint8_t *key);

/**
 * Encrypt or decrypt block of size @c KAA_SESSION_KEY_LENGTH with AES key.
 *
 * @param[in]   mode          Mode of encryption. MBEDTLS_AES_ENCRYPT
 *                            to encryption and MBEDTLS_AES_DECTYPT to decryption.
 * @param[in]   input         Pointer to input data (of size KAA_SESSION_KEY_LENGTH)
 * @param[out]  output        Pointer to output data
 * @param[in]   key           Pointer to AES key
 */
kaa_error_t aes_encrypt_decrypt_block(int mode, const uint8_t *input, uint8_t *output,
        const uint8_t *key);

/**
 * Create RSA signature
 *
 * @param[in]   pk            Pointer to a pk structure
 * @param[in]   input         Pointer to input data
 * @param[in]   input_size    Size of input data
 * @param[out]  output        Pointer to RSA signature
 * @param[out]  output_size   Size of RSA signature
 */
int rsa_sign(mbedtls_pk_context *pk, const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_size);

/**
 * Encrypt data with pubcic RSA key.
 *
 * @param[in]   key         Public RSA key.
 * @param[in]   key_size    Size of public RSA key.
 * @param[in]   input       Input data.
 * @param[in]   input_len   Size of input_data.
 * @param[out]  output      Pointer to buffer where the encrypted data will be stored.
 *
 * @return The error code.
 * @retval KAA_ERR_NONE Success
 * @retval KAA_ERR_INVALID_PUBKEY The key is invalid.
 * @retval KAA_ERR_GENERIC Encryption has failed.
 */
kaa_error_t rsa_encrypt(const uint8_t *key, size_t key_size, const uint8_t *input,
        size_t input_len, uint8_t *output);

#endif /* KAA_AES_RSA_H_ */
