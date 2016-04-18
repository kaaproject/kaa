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
#include "../../platform/ext_key_utils.h"
#include "../../utilities/kaa_mem.h"
#include "../../kaa_common.h"
#include "cc32xx_file_utils.h"
#include "cc32xx_rsa_key.h"

#define KAA_KEY_STORAGE       "kaa_key.pub"

static char *kaa_public_key = (char*)KAA_PUBLIC_KEY_DATA;
static size_t kaa_public_key_length = KAA_PUBLIC_KEY_LENGTH;

void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL3(buffer, buffer_size, needs_deallocation,);
    *buffer = kaa_public_key;
    *buffer_size = kaa_public_key_length;
    *needs_deallocation = false;
}
