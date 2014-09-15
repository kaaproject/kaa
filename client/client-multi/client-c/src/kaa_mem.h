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

#ifndef KAA_MEM_H_
#define KAA_MEM_H_

#ifdef KAA_TRACE_MEMORY_ALLOCATIONS
#include <stdlib.h>
#include <stdio.h>
void *malloc_stub(size_t s, const char *file, int line);
void *calloc_stub(size_t n, size_t s, const char *file, int line);

#define KAA_MALLOC(T)           (T*)malloc_stub(sizeof(T), __FILE__, __LINE__)
#define KAA_CALLOC(N,S)         calloc_stub(N, S, __FILE__, __LINE__)
#define KAA_FREE(P)             printf("[%s:%i] going to deallocate memory at {%p}\n", __FILE__, __LINE__, P);     free((void *)P)
#else
#include <stdlib.h>
#define KAA_MALLOC(T)           (T*)malloc(sizeof(T))
#define KAA_CALLOC(N,S)         calloc(N, S)
#define KAA_FREE(P)             free(P)
#endif


#endif /* KAA_MEM_H_ */
