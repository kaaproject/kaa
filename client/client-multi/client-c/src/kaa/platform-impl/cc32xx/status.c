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
#include <platform/ext_status.h>
#include <platform/file_utils.h>

#define KAA_STATUS_STORAGE    "kaa_status.bin"

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    cc32xx_binary_file_read(KAA_STATUS_STORAGE, buffer, buffer_size, needs_deallocation);
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
    cc32xx_binary_file_store(KAA_STATUS_STORAGE, buffer, buffer_size);
}

void ext_status_delete(void)
{
    cc32xx_binary_file_delete(KAA_STATUS_STORAGE);
}


