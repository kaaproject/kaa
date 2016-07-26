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

#include "kaa_aes_rsa.h"

int init_aes_key(unsigned char *key, size_t bytes)
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

kaa_error_t aes_encrypt_decrypt(int mode, const uint8_t *input, size_t input_size,
        uint8_t *output, const uint8_t *key)
{
    if ((input_size % 16) != 0 || !input || !input_size) {
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
        /* KAA_SESSION_KEY_LENGTH * 8 - size in bits */
        mbedtls_aes_setkey_enc(&aes_ctx, key, KAA_SESSION_KEY_LENGTH * 8);
        mbedtls_aes_crypt_ecb(&aes_ctx, MBEDTLS_AES_ENCRYPT, input, output);
    } else {
        /* KAA_SESSION_KEY_LENGTH * 8 - size in bits */
        mbedtls_aes_setkey_dec(&aes_ctx, key, KAA_SESSION_KEY_LENGTH * 8);
        mbedtls_aes_crypt_ecb(&aes_ctx, MBEDTLS_AES_DECRYPT, input, output);
    }

    return KAA_ERR_NONE;
}

int rsa_sign(mbedtls_pk_context *pk, const uint8_t *input,
        size_t input_size, uint8_t *output, size_t *output_size)
{
    // TODO(KAA-982): Use asserts
    if (!input || !input_size || !output || !output_size) {
        return KAA_ERR_BADPARAM;
    }
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

    return ret ? KAA_ERR_BADDATA : KAA_ERR_NONE;
}
