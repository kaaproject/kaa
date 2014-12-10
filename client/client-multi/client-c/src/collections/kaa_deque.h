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

#ifndef KAA_COLLECTIONS_KAA_DEQUE_H_
#define KAA_COLLECTIONS_KAA_DEQUE_H_

#include <stdbool.h>
#include <stddef.h>
#include <sys/types.h>

#include "kaa_error.h"

typedef struct kaa_deque_iterator_t kaa_deque_iterator_t;
typedef struct kaa_deque_t          kaa_deque_t;

typedef void (*kaa_deque_data_destroy_fn)(void *);

void                   *kaa_deque_iterator_get_data(kaa_deque_iterator_t *);
kaa_deque_iterator_t   *kaa_deque_iterator_next(kaa_deque_iterator_t *);
kaa_deque_iterator_t   *kaa_deque_iterator_previous(kaa_deque_iterator_t *);
void                    kaa_deque_iterator_destroy(kaa_deque_iterator_t *it, kaa_deque_data_destroy_fn fn);

kaa_error_t             kaa_deque_create(kaa_deque_t **);
void                    kaa_deque_destroy(kaa_deque_t *, kaa_deque_data_destroy_fn);

kaa_error_t             kaa_deque_first(kaa_deque_t *,kaa_deque_iterator_t **);
kaa_error_t             kaa_deque_last(kaa_deque_t *,kaa_deque_iterator_t **);

kaa_error_t             kaa_deque_pop_front(kaa_deque_t *, kaa_deque_iterator_t **);
kaa_error_t             kaa_deque_pop_back(kaa_deque_t *, kaa_deque_iterator_t **);

kaa_error_t             kaa_deque_push_front_data(kaa_deque_t *, void *);
kaa_error_t             kaa_deque_push_front_iterator(kaa_deque_t *, kaa_deque_iterator_t *);
kaa_error_t             kaa_deque_push_back_data(kaa_deque_t *, void *);
kaa_error_t             kaa_deque_push_back_iterator(kaa_deque_t *, kaa_deque_iterator_t *);

ssize_t                 kaa_deque_size(kaa_deque_t *);

kaa_deque_t            *kaa_deque_merge_move(kaa_deque_t *, kaa_deque_t *);

#endif /* KAA_COLLECTIONS_KAA_DEQUE_H_ */
