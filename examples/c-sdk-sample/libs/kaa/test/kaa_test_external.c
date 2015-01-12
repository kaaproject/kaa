/*
 * Copyright 2014 CyberVision, Inc.
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

#include "kaa_external.h"

#include <string.h>
#include "utilities/kaa_mem.h"

static const char test_ep_key[20] = {0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14};


void    kaa_read_status_ext(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{

}

void    kaa_store_status_ext(const char *buffer, size_t buffer_size)
{

}

void    kaa_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = (char *) KAA_MALLOC(20 * sizeof(char));
    if (*buffer != NULL) {
        memcpy(*buffer, test_ep_key, 20);
        *buffer_size = 20;
        *needs_deallocation = true;
    } else {
        *buffer_size = 0;
        *needs_deallocation = false;
    }
}

