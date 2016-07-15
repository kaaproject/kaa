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
#include <utilities/kaa_mem.h>
#include <platform/ext_key_utils.h>
#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>

#include <kaa_rsa_key_gen.h>

#define KAA_SESSION_KEY_LENGTH         16
#define ENC_SESSION_KEY_LENGTH         256
#define KAA_SIGNATURE_LENGTH           256
#define AES_ECB_ENCRYPTION_CHUNCK_SIZE 16
#define KAA_RSA_PUBLIC_KEY_LENGTH_MAX  294
#define KAA_SHA1_PUB_LEN               20
#define KAA_SHA1_PUB_BASE64_LEN        28

#define KAA_PRIVATE_KEY_STORAGE "private.key"
#define KAA_PUBLIC_KEY_STORAGE "public.key"

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
} endpoint_key_utils_t;

/* Endpoint's RSA Keys */
static endpoint_key_utils_t keys;
static mbedtls_pk_context pk_context_;
static mbedtls_pk_context pk_pub_context;

/* TODO(KAA-1089) */
__attribute__((weak))
void ext_get_endpoint_public_key(uint8_t **buffer, size_t *buffer_size)
{
    if (!buffer || !buffer_size) {
        return;
    }
    static int key_length;
    static int initialization = false;
    static uint8_t buff[KAA_RSA_PUBLIC_KEY_LENGTH_MAX];
    if (!initialization)
    {
        key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, buff, sizeof(buff));
        if (key_length < 0) {
            return;
	}
        initialization = true;
    }
    *buffer = buff;
    *buffer_size = key_length;
}

static int rsa_encrypt(mbedtls_pk_context *pk, const uint8_t *input, size_t input_len, uint8_t *output)
{
    int ret = 0;

    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;
    const uint8_t pers[] = "key_gen";

    mbedtls_ctr_drbg_init(&ctr_drbg);
    mbedtls_entropy_init(&entropy);

    ret = mbedtls_ctr_drbg_seed(&ctr_drbg, mbedtls_entropy_func, &entropy,
            pers, sizeof(pers) - 1);
    if (!ret) {
        ret = mbedtls_rsa_rsaes_pkcs1_v15_encrypt(mbedtls_pk_rsa(*pk), mbedtls_ctr_drbg_random, &ctr_drbg,
                MBEDTLS_RSA_PUBLIC, input_len, input, output);
    }

    mbedtls_ctr_drbg_free(&ctr_drbg);
    mbedtls_entropy_free(&entropy);

    return ret;
}

static kaa_error_t set_rsa_public_key(mbedtls_pk_context *pk, const uint8_t *key, size_t key_size)
{
    if (!pk || !key || !key_size) {
        return KAA_ERR_BADPARAM;
    }

    if (mbedtls_pk_parse_public_key(pk, key, key_size)!= 0) {
        return KAA_ERR_INVALID_PUB_KEY;
    }

    return KAA_ERR_NONE;
}

static kaa_error_t aes_encrypt_decrypt(int mode, const uint8_t *input, size_t input_size,
        uint8_t *output, const uint8_t *key)
{
    if ((input_size % 16) != 0 || !input) {
        return KAA_ERR_BADPARAM;
    }

    if (mode != MBEDTLS_AES_ENCRYPT && mode != MBEDTLS_AES_DECRYPT) {
        return KAA_ERR_BADPARAM;
    }

    static bool initialized = false;
    static mbedtls_aes_context aes_ctx;

    if (!initialized) {
        mbedtls_aes_init(&aes_ctx);
        initialized = true;
    }

    if (mode == MBEDTLS_AES_ENCRYPT) {
        mbedtls_aes_setkey_enc(&aes_ctx, key, KAA_SESSION_KEY_LENGTH * 8);
        mbedtls_aes_crypt_ecb(&aes_ctx, MBEDTLS_AES_ENCRYPT, input, output);
    } else if (mode == MBEDTLS_AES_DECRYPT) {
        mbedtls_aes_setkey_dec(&aes_ctx, key, KAA_SESSION_KEY_LENGTH * 8);
        mbedtls_aes_crypt_ecb(&aes_ctx, MBEDTLS_AES_DECRYPT, input, output);
    }

    return KAA_ERR_NONE;
}

