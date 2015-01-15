/*
 * sock.h
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#ifndef SRC_KAA_PLATFORM_SOCK_H_
#define SRC_KAA_PLATFORM_SOCK_H_

#ifdef ECONAIS_PLATFORM
#include "../platform-impl/EconaisEC19D/sock.h"
#else
#include "../platform-impl/posix/sock.h"
#endif


#endif /* SRC_KAA_PLATFORM_SOCK_H_ */
