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

#include <kaa_rsa_key_gen.h>

/* Endpoint's RSA Keys */

endpoint_keys_t keys;
mbedtls_pk_context pk_context_;

int main(void)
{
    keys.public_key_length = KAA_RSA_PUBLIC_KEY_LENGTH_MAX;
    keys.private_key_length = KAA_RSA_PRIVATE_KEY_LENGTH_MAX;

    int error = rsa_keys_create(&pk_context_, keys.public_key, &keys.public_key_length,
                                keys.private_key, &keys.private_key_length);
    if (error) {
        printf("Error: can't generate keys (%i)\n", error);
    }

    error = kaa_keys_store(keys.public_key, keys.public_key_length, keys.private_key, keys.private_key_length);
    if (error) {
        printf("Error: Can't store the keys\n");
    }

    mbedtls_pk_free(&pk_context_);
    return error ? EXIT_FAILURE : EXIT_SUCCESS;
}