static int init_aes_key(unsigned char *key, size_t bytes)
{
    mbedtls_ctr_drbg_context ctr_drbg;
    mbedtls_entropy_context entropy;

    const uint8_t pers[] = "aes_generate_key";
    int ret;

    mbedtls_entropy_init(&entropy);
    mbedtls_ctr_drbg_init(&ctr_drbg);

    if ((ret = mbedtls_ctr_drbg_seed(&ctr_drbg, mbedtls_entropy_func, &entropy,
            pers, sizeof(pers) - 1)) == 0) {
        ret = mbedtls_ctr_drbg_random(&ctr_drbg, key, bytes);
    }

    mbedtls_ctr_drbg_free(&ctr_drbg);
    mbedtls_entropy_free(&entropy);

    return ret;
}

/* Performs initialization of the keys */
kaa_error_t kaa_init_keys(void)
{
    /* Initialization should be performed only once */
    static bool initialized = false;
    if (!initialized) {
        /* Generate session key */
        if (init_aes_key(keys.session_key, KAA_SESSION_KEY_LENGTH)) {
            return KAA_ERR_BADDATA;
        }
        keys.session_key_length = KAA_SESSION_KEY_LENGTH;
        if (mbedtls_pk_parse_keyfile(&pk_context_, KAA_PRIVATE_KEY_STORAGE, NULL)) {
#ifdef KAA_RUNTIME_KEY_GENERATION
            if (rsa_genkey(&pk_context_)) {
                return -1;
            }
#else
            return KAA_ERR_BADDATA;
#endif
        }
         if (mbedtls_pk_parse_public_keyfile(&pk_pub_context, KAA_PUBLIC_KEY_STORAGE)) {
#ifdef KAA_RUNTIME_KEY_GENERATION
		pk_pub_context = pk_context_;
#else
            return KAA_ERR_BADDATA;
#endif
        }
        initialized = true;
    }

    return KAA_ERR_NONE;
}

void  kaa_deinit_keys(void)
{
    mbedtls_pk_free(&pk_context_);
}

void ext_get_endpoint_session_key(uint8_t **buffer, size_t *buffer_size)
{
    if (!buffer || !buffer_size) {
        return;
    }
    *buffer = keys.session_key;
    *buffer_size = keys.session_key_length;
}

kaa_error_t ext_encrypt_data(const uint8_t *input, size_t payload_size, uint8_t *output)
{
    if (!input || !payload_size || !output) {
        return KAA_ERR_BADPARAM;
    }

    /* Adding PKCS7 padding */
    size_t enc_data_size = ext_get_encrypted_data_size(payload_size);
    uint8_t padding = enc_data_size - payload_size;
    memset(output + payload_size, padding, padding);

    /* Process the buffer chunk by chunk */
    while (enc_data_size >= KAA_SESSION_KEY_LENGTH) {
        if (aes_encrypt_decrypt(MBEDTLS_AES_ENCRYPT, input,
                KAA_SESSION_KEY_LENGTH,
                output, keys.session_key) < 0) {
            return KAA_ERR_BADDATA;
        }

        enc_data_size -= KAA_SESSION_KEY_LENGTH;

        input += KAA_SESSION_KEY_LENGTH;
        output += KAA_SESSION_KEY_LENGTH;
    }

    return KAA_ERR_NONE;
}

kaa_error_t ext_decrypt_data(const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_payload_size)
{
    if (!input || !input_size || !output || !output_payload_size) {
        return KAA_ERR_BADPARAM;
    }

    size_t tmp_in_size = input_size;
    while (input_size >= KAA_SESSION_KEY_LENGTH) {
        if (aes_encrypt_decrypt(MBEDTLS_AES_DECRYPT, input,
                KAA_SESSION_KEY_LENGTH, output,
                keys.session_key) < 0) {
            return KAA_ERR_BADDATA;
        }
        input_size -= KAA_SESSION_KEY_LENGTH;

        input += KAA_SESSION_KEY_LENGTH;
        output += KAA_SESSION_KEY_LENGTH;
    }

    /* Reduce PKCS7 padding */
    uint8_t padding_length = *(output - 1);
    *output_payload_size = tmp_in_size - padding_length;

    return KAA_ERR_NONE;
}

