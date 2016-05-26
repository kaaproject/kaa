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

#ifndef KAA_LIST_H_
#define KAA_LIST_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>
#include <stddef.h>

#include "kaa_error.h"

typedef struct kaa_list_node_t kaa_list_node_t;
typedef struct kaa_list_t kaa_list_t;

/**
 * @brief Return @b false if data doesn't match search criteria.
 */
typedef bool (*match_predicate)(void *data, void *context);

/**
 * @brief Use to deallocate list node data.
 */
typedef void (*deallocate_list_data)(void *);

/**
 *  @brief Returns list node hash.
 */
typedef uint64_t list_node_hash(void * item);

/**
 * @brief Use to process element data.
 */
typedef void (*process_data)(void *data, void *context);

/**
 * @brief Creates empty list.
 * @return The list object.
 */
kaa_list_t *kaa_list_create(void);

/**
 * @brief Destroys list and all elements.
 */
void kaa_list_destroy(kaa_list_t *list, deallocate_list_data deallocator);

/**
 * @brief Removes all elements from the list (which are destroyed), and leaving the list with a size of 0.
 */
void kaa_list_clear(kaa_list_t *list, deallocate_list_data deallocator);

/**
 * @brief Returns the number of elements in the list.
 * @return The number of elements.
 * @retval NULL the list is @c NULL
 */
size_t kaa_list_get_size(kaa_list_t *list);

/**
 * @brief Inserts a new element at the beginning of the list, right before its current first element.
 * @return An iterator to the inserted element.
 * @retval NULL the list or data are @c NULL
 */
kaa_list_node_t *kaa_list_push_front(kaa_list_t *list, void *data);

/**
 * @brief Inserts a new element at the end of the list, after its current last element.
 * @return An iterator to the inserted element
 * @retval NULL the list or data are @c NULL
 */
kaa_list_node_t *kaa_list_push_back(kaa_list_t *list, void *data);

/**
 * @brief Returns an iterator pointing to the first element in the list.
 * @return An iterator
 * @retval NULL the list is @c NULL
 */
kaa_list_node_t *kaa_list_begin(kaa_list_t *list);

/**
 * @brief Returns an iterator pointing to the last element in the list.
 * @return An iterator
 * @retval NULL the list is @c NULL
 */
kaa_list_node_t *kaa_list_back(kaa_list_t *list);

/**
 * @brief Gets iterator to the next element.
 * @return An iterator
 * @retval NULL the provided iterator is @c NULL
 */
kaa_list_node_t *kaa_list_next(kaa_list_node_t *it);

/**
 * @brief Gets iterator to the previous element.
 * @return An iterator
 * @retval NULL the provided iterator is @c NULL
 */
kaa_list_node_t *kaa_list_prev(kaa_list_node_t *it);

/**
 * @brief Gets data from the iterator.
 * @return Data
 * @retval NULL the iterator is @c NULL
 */
void *kaa_list_get_data(kaa_list_node_t *it);

/**
 * @brief Sets new data to the element. Old data will be destroyed.
 */
void kaa_list_set_data_at(kaa_list_node_t *it, void *data, deallocate_list_data deallocator);

/**
 * @brief Returns an iterator to the first element in the list that matches by the predicate.
 * @retval NULL no such element is found
 */
kaa_list_node_t *kaa_list_find_next(kaa_list_node_t *from, match_predicate pred, void *context);

/**
 * @brief Merges the source list into the destination list by transferring all of its elements at their respective
 * ordered positions at the end of the source list.
 * @return The result list which contains all merged elements.
 */
kaa_list_t *kaa_lists_merge(kaa_list_t *destination, kaa_list_t *source);

/**
 * @brief Removes from the list a single element.
 * @return An iterator pointing to the element that followed the last element erased by the function call or NULL.
 */
kaa_list_node_t *kaa_list_remove_at(kaa_list_t *list, kaa_list_node_t *it, deallocate_list_data deallocator);

/**
 * @brief Removes from the list the first element for which the predicate returns true.
 * @retval KAA_ERR_NONE element was found
 */
kaa_error_t kaa_list_remove_first(kaa_list_t *list, match_predicate pred, void *context, deallocate_list_data deallocator);

/**
 * @brief Applies the function process to each of the elements in the range [first,last].
 */
void kaa_list_for_each(kaa_list_node_t *first, kaa_list_node_t *last, process_data process, void *context);

/**
 * @brief Sorts list according to predicate condition.
 * @param list  List to sort.
 * @param pred  Predicate that is used to sort list.
 */
void kaa_list_sort(kaa_list_t *list, match_predicate pred);

/**
 * @brief Estimate hash from sorted array.
 * @param list  List to calculate hash from.
 * @param pred  Predicate that is used to get list node's hash, id or other unique @c uint64_t value.
 */
int32_t kaa_list_hash(kaa_list_t *list, list_node_hash pred);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_LIST_H_ */
