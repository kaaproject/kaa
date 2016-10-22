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

#ifndef ESP8266_SOCK_H
#define ESP8266_SOCK_H

#include <lwip/lwip/sockets.h>

typedef int kaa_fd_t;

typedef struct sockaddr kaa_sockaddr_t;
typedef struct sockaddr_in kaa_sockaddr_storage_t;
typedef socklen_t kaa_socklen_t;


#define KAA_HTONS(a)    htons((a))
#define KAA_HTONL(a)    htonl((a))
#define KAA_HTONLL(a)   kaa_htonll((a))

#define KAA_NTOHS(a)    ntohs((a))
#define KAA_NTOHL(a)    ntohl((a))
#define KAA_NTOHLL(a)   kaa_ntohll((a))
#endif /* ESP8266_SOCK_H */
