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

#include "kaa_mem.h"

#ifdef KAA_TRACE_MEMORY_ALLOCATIONS
void *malloc_stub(size_t s, const char *file, int line) {
    void *ptr = malloc(s);
    kaa_log_write(file, line, KAA_LOG_TRACE, KAA_ERR_NONE, "Allocated (using malloc) %zu bytes at {%p}", s, ptr);
    return ptr;
}

void *calloc_stub(size_t n, size_t s, const char *file, int line) {
    void *ptr = calloc(n, s);
    kaa_log_write(file, line, KAA_LOG_TRACE, KAA_ERR_NONE, "Allocated (using calloc) %zu blocks of %zu bytes (total %zu) at {%p}", n, s, n*s, ptr);
    return ptr;
}
#endif


