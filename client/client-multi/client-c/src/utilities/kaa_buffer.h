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


#ifndef KAA_BUFFER_H_
#define KAA_BUFFER_H_

#include "../kaa_error.h"

typedef struct kaa_buffer_t kaa_buffer_t;


kaa_error_t kaa_buffer_create_buffer(kaa_buffer_t **buffer_p, size_t buffer_size);


kaa_error_t kaa_buffer_destroy(kaa_buffer_t *buffer_p);


kaa_error_t kaa_buffer_allocate_space(kaa_buffer_t *buffer_p, char **buffer, size_t *free_size);


kaa_error_t kaa_buffer_lock_space(kaa_buffer_t *buffer_p, size_t lock_size);


kaa_error_t kaa_buffer_free_allocated_space(kaa_buffer_t *buffer_p, size_t size);


kaa_error_t kaa_buffer_get_unprocessed_space(kaa_buffer_t *buffer_p, char **buffer, size_t *available_size);


kaa_error_t kaa_buffer_reset(kaa_buffer_t *buffer_p);

#endif /* KAA_BUFFER_H_ */
