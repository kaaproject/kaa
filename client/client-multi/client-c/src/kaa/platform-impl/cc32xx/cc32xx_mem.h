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

#ifndef CC32XX_MEM_H_
#define CC32XX_MEM_H_

#include <stdlib.h>

#ifndef __KAA_MALLOC
#define __KAA_MALLOC(S)           malloc(S)
#endif

#ifndef __KAA_CALLOC
#define __KAA_CALLOC(N,S)         calloc(N, S)
#endif

#ifndef __KAA_REALLOC
#define __KAA_REALLOC(P, S)       realloc(P, S)
#endif

#ifndef __KAA_FREE
#define __KAA_FREE(P)             free(P)
#endif

#endif /* CC32XX_MEM_H_ */
