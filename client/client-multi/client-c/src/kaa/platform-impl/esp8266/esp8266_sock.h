#ifndef ESP8266_SOCK_H
#define ESP8266_SOCK_H

#include <sys/socket.h>

typedef int kaa_fd_t;

typedef struct sockaddr kaa_sockaddr_t;
typedef struct sockaddr_in kaa_sockaddr_storage_t;
typedef socklen_t kaa_socklen_t;


#define KAA_HTONS(a)    htons((a))
#define KAA_HTONL(a)    htonl((a))
#define KAA_HTONLL(a)   htonll((a))

#define KAA_NTOHS(a)    ntohs((a))
#define KAA_NTOHL(a)    ntohl((a))
#define KAA_NTOHLL(a)   ntohll((a))
#endif /* ESP8266_SOCK_H */
