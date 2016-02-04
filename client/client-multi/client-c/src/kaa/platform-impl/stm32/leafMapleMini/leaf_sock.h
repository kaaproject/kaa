/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 @file leaf_sock.h
*/

#ifndef LEAF_SOCK_H_
#define LEAF_SOCK_H_

//TODO create

//#include <sys/socket.h>


typedef int kaa_fd_t;
typedef size_t kaa_socklen_t;
typedef unsigned char kaa_uint8_t;
typedef unsigned short kaa_uint16_t;

typedef uint32_t in_addr_t;

struct in_addr {
	in_addr_t s_addr;
};

struct leaf_sockaddr {
	kaa_uint8_t	sa_len;
	kaa_uint8_t sa_family;
	char sa_data[14];
};

struct leaf_sockaddr_in {
	kaa_uint8_t	sin_len;
	kaa_uint8_t sin_family;
	kaa_uint16_t sin_port;
	struct in_addr sin_addr;
	char sin_zero[8];
};

typedef struct leaf_sockaddr kaa_sockaddr_t;
typedef struct leaf_sockaddr_in kaa_sockaddr_storage_t;

//kaa_uint16_t htons(kaa_uint16_t value);
//kaa_uint16_t ntohs(kaa_uint16_t value);
//
//uint32_t htonl(uint32_t value);
//uint32_t ntohl(uint32_t value);
//
//#define KAA_HTONS(hostshort)    htons((hostshort))
//#define KAA_HTONL(hostlong)     htonl((hostlong))
//
//#define KAA_NTOHS(netshort)     ntohs((netshort))
//#define KAA_NTOHL(netlong)      ntohl((netlong))

#define KAA_HTONS(n) (((((unsigned short)(n) & 0xFF)) << 8) | (((unsigned short)(n) & 0xFF00) >> 8))
#define KAA_NTOHS(n) (((((unsigned short)(n) & 0xFF)) << 8) | (((unsigned short)(n) & 0xFF00) >> 8))

#define KAA_HTONL(n) (((((unsigned long)(n) & 0xFF)) << 24) | \
                  ((((unsigned long)(n) & 0xFF00)) << 8) | \
                  ((((unsigned long)(n) & 0xFF0000)) >> 8) | \
                  ((((unsigned long)(n) & 0xFF000000)) >> 24))

#define KAA_NTOHL(n) (((((unsigned long)(n) & 0xFF)) << 24) | \
                  ((((unsigned long)(n) & 0xFF00)) << 8) | \
                  ((((unsigned long)(n) & 0xFF0000)) >> 8) | \
                  ((((unsigned long)(n) & 0xFF000000)) >> 24))

#define KAA_HTONLL(n) (((((unsigned long long)(n) & 0xFF)) << 56) | \
                       ((((unsigned long long)(n) & 0xFF00)) << 48) | \
                       ((((unsigned long long)(n) & 0xFF0000)) << 24) | \
                       ((((unsigned long long)(n) & 0xFF000000)) << 8) | \
                       ((((unsigned long long)(n) & 0xFF00000000)) >> 8) | \
                       ((((unsigned long long)(n) & 0xFF0000000000)) >> 24) | \
                       ((((unsigned long long)(n) & 0xFF000000000000)) >> 48) | \
                       ((((unsigned long long)(n) & 0xFF00000000000000)) >> 56))

#define KAA_NTOHLL(n) KAA_HTONLL(n)

#endif /* LEAF_SOCK_H_ */
