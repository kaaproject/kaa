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

#include <stddef.h>
#include <stdint.h>

#include "kaa_list.h"
#include "kaa_common.h"
#include "utilities/kaa_mem.h"



struct kaa_list_node_t {
    void                      *data;
    struct kaa_list_node_t    *next;
    struct kaa_list_node_t    *prev;
};

struct kaa_list_t {
    kaa_list_node_t    *head;
    kaa_list_node_t    *tail;
    size_t             size;
};

static kaa_list_node_t *set_next_neighbor(kaa_list_node_t *whom, kaa_list_node_t *neighbor)
{
    if (neighbor) {
        neighbor->prev = whom;
    }

    if (whom) {
        whom->next = neighbor;
    }

    return neighbor;
}

static kaa_list_node_t *create_node(void *data)
{
    KAA_RETURN_IF_NIL(data, NULL);
    kaa_list_node_t *node = (kaa_list_node_t *)KAA_MALLOC(sizeof(kaa_list_node_t));
    KAA_RETURN_IF_NIL(node, NULL);
    node->data = data;
    node->next = node->prev = NULL;
    return node;
}

static void destroy_node(kaa_list_node_t *it, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL(it, );
    if (deallocator) {
        (*deallocator)(it->data);
    } else {
        KAA_FREE(it->data);
    }
    KAA_FREE(it);
}

static void reset_list(kaa_list_t *list)
{
    KAA_RETURN_IF_NIL(list, );
    list->head = list->tail = NULL;
    list->size = 0;
}

void *kaa_list_get_data(kaa_list_node_t *it)
{
    return (it ? it->data : NULL);
}

kaa_list_node_t *kaa_list_next(kaa_list_node_t *it)
{
    return (it ? it->next : NULL);
}

kaa_list_node_t *kaa_list_prev(kaa_list_node_t *it)
{
    return (it ? it->prev : NULL);
}

kaa_list_t *kaa_lists_merge(kaa_list_t *destination_head, kaa_list_t *tail)
{
    KAA_RETURN_IF_NIL(destination_head, tail);
    KAA_RETURN_IF_NIL(tail, destination_head);

    if (!destination_head->head) {
        destination_head->head = tail->head;
    } else {
        set_next_neighbor(destination_head->tail, tail->head);
    }

    destination_head->tail = tail->tail;
    destination_head->size += tail->size;

    reset_list(tail);

    return destination_head;
}

size_t kaa_list_get_size(kaa_list_t *list)
{
    KAA_RETURN_IF_NIL(list, 0);
    return list->size;
}

kaa_list_t *kaa_list_create(void)
{
    return (kaa_list_t *) KAA_CALLOC(1, sizeof(kaa_list_t));
}

kaa_list_node_t *kaa_list_push_front(kaa_list_t *list, void *data)
{
    KAA_RETURN_IF_NIL2(list, data, NULL);
    kaa_list_node_t *node = create_node(data);
    KAA_RETURN_IF_NIL(node, NULL);

    ++list->size;
    set_next_neighbor(node, list->head);

    list->head = node;
    if (!list->tail) {
        list->tail = node;
    }

    return node;
}

kaa_list_node_t *kaa_list_push_back(kaa_list_t *list, void *data)
{
    KAA_RETURN_IF_NIL(list, NULL);
    kaa_list_node_t *node = create_node(data);
    KAA_RETURN_IF_NIL(node, NULL);

    ++list->size;
    set_next_neighbor(list->tail, node);

    list->tail = node;
    if (!list->head) {
        list->head = node;
    }

    return node;
}

kaa_list_node_t *kaa_list_begin(kaa_list_t *list)
{
    KAA_RETURN_IF_NIL(list, NULL);
    return list->head;
}

kaa_list_node_t *kaa_list_back(kaa_list_t *list)
{
    KAA_RETURN_IF_NIL(list, NULL);
    return list->tail;
}

void kaa_list_clear(kaa_list_t *list, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL2(list, list->size, );
    kaa_list_node_t *it = list->head;
    while (it) {
        kaa_list_node_t *next = it->next;
        destroy_node(it, deallocator);
        it = next;
    }

    reset_list(list);
}

