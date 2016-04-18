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
@file econais_ec19d_file_utils.h
*/

#ifndef ECONAIS_EC19D_FILE_UTILS_H_
#define ECONAIS_EC19D_FILE_UTILS_H_

#include <stddef.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

int econais_ec19d_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation);


int econais_ec19d_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size);


int econais_ec19d_binary_file_delete(const char *file_name);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* ECONAIS_EC19D_FILE_UTILS_H_ */
