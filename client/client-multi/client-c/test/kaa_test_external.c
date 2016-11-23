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

#include <string.h>
#include <stdio.h>
#include <stdint.h>
#include <stdbool.h>

#include "platform/ext_status.h"
#include "platform/ext_sha.h"
#include "platform/ext_key_utils.h"
#include "utilities/kaa_mem.h"
#include "kaa_common.h"

static const uint8_t test_ep_key[] = {0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14};

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    (void)buffer;
    (void)buffer_size;
    (void)needs_deallocation;
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
    (void)buffer;
    (void)buffer_size;
}

void ext_get_endpoint_public_key(const uint8_t **buffer, size_t *buffer_size)
{
    *buffer = test_ep_key;
    *buffer_size = sizeof(test_ep_key);
}

void ext_configuration_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    (void)buffer;
    (void)buffer_size;
    (void)needs_deallocation;
}

void ext_configuration_store(const char *buffer, size_t buffer_size)
{
    (void)buffer;
    (void)buffer_size;
}
