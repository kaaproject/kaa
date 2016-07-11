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

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <sys/stat.h>
#include <string.h>
#include <platform/ext_key_utils.h>
#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>

#define KAA_SESSION_KEY_LENGTH         16
#define ENC_SESSION_KEY_LENGTH         256
#define KAA_SIGNATURE_LENGTH           256
#define AES_ECB_ENCRYPTION_CHUNCK_SIZE 16

/**
 * Contains Endpoint keys.
 *
 * The main purpose of the structure is
 * caching calculated keys.
 */

typedef struct {
    uint8_t session_key[KAA_SESSION_KEY_LENGTH];
    uint8_t encrypted_session_key[ENC_SESSION_KEY_LENGTH];
    uint8_t signature[KAA_SIGNATURE_LENGTH];
    size_t  encrypted_session_key_length;
    size_t  session_key_length;
} endpoint_keys_t;

/* Endpoint's RSA Keys */
static endpoint_keys_t keys;
static mbedtls_pk_context pk_context_;

/* TODO(KAA-1089) */
__attribute__((weak))
void ext_get_endpoint_public_key(uint8_t **buffer, size_t *buffer_size)
{
}

/* Performs initialization of the keys */
kaa_error_t kaa_init_keys(void)
{
}

void  kaa_deinit_keys(void)
{
}

void ext_get_endpoint_session_key(uint8_t **buffer, size_t *buffer_size)
{
}

kaa_error_t ext_encrypt_data(const uint8_t *input, size_t input_size, uint8_t *output)
{
}

kaa_error_t ext_decrypt_data(const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_payload_size)
{
}

/* Get encrypted session key (enctypted with remote key) */
kaa_error_t ext_get_encrypted_session_key(uint8_t **buffer, size_t *buffer_size,
        const uint8_t *remote_key, size_t remote_key_size)
{
}

size_t ext_get_encrypted_data_size(size_t input_size)
{
}

kaa_error_t ext_get_signature(const uint8_t *input, size_t input_size,
                              uint8_t **output, size_t *output_size)
{
}

void ext_get_sha1_public(uint8_t **sha1, size_t *length)
{
}

void ext_get_sha1_base64_public(uint8_t **sha1, size_t *length)
{
}
