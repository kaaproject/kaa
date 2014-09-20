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

#include "kaa_list.h"
#include "kaa_mem.h"

static inline kaa_list_t *kaa_list_tail(kaa_list_t *list)
{
    if (!list) {
        return NULL;
    }

    kaa_list_t *cur = list;
    while (cur->next) {
        cur = cur->next;
    }
    return cur;
}

kaa_error_t kaa_list_push_back(kaa_list_t **head, void *data)
{
    if (!*head) {
        *head = kaa_list_create(data);
        return *head ? KAA_ERR_NONE : KAA_ERR_NOMEM;
    } else {
        kaa_list_t *tail = kaa_list_tail(*head);
        tail->next = kaa_list_create(data);
        return tail->next ? KAA_ERR_NONE : KAA_ERR_NOMEM;
    }
}

kaa_error_t kaa_list_push_front(kaa_list_t **head, void *data)
{
    kaa_list_t *new_item = kaa_list_create(data);
    if (!new_item) {
        return KAA_ERR_NOMEM;
    }
    new_item->next = *head;
    *head = new_item;
    return KAA_ERR_NONE;
}

kaa_list_t *kaa_lists_merge(kaa_list_t *head, kaa_list_t *tail)
{
    if (head) {
        kaa_list_t *destination_head = kaa_list_tail(head);
        destination_head->next = tail;
        return head;
    } else {
        return tail;
    }
}

size_t kaa_list_get_size(kaa_list_t * position)
{
    size_t size = 0;
    if (position) {
        kaa_list_t *cursor = position;
        while (cursor) {
            ++size;
            cursor = cursor->next;
        }
    }
    return size;
}

kaa_list_t *kaa_list_create(void *data) {
    kaa_list_t * new_head = KAA_MALLOC(kaa_list_t);
    if (!new_head) {
        return NULL;
    }
    new_head->data = data;
    new_head->next = NULL;
    return new_head;
}

void kaa_list_destroy(kaa_list_t * head, deallocate_list_data deallocator)
{
    while (head != NULL) {
        kaa_list_t *new_head = head->next;
        if (deallocator) {
            (*deallocator)(head->data);
        }
        KAA_FREE(head->data);
        KAA_FREE(head);
        head = new_head;
    }
}

kaa_list_t *kaa_list_remove_at(kaa_list_t **head, kaa_list_t *position, deallocate_list_data deallocator)
{
    if (position == NULL) {
        return *head;
    }

    if (position == *head) {
        *head = position->next;
        (*deallocator)(position->data);
        KAA_FREE(position->data);
        KAA_FREE(position);
        return *head;
    }

    kaa_list_t *temp_head = *head;
    while (temp_head->next != NULL) {
        if (temp_head->next == position) {
            temp_head->next = temp_head->next->next;
            (*deallocator)(position->data);
            KAA_FREE(position->data);
            KAA_FREE(position);
            return temp_head;
        }
        temp_head = temp_head->next;
    }
    return *head;
}

void kaa_list_set_data_at(kaa_list_t * position, void * data, deallocate_list_data deallocator)
{
    if (position) {
        if (deallocator) {
            (*deallocator)(position->data);
        }
        KAA_FREE(position->data);
        position->data = data;
    }
}

kaa_list_t * kaa_list_insert_after(kaa_list_t * position, void * data)
{
    kaa_list_t *new_element = kaa_list_create(data);
    if (!new_element) {
        return NULL;
    }

    if (position) {
        new_element->next = position->next;
        position->next = new_element;
    }
    return new_element;
}

kaa_list_t * kaa_list_find_next(kaa_list_t * from, match_predicate pred)
{
    while (from) {
        if ((*pred)(from->data)) {
            return from;
        }
        from = from->next;
    }
    return NULL;
}

kaa_list_t * kaa_list_find_last_occurance(kaa_list_t * from, match_predicate pred)
{
    kaa_list_t * it = NULL;
    while (from) {
        if ((*pred)(from->data)) {
            it = from;
        }
        from = from->next;
    }
    return it;
}
