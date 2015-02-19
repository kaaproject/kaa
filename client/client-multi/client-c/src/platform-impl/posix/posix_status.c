/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#include "../../platform/ext_status.h"
#include "../../platform/stdio.h"
#include "../../utilities/kaa_mem.h"

#define KAA_STATUS_STORAGE    "status.conf"

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "rb");

    if (!status_file) {
        return;
    }

    fseek(status_file, 0, SEEK_END);
    *buffer_size = ftell(status_file);
    *buffer = (char*) KAA_CALLOC(*buffer_size, sizeof(char));

    if (*buffer == NULL) {
        return;
    }

    *needs_deallocation = true;
    fseek(status_file, 0, SEEK_SET);
    if (fread(*buffer, *buffer_size, 1, status_file) == 0) {
        free(*buffer);
        return;
    }

    fclose(status_file);
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
    if (!buffer || !buffer_size)
        return;

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "wb");
    if (status_file) {
        fwrite(buffer, buffer_size, 1, status_file);
        fclose(status_file);
    }
}
