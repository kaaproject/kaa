/*
 * mem.h
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#ifndef SRC_KAA_PLATFORM_IMPL_ECONAISEC19D_MEM_H_
#define SRC_KAA_PLATFORM_IMPL_ECONAISEC19D_MEM_H_

#include <sndc_mem_api.h>

#ifndef KAA_MALLOC
#define KAA_MALLOC(S)           sndc_mem_malloc(S)
#endif

#ifndef KAA_CALLOC
#define KAA_CALLOC(N,S)         sndc_mem_calloc(N, S)
#endif

#ifndef KAA_FREE
#define KAA_FREE(P)             sndc_mem_free(P)
#endif

#endif /* SRC_KAA_PLATFORM_IMPL_ECONAISEC19D_MEM_H_ */
