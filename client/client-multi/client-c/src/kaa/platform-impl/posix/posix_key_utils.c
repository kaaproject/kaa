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
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <sys/stat.h>
#include "../../platform/ext_key_utils.h"
#include "../../utilities/kaa_mem.h"
#include "../../kaa_common.h"
#include "posix_file_utils.h"


#define KAA_KEY_STORAGE       "kaa_key.pub"

static char *kaa_public_key = NULL;
static size_t kaa_public_key_length = 0;

static void kaa_generate_pub_key(void)
{
    const int kBits = 2048;
    const int kExp = 65537;

    RSA *rsa = RSA_generate_key(kBits, kExp, 0, 0);

    BIO *bio_pem = BIO_new(BIO_s_mem());
    i2d_RSA_PUBKEY_bio(bio_pem, rsa);

    kaa_public_key_length = BIO_pending(bio_pem);
    kaa_public_key = (char *) KAA_MALLOC(kaa_public_key_length);
    if (!kaa_public_key) {
        kaa_public_key_length = 0;
        BIO_free_all(bio_pem);
        RSA_free(rsa);
        return;
    }
    BIO_read(bio_pem, kaa_public_key, kaa_public_key_length);

    BIO_free_all(bio_pem);
    RSA_free(rsa);
}

static int kaa_init_key(void)
{
    struct stat stat_result;
    int key_result = stat(KAA_KEY_STORAGE, &stat_result);

    if (!key_result) {
        bool need_dealloc = false;
        posix_binary_file_read(KAA_KEY_STORAGE, &kaa_public_key, &kaa_public_key_length, &need_dealloc);
    } else {
        kaa_generate_pub_key();
        posix_binary_file_store(KAA_KEY_STORAGE, kaa_public_key, kaa_public_key_length);
    }

    return 0;
}

void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL3(buffer, buffer_size, needs_deallocation,);
    if (!kaa_public_key)
        kaa_init_key();
    *buffer = kaa_public_key;
    *buffer_size = kaa_public_key_length;
    *needs_deallocation = false;
}
