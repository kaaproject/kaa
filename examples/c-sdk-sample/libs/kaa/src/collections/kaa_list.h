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

#ifndef KAA_LIST_H_
#define KAA_LIST_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "../kaa_common.h"
#include <stdbool.h>
#include <sys/types.h> // For ssize_t

typedef struct kaa_list_t kaa_list_t;

/**
 * Return 0 if data doesn't match search criteria.
 */
typedef bool (*match_predicate)(void *data, void *context);

/**
 * Use to deallocate list node data.
 */
typedef void (*deallocate_list_data)(void *);



/**
 * Adds new element to the end of the list.
 */
kaa_list_t *kaa_list_push_back(kaa_list_t *head, void *data);

/**
 * Adds new element to the begin of the list, returns new list head.
 */
kaa_list_t *kaa_list_push_front(kaa_list_t *head, void *data);

/**
 * Returns data on current list position.
 */
void *kaa_list_get_data(kaa_list_t *position);

/**
 * Returns size of the list.
 */
ssize_t kaa_list_get_size(kaa_list_t *position);

/**
 * Checks if there is an element after current position.
 */
bool kaa_list_has_next(kaa_list_t *position);

/**
 * Returns next element.
 */
kaa_list_t *kaa_list_next(kaa_list_t *position);

/**
 * Adds all elements of list2 to the end of list1.
 * Returns iterator to the beginning of the inserted elements
 */
kaa_list_t *kaa_lists_merge(kaa_list_t *list1, kaa_list_t *list2);

/**
 * Creates list with 1 element having given data.
 * Returns iterator to the head of created list.
 */
kaa_list_t *kaa_list_create(void *data);

/**
 * Frees data occupied by list, deallocates data from the list using given deallocator.
 */
void kaa_list_destroy(kaa_list_t *head, deallocate_list_data deallocator);

/**
 * Frees data occupied by list, data will not be deallocated.
 */
void kaa_list_destroy_no_data_cleanup(void *head);

/**
 * Removes element from list at given position. Position must be valid iterator
 * to the element in the given list. Deallocates released data using given deallocator.
 * Returns iterator pointing to the position before removed element, pointer
 * to the head of the list if (*head == position) or NULL if the position was not found.
 */
kaa_list_t *kaa_list_remove_at(kaa_list_t **head, kaa_list_t *position, deallocate_list_data deallocator);

/**
 * Removes first element that is matched by predicate.
 * Returns KAA_ERR_NONE if element was found.
 */
kaa_error_t kaa_list_remove_first(kaa_list_t **head, match_predicate pred, void *context, deallocate_list_data deallocator);

/**
 * Inserts data into the given position. Deallocates memory occupied by previous
 * data using given deallocator.
 */
void kaa_list_set_data_at(kaa_list_t *position, void *data, deallocate_list_data deallocator);

/**
 * Insert data after a given iterator.
 * Returns iterator to an inserted item in list.
 */
kaa_list_t *kaa_list_insert_after(kaa_list_t *position, void *data);

/**
 * Returns first element in list from given position where ((*pred)(data, context) != 0).
 * If nothing matched given criteria or list is empty NULL is returned.
 */
kaa_list_t *kaa_list_find_next(kaa_list_t *from, match_predicate pred, void *context);

/**
 * Returns last element in list from given position where ((*pred)(data, context) != 0).
 * If nothing matched given criteria or list is empty NULL is returned.
 */
kaa_list_t *kaa_list_find_last(kaa_list_t *from, match_predicate pred, void *context);

kaa_list_t *kaa_list_split_after(kaa_list_t *head, kaa_list_t *after, kaa_list_t **tail);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_LIST_H_ */
