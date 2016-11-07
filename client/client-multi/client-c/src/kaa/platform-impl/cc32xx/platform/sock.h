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

#ifndef CC32XX_SOCK_H_
#define CC32XX_SOCK_H_

/* Avoid redefined warning */
#undef __CONCAT
#undef FD_SETSIZE
#undef FD_SET
#undef FD_CLR
#undef FD_ISSET
#undef FD_ZERO
#undef fd_set
#undef EBADF
#undef EAGAIN
#undef EWOULDBLOCK
#undef ENOMEM
#undef EACCES
#undef EFAULT
#undef EINVAL
#undef EDESTADDRREQ
#undef EPROTOTYPE
#undef ENOPROTOOPT
#undef EPROTONOSUPPORT
#undef EOPNOTSUPP
#undef EAFNOSUPPORT
#undef EADDRINUSE
#undef EADDRNOTAVAIL
#undef ENETUNREACH
#undef ENOBUFS
#undef EISCONN
#undef ENOTCONN
#undef ETIMEDOUT
#undef ECONNREFUSED

#include "simplelink.h"
#include "socket.h"
#include "platform-impl/common/kaa_htonll.h"

struct addrinfo
{
  int ai_flags;			/* Input flags.  */
  int ai_family;		/* Protocol family for socket.  */
  int ai_socktype;		/* Socket type.  */
  int ai_protocol;		/* Protocol for socket.  */
  socklen_t ai_addrlen;		/* Length of socket address.  */
  struct sockaddr *ai_addr;	/* Socket address for socket.  */
  char *ai_canonname;		/* Canonical name for service location.  */
  struct addrinfo *ai_next;	/* Pointer to next in list.  */
};

#define _SS_PADSIZE	(128 - (2 * sizeof (unsigned long)))
struct sockaddr_storage
{
    unsigned short ss_family; /* Address family, etc.  */
    unsigned long __ss_align;	/* Force desired alignment.  */
    char __ss_padding[_SS_PADSIZE];
};

typedef int kaa_fd_t;

typedef struct sockaddr kaa_sockaddr_t;
typedef struct sockaddr_storage kaa_sockaddr_storage_t;
typedef socklen_t kaa_socklen_t;

#define KAA_HTONS(hostshort)     htons((hostshort))
#define KAA_HTONL(hostlong)      htonl((hostlong))
#define KAA_HTONLL(hostlonglong) kaa_htonll((hostlonglong))

#define KAA_NTOHS(netshort)      ntohs((netshort))
#define KAA_NTOHL(netlong)       ntohl((netlong))
#define KAA_NTOHLL(netlonglong)  kaa_ntohll((netlonglong))

#endif /* CC32XX_SOCK_H_ */
