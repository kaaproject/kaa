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
econais_ec19d_sock.h
 Created on: Jan 15, 2015
     Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
*/

#ifndef ECONAIS_EC19D_SOCK_H_
#define ECONAIS_EC19D_SOCK_H_

#include <sys/types.h>
#include <sndc_sock_api.h>

typedef int kaa_fd_t;

typedef struct sndc_sockaddr kaa_sockaddr_t;
typedef struct sndc_sockaddr_in kaa_sockaddr_storage_t;
typedef sndc_socklen_t kaa_socklen_t;


#define KAA_HTONS(hostshort)        sndc_htons((hostshort))
#define KAA_HTONL(hostlong)         sndc_htonl((hostlong))
#define KAA_HTONLL(hostlonglong)    kaa_htonll((hostlonglong))

#define KAA_NTOHS(netshort)     sndc_ntohs((netshort))
#define KAA_NTOHL(netlong)      sndc_ntohl((netlong))
#define KAA_NTOHLL(netlonglong) kaa_ntohll((netlonglong))

#endif /* ECONAIS_EC19D_SOCK_H_ */
