/*
 * stdio.h
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#ifndef SRC_KAA_PLATFORM_STDIO_H_
#define SRC_KAA_PLATFORM_STDIO_H_

#ifdef ECONAIS_PLATFORM
#include "../platform-impl/EconaisEC19D/stdio.h"
#else
#include "../platform-impl/posix/stdio.h"
#endif

#endif /* SRC_KAA_PLATFORM_STDIO_H_ */
