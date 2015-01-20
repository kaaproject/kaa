/*
 * mem.h
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#ifndef SRC_KAA_PLATFORM_IMPL_POSIX_MEM_H_
#define SRC_KAA_PLATFORM_IMPL_POSIX_MEM_H_

#include <stdlib.h>

#ifndef KAA_MALLOC
#define KAA_MALLOC(S)           malloc(S)
#endif

#ifndef KAA_CALLOC
#define KAA_CALLOC(N,S)         calloc(N, S)
#endif

#ifndef KAA_FREE
#define KAA_FREE(P)             free(P)
#endif

#endif /* SRC_KAA_PLATFORM_IMPL_POSIX_MEM_H_ */
