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

#include "../platform/platform.h"
#include "kaa_deque.h"

#include "../kaa_common.h"
#include "../utilities/kaa_mem.h"

struct kaa_deque_iterator_t {
    kaa_deque_iterator_t  *prev;
    kaa_deque_iterator_t  *next;
    void * data;
};

static kaa_deque_iterator_t *kaa_deque_iterator_create(kaa_deque_iterator_t *prev, kaa_deque_iterator_t *next, void *data)
{
    kaa_deque_iterator_t *new_it = (kaa_deque_iterator_t *) KAA_MALLOC(sizeof(kaa_deque_iterator_t));
    KAA_RETURN_IF_NIL(new_it, NULL);

    new_it->prev = prev;
    new_it->next = next;
    new_it->data = data;
    if (prev) {
        prev->next = new_it;
    }
    if (next) {
        next->prev = new_it;
    }
    return new_it;
}

void kaa_deque_iterator_destroy(kaa_deque_iterator_t *it, kaa_deque_data_destroy_fn fn)
{
    if (fn) {
        (*fn)(it->data);
    }
    KAA_FREE(it);
}

inline void * kaa_deque_iterator_get_data(kaa_deque_iterator_t *self)
{
    return self
            ? self->data
            : NULL;
}

inline kaa_deque_iterator_t *kaa_deque_iterator_next(kaa_deque_iterator_t *self)
{
    return self
            ? self->next
            : NULL;
}

inline kaa_deque_iterator_t *kaa_deque_iterator_previous(kaa_deque_iterator_t *self)
{
    return self
            ? self->prev
            : NULL;
}

struct kaa_deque_t {
    kaa_deque_iterator_t   *first;
    kaa_deque_iterator_t   *last;
    size_t                  size;
};

kaa_error_t kaa_deque_create(kaa_deque_t **self_p)
{
    KAA_RETURN_IF_NIL(self_p, KAA_ERR_BADPARAM);
    *self_p = (kaa_deque_t *) KAA_MALLOC(sizeof(kaa_deque_t));
    KAA_RETURN_IF_NIL(*self_p, KAA_ERR_NOMEM);
    (*self_p)->first = NULL;
    (*self_p)->last  = NULL;
    (*self_p)->size  = 0;
    return KAA_ERR_NONE;
}

void kaa_deque_destroy(kaa_deque_t *self, kaa_deque_data_destroy_fn fn)
{
    while (self->first) {
        kaa_deque_iterator_t *it = self->first;
        self->first = self->first->next;
        kaa_deque_iterator_destroy(it, fn);
    }
    KAA_FREE(self);
}

inline kaa_error_t kaa_deque_first(kaa_deque_t *self, kaa_deque_iterator_t **it_p)
{
    KAA_RETURN_IF_NIL2(self, it_p, KAA_ERR_BADPARAM);
    *it_p = self->first;
    return KAA_ERR_NONE;
}

inline kaa_error_t kaa_deque_last(kaa_deque_t *self, kaa_deque_iterator_t **it_p)
{
    KAA_RETURN_IF_NIL2(self, it_p, KAA_ERR_BADPARAM);
    *it_p = self->last;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_pop_front(kaa_deque_t *self, kaa_deque_iterator_t **it_p)
{
    KAA_RETURN_IF_NIL2(self, it_p, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->first, KAA_ERR_NOT_FOUND);

    *it_p = self->first;
    self->first = self->first->next;

    (*it_p)->prev = NULL;
    (*it_p)->next = NULL;


    if (!self->first) {
        self->last = NULL;
        self->size = 0;
    } else {
        self->first->prev = NULL;
        --self->size;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_pop_back(kaa_deque_t *self, kaa_deque_iterator_t **it_p)
{
    KAA_RETURN_IF_NIL2(self, it_p, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->last, KAA_ERR_NOT_FOUND);

    *it_p = self->last;
    self->last = self->last->prev;

    (*it_p)->prev = NULL;
    (*it_p)->next = NULL;

    if (!self->last) {
        self->first = NULL;
        self->size  = 0;
    } else {
        self->last->next = NULL;
        --self->size;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_push_front_data(kaa_deque_t *self, void *data)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_deque_iterator_t * new_it = kaa_deque_iterator_create(NULL, self->first, data);
    KAA_RETURN_IF_NIL(new_it, KAA_ERR_NOMEM);

    self->first = new_it;
    if (!self->last) {
        self->last = new_it;
        self->size = 1;
    } else {
        ++self->size;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_push_front_iterator(kaa_deque_t *self, kaa_deque_iterator_t *it)
{
    KAA_RETURN_IF_NIL2(self, it, KAA_ERR_BADPARAM);

    it->prev = NULL;
    it->next = self->first;

    if (self->first) {
        self->first->prev = it;
    }

    self->first = it;
    if (!self->last) {
        self->last = it;
        self->size = 1;
    } else {
        ++self->size;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_push_back_data(kaa_deque_t *self, void *data)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_deque_iterator_t * new_it = kaa_deque_iterator_create(self->last, NULL, data);
    KAA_RETURN_IF_NIL(new_it, KAA_ERR_NOMEM);

    self->last = new_it;
    if (!self->first) {
        self->first = new_it;
        self->size  = 1;
    } else {
        ++self->size;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_deque_push_back_iterator(kaa_deque_t *self, kaa_deque_iterator_t *it)
{
    KAA_RETURN_IF_NIL2(self, it, KAA_ERR_BADPARAM);

    it->prev = self->last;
    it->next = NULL;

    if (self->last) {
        self->last->next = it;
    }

    self->last = it;
    if (!self->first) {
        self->first = it;
        self->size  = 1;
    } else {
        ++self->size;
    }

    return KAA_ERR_NONE;
}

inline ssize_t kaa_deque_size(kaa_deque_t *self)
{
    return self
            ? self->size
            : -1;
}

kaa_deque_t * kaa_deque_merge_move(kaa_deque_t *head, kaa_deque_t *tail)
{
    if (!head) {
        return tail;
    }

    if (tail) {
        if (head->size > 0) {
            if (tail->first) {
                tail->first->prev = head->last;
            }
            head->last->next = tail->first;
            head->size += tail->size;
        } else {
            head->first = tail->first;
            head->last = tail->last;
            head->size = tail->size;
        }
        tail->first = NULL;
        tail->last  = NULL;
        tail->size  = 0;
    }

    return head;
}
