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

#ifndef ESP8266_MEM_H
#define ESP8266_MEM_H

#include <stddef.h>

extern void *pvPortMalloc(size_t xSize);
extern void *pvPortZalloc(size_t xSize);
extern void *pvPortRealloc(void *pvPtr, size_t xSize);
extern void vPortFree(void *pvPtr);

#ifndef __KAA_MALLOC
#define __KAA_MALLOC(S) pvPortMalloc(S)
#endif /* __KAA_MALLOC */

#ifndef __KAA_CALLOC
#define __KAA_CALLOC(S, N) pvPortZalloc((S)*(N))
#endif /* __KAA_CALLOC */

#ifndef __KAA_REALLOC
#define __KAA_REALLOC(P, S) pvPortRealloc(P, S)
#endif /* __KAA_REALLOC */

#ifndef __KAA_FREE
#define __KAA_FREE(P) vPortFree(P)
#endif /* __KAA_FREE */

#endif /* ESP8266_MEM_H */
