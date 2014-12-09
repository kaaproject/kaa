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


struct kaa_list_t {
    void              *data;
    struct kaa_list_t *next;
};

static void kaa_list_destroy_node(kaa_list_t *position, deallocate_list_data deallocator)
{
    if (position) {
        if (deallocator) {
            (*deallocator)(position->data);
        } else {
            KAA_FREE(position->data);
        }
        KAA_FREE(position);
    }
}

kaa_list_t *kaa_list_push_back(kaa_list_t *head, void *data)
{
    KAA_RETURN_IF_NIL(head, NULL);
    while (head->next) {
        head = head->next;
    }
    head->next = kaa_list_create(data);
    return head->next;
}

kaa_list_t *kaa_list_push_front(kaa_list_t *head, void *data)
{
    KAA_RETURN_IF_NIL(head, NULL);
    kaa_list_t *new_item = kaa_list_create(data);
    KAA_RETURN_IF_NIL(new_item, NULL);
    new_item->next = head;
    return new_item;
}

void *kaa_list_get_data(kaa_list_t *position)
{
    return (position ? position->data : NULL);
}

bool kaa_list_has_next(kaa_list_t *position)
{
    return (position && position->next);
}

kaa_list_t *kaa_list_next(kaa_list_t *position)
{
    return (position ? position->next : NULL);
}

kaa_list_t *kaa_lists_merge(kaa_list_t *destination_head, kaa_list_t *tail)
{
    if (destination_head) {
        kaa_list_t *head = destination_head;
        while (destination_head->next) {
            destination_head = destination_head->next;
        }
        destination_head->next = tail;
        return head;
    }
    return tail;
}

ssize_t kaa_list_get_size(kaa_list_t *position)
{
    if (position) {
        ssize_t size = 0;
        kaa_list_t *cursor = position;
        while (cursor) {
            ++size;
            cursor = cursor->next;
        }
        return size;
    }
    return -1;
}

kaa_list_t *kaa_list_create(void *data) {
    kaa_list_t *new_head = (kaa_list_t *) KAA_MALLOC(sizeof(kaa_list_t));
    KAA_RETURN_IF_NIL(new_head, NULL);
    new_head->data = data;
    new_head->next = NULL;
    return new_head;
}

void kaa_list_destroy(kaa_list_t *head, deallocate_list_data deallocator)
{
    while (head) {
        kaa_list_t *new_head = head->next;
        if (deallocator) {
            (*deallocator)(head->data);
        } else {
            KAA_FREE(head->data);
        }
        KAA_FREE(head);
        head = new_head;
    }
}

void kaa_list_destroy_no_data_cleanup(kaa_list_t *head)
{
    while (head) {
        kaa_list_t *new_head = head->next;
        KAA_FREE(head);
        head = new_head;
    }
}

kaa_list_t *kaa_list_remove_at(kaa_list_t **head, kaa_list_t *position, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL3(head, *head, position, NULL);

    if (position == *head) {
        *head = position->next;
        kaa_list_destroy_node(position, deallocator);
        return *head;
    }

    for (kaa_list_t *curr_head = *head; curr_head->next != NULL; curr_head = curr_head->next) {
        if (curr_head->next == position) {
            curr_head->next = curr_head->next->next;
            kaa_list_destroy_node(position, deallocator);
            return curr_head;
        }
    }
    return NULL;
}

void kaa_list_set_data_at(kaa_list_t *position, void *data, deallocate_list_data deallocator)
{
    if (position) {
        if (deallocator) {
            (*deallocator)(position->data);
        } else {
            KAA_FREE(position->data);
        }
        position->data = data;
    }
}

kaa_list_t *kaa_list_insert_after(kaa_list_t *position, void *data)
{
    KAA_RETURN_IF_NIL(position, NULL);
    kaa_list_t *new_element = kaa_list_create(data);
    KAA_RETURN_IF_NIL(new_element, NULL);

    new_element->next = position->next;
    position->next = new_element;
    return new_element;
}

kaa_list_t *kaa_list_find_next(kaa_list_t *from, match_predicate pred)
{
    KAA_RETURN_IF_NIL2(from, pred, NULL);
    while (from) {
        if ((*pred)(from->data)) {
            return from;
        }
        from = from->next;
    }
    return NULL;
}

kaa_list_t *kaa_list_find_last_occurance(kaa_list_t *from, match_predicate pred)
{
    KAA_RETURN_IF_NIL2(from, pred, NULL);
    kaa_list_t *it = NULL;
    while (from) {
        if ((*pred)(from->data)) {
            it = from;
        }
        from = from->next;
    }
    return it;
}

kaa_list_t *kaa_list_split_after(kaa_list_t *head, kaa_list_t *after, kaa_list_t **tail)
{
    KAA_RETURN_IF_NIL2(head, after, NULL);
    while (head != after) {
        head = head->next;
        KAA_RETURN_IF_NIL(head, NULL);
    }
    kaa_list_t *ret_val = head->next;
    head->next = NULL;
    if (tail) {
        *tail = ret_val;
    }
    return ret_val;
}
