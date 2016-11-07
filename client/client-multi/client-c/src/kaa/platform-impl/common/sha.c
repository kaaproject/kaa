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

#include <stdint.h>
#include <stddef.h>
#include <mbedtls/sha1.h>
#include <string.h>

#include "kaa_common.h"
#include "platform/ext_sha.h"

kaa_error_t ext_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest)
{
    if ((digest == NULL) || ((data == NULL) && (data_size != 0))) {
        return KAA_ERR_BADPARAM;
    }

    mbedtls_sha1_context sha1_ctx;
    mbedtls_sha1_init(&sha1_ctx);
    mbedtls_sha1_starts(&sha1_ctx);
    mbedtls_sha1_update(&sha1_ctx, (unsigned char*)data, (int)data_size);
    mbedtls_sha1_finish(&sha1_ctx, (unsigned char *)digest);
    mbedtls_sha1_free(&sha1_ctx);

    return KAA_ERR_NONE;
}

kaa_error_t ext_copy_sha_hash(kaa_digest_p dst, const kaa_digest_p src)
{
    if (!dst || !src) {
        return KAA_ERR_BADPARAM;
    }

    memcpy(dst, src, SHA_1_DIGEST_LENGTH);
    return KAA_ERR_NONE;
}
