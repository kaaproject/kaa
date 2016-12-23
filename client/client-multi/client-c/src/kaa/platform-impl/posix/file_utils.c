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
#include <string.h>
#include <platform/kaa_client_properties.h>
#include "utilities/kaa_mem.h"
#include "kaa_common.h"

static FILE *binary_file_open(const char *filename, const char *mode)
{
    const char *working_directory = kaa_client_props_get()->working_directory;
    size_t path_size = strlen(working_directory) + strlen(filename) + 2; // 2 bytes for directory separator and null-terminator
    char full_file_path[path_size];

    int error_code = snprintf(full_file_path, path_size, "%s/%s", working_directory, filename);
    if (error_code < 0 || error_code >= (int)path_size) {
        return NULL;
    }

    return fopen(full_file_path, mode);
}

int posix_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    if (file_name == NULL || buffer == NULL || buffer_size == NULL || needs_deallocation == NULL) {
        return -1;
    }

    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    FILE *file = binary_file_open(file_name, "rb");
    if (file == NULL) {
        return -1;
    }
    fseek(file, 0, SEEK_END);
    long result_size = ftell(file);
    if (result_size <= 0) {
        fclose(file);
        return -1;
    }
    char *result_buffer = KAA_MALLOC(result_size * sizeof(char));
    if (result_buffer == NULL) {
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
    if (file_name == NULL || buffer == NULL || buffer_size == 0) {
        return -1;
    }

    FILE *status_file = binary_file_open(file_name, "wb");
    if (status_file != NULL) {
        fwrite(buffer, buffer_size, 1, status_file);
        fclose(status_file);
        return 0;
    }
    return -1;
}

int posix_binary_file_delete(const char *file_name)
{
    if (file_name == NULL) {
        return -1;
    }
    const char* working_directory = kaa_client_props_get()->working_directory;
    size_t kaa_path_size = strlen(working_directory) +
        strlen(file_name) + 2; // 2 bytes for directory separator and null-terminator
    char kaa_binary_file_path[kaa_path_size];

    int error_code = snprintf(kaa_binary_file_path, kaa_path_size, "%s/%s",
        working_directory, file_name);
    if (error_code < 0 || error_code >= (int)kaa_path_size) {
        return KAA_ERR_BADPARAM;
    }

    FILE *file = fopen(kaa_binary_file_path, "r");
    if (file != NULL) {
        fclose(file);
        return remove(kaa_binary_file_path);
    }
    return -1;
}
