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

#include <sys/types.h>
#include "../kaa_error.h"

/**
 * @brief Iterator to access the data stored in kaa_deque_t.
 */
typedef struct kaa_deque_iterator_t kaa_deque_iterator_t;

/**
 * @brief Struct representing a deque object.
 */
typedef struct kaa_deque_t          kaa_deque_t;

/**
 * @brief Function signature for functions used to cleanup data stored in a deque.
 */
typedef void (*kaa_deque_data_destroy_fn)(void *);

/**
 * @brief Extracts data pointed by an iterator.
 *
 * @param[in]   it  Valid iterator to the element in a deque.
 *
 * @return Pointer to a raw data.
 */
void                   *kaa_deque_iterator_get_data(kaa_deque_iterator_t *it);

/**
 * @brief Accesses iterator placed after the given one.
 *
 * @param[in]   it  Valid iterator to the element in a deque.
 *
 * @return Next iterator or NULL if given iterator was last or NULL.
 */
kaa_deque_iterator_t   *kaa_deque_iterator_next(kaa_deque_iterator_t *it);

/**
 * @brief Accesses iterator placed before the given one.
 *
 * @param[in]   it  Valid iterator to the element in a deque.
 *
 * @return Previous iterator or NULL if given iterator was first or NULL.
 */

kaa_deque_iterator_t   *kaa_deque_iterator_previous(kaa_deque_iterator_t *);

/**
 * @brief Releases memory occupied by an iterator.
 *
 * @param[in]   it                  Iterator to be destroyed.
 * @param[in]   data_destroy_fn     Pointer to the function which will be used to
 *                                  destroy the data pointed by an iterator. If NULL -
 *                                  no data destruction will be performed.
 */
void                    kaa_deque_iterator_destroy(kaa_deque_iterator_t *it, kaa_deque_data_destroy_fn data_destroy_fn);

/**
 * @brief Create new @link kaa_deque_t @endlink object.
 *
 * @param[in,out]   self    Pointer to the memory where kaa_deque_t object will be created.
 *
 * @return  KAA_ERR_BADPARAM if @code self == NULL @endcode,
 *          KAA_ERR_NOMEM if memory allocation failed,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_create(kaa_deque_t **self);

/**
 * @brief Releases memory occupied by a kaa_deque_t object (including memory occupied by iterators).
 *
 * @param[in,out]   self                Pointer to the kaa_deque_t object.
 * @param[in]       data_destroy_fn     Pointer to the function which will be used to
 *                                      destroy the data pointed by an iterator. If NULL -
 *                                      no data destruction will be performed.
 */
void                    kaa_deque_destroy(kaa_deque_t *self, kaa_deque_data_destroy_fn data_destroy_fn);

/**
 * @brief Fetch iterator to the first element in a deque.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in,out]   it                  Pointer to an iterator which will contain iterator to the first element.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_first(kaa_deque_t *self, kaa_deque_iterator_t **it);

/**
 * @brief Fetch iterator to the last element in a deque.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in,out]   it                  Pointer to an iterator which will contain iterator to the last element.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_last(kaa_deque_t *self,kaa_deque_iterator_t **it);

/**
 * @brief Fetch and remove iterator to the first element in a deque.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in,out]   it                  Pointer to an iterator which will contain iterator to the first element.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NOT_FOUND if deque is empty,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_pop_front(kaa_deque_t *self, kaa_deque_iterator_t **it);

/**
 * @brief Fetch and remove iterator to the last element in a deque.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in,out]   it                  Pointer to an iterator which will contain iterator to the last element.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NOT_FOUND if deque is empty,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_pop_back(kaa_deque_t *self, kaa_deque_iterator_t **it);

/**
 * @brief Add data to the beginning of the deque.
 *
 * Allocates memory for new kaa_deque_iterator_t object.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in]       data                Pointer to the data.
 *
 * @return  KAA_ERR_BADPARAM if parameter self is NULL,
 *          KAA_ERR_NOMEM if memory allocation for a new iterator fails,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_push_front_data(kaa_deque_t *self, void *data);

/**
 * @brief Add existing iterator to the beginning of the deque.
 *
 * No additional memory allocation is performed.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in]       it                  Iterator to be added.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_push_front_iterator(kaa_deque_t *self, kaa_deque_iterator_t *it);

/**
 * @brief Add data to the end of the deque.
 *
 * Allocates memory for new kaa_deque_iterator_t object.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in]       data                Pointer to the data.
 *
 * @return  KAA_ERR_BADPARAM if parameter self is NULL,
 *          KAA_ERR_NOMEM if memory allocation for a new iterator fails,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_push_back_data(kaa_deque_t *self, void *data);

/**
 * @brief Add existing iterator to the end of the deque.
 *
 * No additional memory allocation is performed.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 * @param[in]       it                  Iterator to be added.
 *
 * @return  KAA_ERR_BADPARAM if one of parameters is NULL,
 *          KAA_ERR_NONE otherwise.
 */
kaa_error_t             kaa_deque_push_back_iterator(kaa_deque_t *self, kaa_deque_iterator_t *it);

/**
 * @brief Returns number of elements stored in the deque.
 *
 * @param[in]       self                Pointer to the kaa_deque_t object.
 *
 * @return  Number ( >= 0 ) of the elements in a deque,
 *          -1  if parameter self is NULL.
 */
ssize_t                 kaa_deque_size(kaa_deque_t *self);

/**
 * @brief Moves elements from deque2 to deque1.
 *
 * @param[in]       deque1              Pointer to the kaa_deque_t object.
 * @param[in]       deque2              Pointer to the kaa_deque_t object.
 *
 * @return  Pointer to the deque containing all elements from deque1 and deque2.
 *          If deque1 is NULL will return deque2,
 *          otherwise pointer to deque1.
 */
kaa_deque_t            *kaa_deque_merge_move(kaa_deque_t *deque1, kaa_deque_t *deque2);

#endif /* KAA_COLLECTIONS_KAA_DEQUE_H_ */
