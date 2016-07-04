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

/*
@file econais_ec19d_file_utils.c
*/
#include <sndc_sdk_api.h>
#include <sndc_file_api.h>
#include "econais_ec19d_file_utils.h"
#include "kaa_common.h"

int econais_ec19d_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL4(file_name, buffer, buffer_size, needs_deallocation, -1);

    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = false;

    sndc_file_ref_t status_file = sndc_file_open(file_name, DE_FRDONLY);
    KAA_RETURN_IF_NIL(status_file, -1);

    sndc_file_seek(status_file, 0, SEEK_END);
    *buffer_size = sndc_file_tell(status_file);
    *buffer = (char*)sndc_mem_calloc(*buffer_size, sizeof(char));
    KAA_RETURN_IF_NIL(*buffer, -1);


    sndc_file_seek(status_file, 0, SEEK_SET);
    if (sndc_file_read(status_file, *buffer, *buffer_size) == 0) {
        sndc_mem_free(*buffer);
        return -1;
    }
    *needs_deallocation = true;
    sndc_file_close(status_file);
    return 0;
}

int econais_ec19d_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(file_name, buffer, buffer_size, -1);
    sndc_file_ref_t status_file = sndc_file_open(file_name, DE_FCREATE|DE_FWRONLY|DE_FTRUNC);

    if (status_file) {
        int i = sndc_file_write(status_file, (void*)buffer, buffer_size);
        sndc_file_close(status_file);
        return 0 ? (i >= 0) : -1;
    }
    return -1;
}

int econais_ec19d_binary_file_delete(const char *file_name)
{
    return sndc_file_delete(file_name);
}
