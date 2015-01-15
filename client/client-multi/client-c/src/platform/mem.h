/*
 * mem.h
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#ifndef SRC_KAA_PLATFORM_MEM_H_
#define SRC_KAA_PLATFORM_MEM_H_

#ifdef ECONAIS_PLATFORM
#include "../platform-impl/EconaisEC19D/mem.h"
#else
#include "../platform-impl/posix/mem.h"
#endif

#endif /* SRC_KAA_PLATFORM_MEM_H_ */
