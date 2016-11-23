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

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <sys/stat.h>
#include <string.h>
#include <platform/ext_key_utils.h>
#include <utilities/kaa_mem.h>
#include <utilities/kaa_aes_rsa.h>
#include <kaa_common.h>
#include <kaa_error.h>

#include <mbedtls/pk.h>

#ifdef KAA_RUNTIME_KEY_GENERATION
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>

#include <kaa_rsa_key_gen.h>

#define KAA_PRIVATE_KEY_STORAGE "key.private"
#define KAA_PUBLIC_KEY_STORAGE "key.public"

#define KAA_RSA_PUBLIC_KEY_LENGTH_MAX  294
#define KAA_SHA1_PUB_LEN               20
#define KAA_SHA1_PUB_BASE64_LEN        28

static mbedtls_pk_context pk_pub_context;
#else
#include <kaa_keys_gen.h>
#endif /* KAA_RUNTIME_KEY_GENERATION */


mbedtls_pk_context kaa_pk_context_;

kaa_error_t kaa_init_rsa_keypair(void)
{
#ifdef KAA_RUNTIME_KEY_GENERATION
    /* Initialization should be performed only once */
    static bool initialized = false;
    if (!initialized) {
        if (mbedtls_pk_parse_keyfile(&kaa_pk_context_, KAA_PRIVATE_KEY_STORAGE, NULL)) {
            if (rsa_genkey(&kaa_pk_context_)) {
                mbedtls_pk_free(&kaa_pk_context_);
                return KAA_ERR_BADDATA;
            }
            if (write_rsa_key(&kaa_pk_context_, KAA_PRIVATE_KEY_STORAGE, PRIVATE_KEY)) {
                mbedtls_pk_free(&kaa_pk_context_);
                return KAA_ERR_BADDATA;
            }
            if (write_rsa_key(&kaa_pk_context_, KAA_PUBLIC_KEY_STORAGE, PUBLIC_KEY)) {
                mbedtls_pk_free(&kaa_pk_context_);
                return KAA_ERR_BADDATA;
            }
        }
        if (mbedtls_pk_parse_public_keyfile(&pk_pub_context, KAA_PUBLIC_KEY_STORAGE)) {
            pk_pub_context = kaa_pk_context_;
        }
        initialized = true;
    }
#else
    if (mbedtls_pk_parse_key(&kaa_pk_context_, KAA_RSA_PRIVATE_KEY,
            KAA_RSA_PRIVATE_KEY_LENGTH, NULL, 0)) {
        return KAA_ERR_BADDATA;
    }
#endif /* KAA_RUNTIME_KEY_GENERATION */
    return KAA_ERR_NONE;
}

void kaa_deinit_rsa_keypair(void)
{
    mbedtls_pk_free(&kaa_pk_context_);
}

#ifdef KAA_RUNTIME_KEY_GENERATION
/* TODO(KAA-1089) */
__attribute__((weak))
void ext_get_endpoint_public_key(const uint8_t **buffer, size_t *buffer_size)
{
    if (buffer == NULL || buffer_size == 0) {
        return;
    }
    static int key_length;
    static int initialized = false;
    static uint8_t buff[KAA_RSA_PUBLIC_KEY_LENGTH_MAX];
    if (!initialized) {
        key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, buff, sizeof(buff));
        if (key_length < 0) {
            *buffer = NULL;
            *buffer_size = 0;
            return;
        }
        initialized = true;
    }
    *buffer = buff;
    *buffer_size = key_length;
}

void ext_get_sha1_public(uint8_t **sha1, size_t *length)
{
    if (sha1 == NULL || length == NULL) {
        return;
    }
    static uint8_t sha1_public[KAA_SHA1_PUB_LEN];
    static int initialized = false;
    if (!initialized) {
        uint8_t pub_key[KAA_RSA_PUBLIC_KEY_LENGTH_MAX];
        int key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, pub_key, KAA_RSA_PUBLIC_KEY_LENGTH_MAX);
        if (key_length < 0) {
            *sha1 = NULL;
            *length = 0;
            return;
        }
        sha1_from_public_key(pub_key, key_length, sha1_public);
        initialized = true;
    }
    *length = KAA_SHA1_PUB_LEN;
    *sha1 = sha1_public;
}

kaa_error_t ext_get_sha1_base64_public(const uint8_t **sha1, size_t *length)
{
    if (sha1 == NULL || length == NULL) {
        return KAA_ERR_BADPARAM;
    }
    static size_t sha1_base64_len = 0;
    /* Additional byte for NULL terminator */
    static uint8_t sha1_base64_buffer[KAA_SHA1_PUB_BASE64_LEN + 1];
    static int initialized = false;
    if (!initialized) {
        uint8_t pub_key[KAA_RSA_PUBLIC_KEY_LENGTH_MAX];
        uint8_t sha1_public[KAA_SHA1_PUB_LEN];
        int key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, pub_key, KAA_RSA_PUBLIC_KEY_LENGTH_MAX);
        if (key_length < 0) {
            return KAA_ERR_BADDATA;
        }
        sha1_from_public_key(pub_key, key_length, sha1_public);
        int error = sha1_to_base64(sha1_public, sizeof(sha1_public), sha1_base64_buffer, sizeof(sha1_base64_buffer), &sha1_base64_len);
        if (error) {
            return KAA_ERR_BADDATA;
        }
        initialized = true;
    }
    *sha1 = sha1_base64_buffer;
    *length = sha1_base64_len;
    return KAA_ERR_NONE;
}
#else
/* TODO(KAA-1089) */
__attribute__((weak))
void ext_get_endpoint_public_key(const uint8_t **buffer, size_t *buffer_size)
{
    if (buffer == NULL || buffer_size == NULL) {
        return;
    }

    *buffer = KAA_RSA_PUBLIC_KEY;
    *buffer_size = KAA_RSA_PUBLIC_KEY_LENGTH;
}

void ext_get_sha1_public(uint8_t **sha1, size_t *length)
{
    if (sha1 == NULL || length == NULL) {
        return;
    }
    *sha1 = (uint8_t *)KAA_SHA1_PUB;
    *length = KAA_SHA1_PUB_LEN;
}

kaa_error_t ext_get_sha1_base64_public(const uint8_t **sha1, size_t *length)
{
    if (sha1 == NULL || length == NULL) {
        return KAA_ERR_BADPARAM;
    }
    *sha1 = (uint8_t *)KAA_SHA1_PUB_BASE64;
    *length = KAA_SHA1_PUB_BASE64_LEN;
    return KAA_ERR_NONE;
}
#endif /* KAA_RUNTIME_KEY_GENERATION */