void kaa_list_destroy(kaa_list_t *list, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL(list, );
    kaa_list_clear(list, deallocator);
    KAA_FREE(list);
}

kaa_list_node_t *kaa_list_remove_at(kaa_list_t *list, kaa_list_node_t *it, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL3(list, it, list->size, NULL);

    kaa_list_node_t *next = it->next;
    if (list->head == it) {
        list->head = next;
    }
    if (list->tail == it) {
        list->tail = it->prev;
    }

    set_next_neighbor(it->prev, next);
    destroy_node(it, deallocator);
    --list->size;

    return next;
}

kaa_error_t kaa_list_remove_first(kaa_list_t *list, match_predicate pred, void *context, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL3(list, pred, list->size, KAA_ERR_BADPARAM);

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(list), pred, context);
    kaa_list_remove_at(list, it, deallocator);
    return it ? KAA_ERR_NONE : KAA_ERR_NOT_FOUND;
}

void kaa_list_set_data_at(kaa_list_node_t *it, void *data, deallocate_list_data deallocator)
{
    KAA_RETURN_IF_NIL(it, );
    if (deallocator) {
        deallocator(it->data);
    } else {
        KAA_FREE(it->data);
    }
    it->data = data;
}

kaa_list_node_t *kaa_list_find_next(kaa_list_node_t *from, match_predicate pred, void *context)
{
    KAA_RETURN_IF_NIL2(from, pred, NULL);
    while (from) {
        if (pred(from->data, context)) {
            return from;
        }
        from = from->next;
    }
    return NULL;
}

void kaa_list_for_each(kaa_list_node_t *first, kaa_list_node_t *last, process_data process, void *context)
{
    KAA_RETURN_IF_NIL3(first, last, process, );
    while (first) {
        process(first->data, context);
        if (first == last) {
            break;
        }

        first = kaa_list_next(first);
    }
}

static kaa_list_node_t *kaa_split_util(kaa_list_node_t *head);


static kaa_list_node_t *kaa_merge_util(kaa_list_node_t *first, kaa_list_node_t *second, match_predicate pred)
{
    KAA_RETURN_IF_NIL(first, second);
    KAA_RETURN_IF_NIL(second, first);

    if (pred(first->data, second->data)) {

        first->next = kaa_merge_util(first->next, second, pred);
        first->next->prev = first;
        first->prev = NULL;

        return first;
    } else {
        second->next = kaa_merge_util(first, second->next, pred);
        second->next->prev = second;
        second->prev = NULL;
        return second;
    }
}

static kaa_list_node_t *kaa_merge_sort(kaa_list_node_t *head, match_predicate pred)
{
    KAA_RETURN_IF_NIL2(head, head->next, head);

    kaa_list_node_t *second = kaa_split_util(head);

    head = kaa_merge_sort(head, pred);
    second = kaa_merge_sort(second, pred);

    head = kaa_merge_util(head, second, pred);
    return head;
}

static kaa_list_node_t *kaa_split_util( kaa_list_node_t *head)
{
    kaa_list_node_t *fast = head;
    kaa_list_node_t *slow = head;
    while (fast->next && fast->next->next) {
        fast = fast->next->next;
        slow = slow->next;
    }
    kaa_list_node_t *temp = slow->next;
    slow->next = NULL;
    return temp;
}

void kaa_list_sort(kaa_list_t *list, match_predicate pred)
{
    KAA_RETURN_IF_NIL(list->size,);
    kaa_list_node_t *node = kaa_merge_sort(kaa_list_begin(list), pred);
    list->head = node;
    while (node->next) {
        node = node->next;
    }
    list->tail = node;
}

int32_t kaa_list_hash(kaa_list_t *list, list_node_hash pred)
{
    KAA_RETURN_IF_NIL2(list, pred, 0);
    uint32_t result = 1;
    kaa_list_node_t *node = kaa_list_begin(list);
    while (node) {
        uint64_t element = pred(node->data);
        result = 31 * result + (uint32_t) (element ^ (element >> 32));
        node = node->next;
    }
    return (int32_t) result;
}
