#ifndef ESP8266_MEM_H
#define ESP8266_MEM_H

#include <mem.h>

#ifndef __KAA_MALLOC
#define __KAA_MALLOC(S) os_malloc(S)
#endif /* __KAA_MALLOC */

#ifndef __KAA_CALLOC
#define __KAA_CALLOC(S, N) os_zalloc((S)*(N))
#endif /* __KAA_CALLOC */

#ifndef __KAA_FREE
#define __KAA_FREE(P) os_free(P)
#endif /* __KAA_FREE */

#endif /* ESP8266_MEM_H */
