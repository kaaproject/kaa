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

#include "platform/ext_encryption_utils.h"
#include "platform/ext_key_utils.h"

#include <stdbool.h>
#include <string.h>

#include <utilities/kaa_aes_rsa.h>

#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>

#define ENCRYPTED_SESSION_KEY_LENGTH   256
#define KAA_SIGNATURE_LENGTH           256
#define AES_ECB_ENCRYPTION_CHUNK_SIZE  16

extern mbedtls_pk_context kaa_pk_context_;

/**
 * Contains Endpoint keys.
 *
 * The main purpose of the structure is
 * caching calculated keys.
 */
static struct {
    uint8_t session_key[KAA_SESSION_KEY_LENGTH];
    uint8_t encrypted_session_key[ENCRYPTED_SESSION_KEY_LENGTH];
    uint8_t signature[KAA_SIGNATURE_LENGTH];
    size_t  session_key_length;
} keys;

kaa_error_t kaa_init_session_key(void)
{
    /* Initialization should be performed only once */
    static bool initialized = false;
    if (!initialized) {
        /* Generate session key */
        if (init_aes_key(keys.session_key, KAA_SESSION_KEY_LENGTH)) {
            return KAA_ERR_BADDATA;
        }

        keys.session_key_length = KAA_SESSION_KEY_LENGTH;
        initialized = true;
    }

    return KAA_ERR_NONE;
}

void ext_get_endpoint_session_key(const uint8_t **buffer, size_t *buffer_size)
{
    if (buffer == NULL || buffer_size == 0) {
        return;
    }
    *buffer = keys.session_key;
    *buffer_size = keys.session_key_length;
}

/* Get encrypted session key (enctypted with remote key) */
kaa_error_t ext_get_encrypted_session_key(uint8_t **buffer, size_t *buffer_size,
        const uint8_t *remote_key, size_t remote_key_size)
{
    if (buffer == NULL || buffer_size == NULL || remote_key == NULL || remote_key_size == 0) {
        return KAA_ERR_BADPARAM;
    }

    kaa_error_t err = rsa_encrypt(remote_key, remote_key_size, keys.session_key,
            KAA_SESSION_KEY_LENGTH, keys.encrypted_session_key);
    if (err) {
        return err;
    }

    *buffer = keys.encrypted_session_key;
    *buffer_size = ENCRYPTED_SESSION_KEY_LENGTH;

    return err;
}
size_t ext_get_encrypted_data_size(size_t input_size)
{
    if (input_size == 0) {
        return 0;
    }

    return input_size + AES_ECB_ENCRYPTION_CHUNK_SIZE -
        (input_size % AES_ECB_ENCRYPTION_CHUNK_SIZE);
}

kaa_error_t ext_encrypt_data(const uint8_t *input, size_t payload_size, uint8_t *output)
{
    if (input == NULL || payload_size == 0 || output == NULL) {
        return KAA_ERR_BADPARAM;
    }

    /* Adding PKCS7 padding */
    size_t enc_data_size = ext_get_encrypted_data_size(payload_size);
    uint8_t padding = enc_data_size - payload_size;
    memset(output + payload_size, padding, padding);

    return aes_encrypt_decrypt(MBEDTLS_AES_ENCRYPT, input, enc_data_size,
            output, keys.session_key);
}

kaa_error_t ext_decrypt_data(const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_size)
{
    if (input == NULL || input_size == 0 || output == NULL || output_size == 0) {
        return KAA_ERR_BADPARAM;
    }

    if (aes_encrypt_decrypt(MBEDTLS_AES_DECRYPT, input, input_size,
                output, keys.session_key)) {
        return KAA_ERR_BADPARAM;
    }

    /* Reduce PKCS7 padding */
    uint8_t padding_length = *(output+input_size - 1);
    *output_size = input_size - padding_length;

    return KAA_ERR_NONE;
}

kaa_error_t ext_get_signature(const uint8_t *input, size_t input_size,
        uint8_t **output, size_t *output_size)
{
    if (!input || !input_size || !output || !output_size) {
        return KAA_ERR_BADPARAM;
    }

    kaa_error_t error = rsa_sign(&kaa_pk_context_, input, input_size, keys.signature, output_size);

    if (error) {
        return KAA_ERR_BADDATA;
    }
    *output = keys.signature;
    *output_size = KAA_SIGNATURE_LENGTH;

    return KAA_ERR_NONE;
}

