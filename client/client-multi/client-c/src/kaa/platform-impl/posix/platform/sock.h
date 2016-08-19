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

/*
posix_sock.h
 Created on: Jan 15, 2015
     Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
*/

#ifndef POSIX_SOCK_H_
#define POSIX_SOCK_H_

#include <sys/socket.h>
#include <sys/select.h>
#include <arpa/inet.h>

#include "platform-impl/common/kaa_htonll.h"

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

#endif /* POSIX_SOCK_H_ */