/* Get encrypted session key (enctypted with remote key) */
kaa_error_t ext_get_encrypted_session_key(uint8_t **buffer, size_t *buffer_size,
        const uint8_t *remote_key, size_t remote_key_size)
{
    if (!buffer || !buffer_size || !remote_key || !remote_key_size) {
        return KAA_ERR_BADPARAM;
    }

    mbedtls_pk_context local_ctx;
    mbedtls_pk_init(&local_ctx);
    int err = set_rsa_public_key(&local_ctx, remote_key, remote_key_size);
    if (err) {
        goto exit;
    }
    err = rsa_encrypt(&local_ctx, keys.session_key,
            KAA_SESSION_KEY_LENGTH, keys.encrypted_session_key);
    if (err) {
        goto exit;
    }

    keys.encrypted_session_key_length = ENC_SESSION_KEY_LENGTH;

    *buffer = keys.encrypted_session_key;
    *buffer_size = keys.encrypted_session_key_length;

exit:
    mbedtls_pk_free(&local_ctx);
    return err ? KAA_ERR_BADDATA : KAA_ERR_NONE;
}

size_t ext_get_encrypted_data_size(size_t input_size)
{
    if (!input_size) {
        return 0;
    }

    if (input_size % AES_ECB_ENCRYPTION_CHUNCK_SIZE != 0) {
        return AES_ECB_ENCRYPTION_CHUNCK_SIZE -
               (input_size % AES_ECB_ENCRYPTION_CHUNCK_SIZE) + input_size;
    }

    return input_size + AES_ECB_ENCRYPTION_CHUNCK_SIZE;
}

static int rsa_sign(mbedtls_pk_context *pk, const uint8_t *input,
        size_t input_size, uint8_t *output, size_t *output_size)
{
    int ret = 0;
    uint8_t hash[32];
    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;
    const uint8_t pers[] = "mbedtls_pk_sign";

    mbedtls_entropy_init(&entropy);
    mbedtls_ctr_drbg_init(&ctr_drbg);


    if ((ret = mbedtls_ctr_drbg_seed(&ctr_drbg, mbedtls_entropy_func, &entropy,
            pers,sizeof(pers) - 1)) != 0) {
        goto exit;
    }

    const mbedtls_md_info_t *info = mbedtls_md_info_from_type(MBEDTLS_MD_SHA1);

    if ((ret = mbedtls_md(info, input, input_size, hash)) != 0) {
        goto exit;
    }

    ret = mbedtls_pk_sign(pk, MBEDTLS_MD_SHA1, hash, 0, output, output_size,
            mbedtls_ctr_drbg_random, &ctr_drbg);

exit:
    mbedtls_ctr_drbg_free(&ctr_drbg);
    mbedtls_entropy_free(&entropy);

    return ret;
}


kaa_error_t ext_get_signature(const uint8_t *input, size_t input_size,
                              uint8_t **output, size_t *output_size)
{
    if (!input || !input_size || !output || !output_size) {
        return KAA_ERR_BADPARAM;
    }

    kaa_error_t error = rsa_sign(&pk_context_, input, input_size, keys.signature, output_size);

    if (error) {
        return KAA_ERR_BADDATA;
    }
    *output = keys.signature;
    *output_size = KAA_SIGNATURE_LENGTH;

    return KAA_ERR_NONE;
}

void ext_get_sha1_public(uint8_t **sha1, size_t *length)
{
    if (!sha1 || !length) {
        return;
    }
    uint8_t *sha1_public = KAA_MALLOC(KAA_SHA1_PUB_LEN);
    unsigned char pub_key[294];
    int key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, pub_key, KAA_RSA_PUBLIC_KEY_LENGTH_MAX);
    if (key_length < 0) {
        KAA_FREE(sha1_public);
        return;
    }
    sha1_from_public_key(pub_key, key_length, sha1_public);
    *length = KAA_SHA1_PUB_LEN;
    *sha1 = sha1_public;
}

void ext_get_sha1_base64_public(uint8_t **sha1, size_t *length)
{
    if (!sha1 || !length) {
        return;
    }
    size_t sha1_base64_len = 0;
    uint8_t *sha1_base64_buffer = KAA_MALLOC(1024);
    uint8_t sha1_public[SHA1_LENGTH];
    unsigned char pub_key[294];
    int key_length = mbedtls_pk_write_pubkey_der(&pk_pub_context, pub_key, KAA_RSA_PUBLIC_KEY_LENGTH_MAX);
    if (key_length < 0) {
        KAA_FREE(sha1_base64_buffer);
        return;
    }
    sha1_from_public_key(pub_key, key_length, sha1_public);
    int error = sha1_to_base64(sha1_public, sizeof(sha1_public), sha1_base64_buffer, sizeof(sha1_base64_buffer), &sha1_base64_len);
    if (error) {
        return;
    }
    *sha1 = (uint8_t *)sha1_base64_buffer;
    *length = KAA_SHA1_PUB_BASE64_LEN;

}
