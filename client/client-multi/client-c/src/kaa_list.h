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
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_common.h"

struct kaa_list_t {
    void *              data;
    struct kaa_list_t * next;
};
typedef struct kaa_list_t kaa_list_t;


typedef void (* deallocate_list_data)(void *);

/**
 * Adds new element to the end of the list.
 *
 * \retval KAA_ERR_NONE Element was added successfuly
 * \retval KAA_ERR_NOMEM Not enough memory
 */
kaa_error_t kaa_list_push_back(kaa_list_t **head, void *data);

/**
 * Adds new element to the begin of the list.
 *
 * \retval KAA_ERR_NONE Element was added successfuly
 * \retval KAA_ERR_NOMEM Not enough memory
 */
kaa_error_t kaa_list_push_front(kaa_list_t **head, void *data);

/**
 * Returns data on current list position.
 */
static inline void *kaa_list_get_data(kaa_list_t * position)
{
    return (position ? position->data : NULL);
}

/**
 * Returns size of the list.
 */
size_t kaa_list_get_size(kaa_list_t * position);

/**
 * Checks if there is an element after current position.
 */
static inline KAA_BOOL kaa_list_has_next(kaa_list_t * position)
{
    return (position && (NULL != position->next));
}

/**
 * Returns next element.
 */
static inline kaa_list_t *kaa_list_next(kaa_list_t * position)
{
    return (position ? position->next : NULL);
}

/**
 * Adds all elements of list2 to the end of list1.
 * Returns iterator to the beginning of the inserted elements
 */
kaa_list_t *kaa_lists_merge(kaa_list_t * list1, kaa_list_t *list2);

/**
 * Creates list with 1 element having given data.
 *
 * \return iterator to the head of created list.
 * \retval NULL Not enough memory
 */
kaa_list_t *kaa_list_create(void *data);

/**
 * Frees data occupied by list, deallocates data from the list using given deallocator.
 */
void kaa_list_destroy(kaa_list_t *head, deallocate_list_data deallocator);

/**
 * Removes element from list at given position. Position must be valid iterator
 * to the element in the given list. Deallocates released data using given deallocator.
 * Returns iterator pointing to the position before removed element or pointer
 * to the head of the list if (*head == position)
 */
kaa_list_t *kaa_list_remove_at(kaa_list_t **head, kaa_list_t *position, deallocate_list_data deallocator);

/**
 * Inserts data into the given position. Deallocates memory occupied by previous
 * data using given deallocator.
 */
void kaa_list_set_data_at(kaa_list_t * position, void * data, deallocate_list_data deallocator);

/**
 * Insert data after a given iterator.
 *
 * \return iterator to an inserted item in list.
 * \retval NULL Not enough memory
 */
kaa_list_t * kaa_list_insert_after(kaa_list_t * position, void * data);

/**
 * Return 0 if data doesn't match search criteria
 */
typedef int (* match_predicate)(void *data);
/**
 * Returns first element in list from given position where ((*pred)(data) != 0).
 * If nothing matched given criteria or list is empty NULL is returned.
 */
kaa_list_t * kaa_list_find_next(kaa_list_t * from, match_predicate pred);

/**
 * Returns last element in list from given position where ((*pred)(data) != 0).
 * If nothing matched given criteria or list is empty NULL is returned.
 */
kaa_list_t * kaa_list_find_last_occurance(kaa_list_t * from, match_predicate pred);

CLOSE_EXTERN
#endif /* KAA_LIST_H_ */
