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

#include <platform/file_utils.h>
#include <stdint.h>
#include <platform/stdio.h>
#include "utilities/kaa_mem.h"
#include "kaa_common.h"


int posix_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL4(file_name, buffer, buffer_size, needs_deallocation, -1);
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    FILE* file = fopen(file_name, "rb");
    KAA_RETURN_IF_NIL(file, -1);

    fseek(file, 0, SEEK_END);
    long result_size = ftell(file);
    if (result_size <= 0) {
        fclose(file);
        return -1;
    }
    char *result_buffer = KAA_MALLOC(result_size * sizeof(char));
    if (!result_buffer) {
        fclose(file);
        return -1;
    }

    fseek(file, 0, SEEK_SET);
    if (fread(result_buffer, result_size, 1, file) == 0) {
        KAA_FREE(result_buffer);
        fclose(file);
        return -1;
    }

    *buffer = result_buffer;
    *buffer_size = result_size;

    fclose(file);
    return 0;
}

int posix_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(file_name, buffer, buffer_size, -1);

    FILE* status_file = fopen(file_name, "wb");
    if (status_file) {
        fwrite(buffer, buffer_size, 1, status_file);
        fclose(status_file);
        return 0;
    }
    return -1;
}

int posix_binary_file_delete(const char *file_name)
{
    FILE *file = fopen(file_name, "r");
    if (file) {
        fclose(file);
        return remove(file_name);
    }
    return -1;
}
