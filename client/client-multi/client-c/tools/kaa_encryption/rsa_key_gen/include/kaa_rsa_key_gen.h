/*
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @brief Kaa rsa key generation library
 */

#ifndef KAA_RSA_KEY_GEN_H_
#define KAA_RSA_KEY_GEN_H_

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <sys/stat.h>
#include <string.h>
#include <stdlib.h>

#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>
#include <mbedtls/sha1.h>
#include <mbedtls/base64.h>

/* Filename where public/private keys are stored */
#define KAA_KEYS_STORAGE "kaa_keys_gen.h"

/* RSA Endpoint definitions */
#define KAA_RSA_KEY_LENGTH 2048
#define KAA_RSA_EXPONENT   65537

#define KAA_RSA_PUBLIC_KEY_LENGTH_MAX  294
#define KAA_RSA_PRIVATE_KEY_LENGTH_MAX 1200

#define SHA1_LENGTH 20

#define PRIVATE_KEY 0
#define PUBLIC_KEY  1

/**
 * Structure which contains Endpoint keys.
 *
 * public_key is a pointer to RSA public key.
 * private_key is a pointer to RSA private key.
 *
 * note: the main purpose of the structure is
 * caching calculated keys.
 */
typedef struct {
    uint8_t public_key[KAA_RSA_PUBLIC_KEY_LENGTH_MAX]; ///< RSA public key
    uint8_t private_key[KAA_RSA_PRIVATE_KEY_LENGTH_MAX]; ///< RSA public key
    size_t  public_key_length; ///< RSA public key length
    size_t  private_key_length; ///< RSA private key length
} endpoint_keys_t;


/**
 * @brief generate RSA keys in mbedtls_pk_context
 */
int rsa_genkey(mbedtls_pk_context *pk);

/**
 * @brief create RSA keys and store then in der format
 */
int rsa_keys_create(mbedtls_pk_context *pk, uint8_t *public_key,
        size_t *public_key_length, uint8_t *private_key,
        size_t *private_key_length);

/**
 * @brief generate sha1 representation of the public key
 */
int sha1_from_public_key(uint8_t *key, size_t length, uint8_t *sha1);

/**
 * @brief generate base64 representation of the public key
 */
int sha1_to_base64(uint8_t *key, size_t length, uint8_t *base64, size_t base64_len, size_t *output_len);

/**
 * @brief store RSA keys, sha1 and sha1_base64
 */
int kaa_keys_store(uint8_t *public_key, size_t public_key_length,
        uint8_t *private_key, size_t private_key_length);

/**
 * @brief Write RSA public or private key in pem format
 *
 * @param[in]   key           Pointer to a pk structure
 * @param[in]   output_file   File path
 * @param[in]   mode          Mode of public or private key storage
 */
int write_rsa_key(mbedtls_pk_context *key, const char *output_file, int mode);

#endif /* KAA_RSA_KEY_GEN_H */
