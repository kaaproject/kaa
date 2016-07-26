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

#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>
#include <kaa_error.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#ifndef KAA_AES_RSA_H_
#define KAA_AES_RSA_H_

#define KAA_SESSION_KEY_LENGTH         16

/**
 * @brief generate AES key
 *
 * @param[out] key Generated AES key
 * @param[in] bytes Size of the AES key
 */
int init_aes_key(unsigned char *key, size_t bytes);

/**
 * @brief encrypt or decrypt with AES key
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
 * @brief create RSA signature
 *
 * @param[in]   pk            Pointer to a pk structure
 * @param[in]   input         Pointer to input data
 * @param[in]   input_size    Size of input data
 * @param[out]  output        Pointer to RSA signature
 * @param[out]  output_size   Size of RSA signature
 */
int rsa_sign(mbedtls_pk_context *pk, const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_size);

#endif /* KAA_AES_RSA_H */
