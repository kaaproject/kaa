/*
 * Copyright 2015 CyberVision, Inc.
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

#include <arpa/inet.h>
#define KAA_HTONS(hostshort)    htons((hostshort))
#define KAA_HTONL(hostlong)     htonl((hostlong))

#define KAA_NTOHS(netshort)     ntohs((netshort))
#define KAA_NTOHL(netlong)      ntohl((netlong))

#endif /* POSIX_SOCK_H_ */
