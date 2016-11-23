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
* @file econais_ec19d_tcp_utils.c
*/

#include <sndc_dns_api.h>
#include <sndc_sock_api.h>
#include <sndc_sdk_api.h>

#include "platform/sock.h"

#include "platform/ext_tcp_utils.h"
#include "utilities/kaa_log.h"
#include "kaa_error.h"
#include "kaa_common.h"

#define START_BASE_PORT 49500
#define PORT_RANG 10000
/* Auxilaury function to get socket error status,
 * which is a socket level option, so set_sock_option is used */
static kaa_error_t get_sock_error(kaa_fd_t sockid, int *sockerror)
{
    KAA_RETURN_IF_NIL(sockerror, KAA_ERR_BADPARAM);
    if (sockid < 0)
        return KAA_ERR_BADPARAM;

   unsigned int sockerrlen = sizeof(*sockerror);


   if (sndc_sock_getsockopt(sockid, SNDC_SOL_SOCKET, SNDC_SO_ERROR,
                               (void *)sockerror, &sockerrlen) != 0) {
       return KAA_ERR_BADDATA;
   }

   return KAA_ERR_NONE;
}

ext_tcp_utils_function_return_state_t ext_tcp_utils_getaddrbyhost(kaa_dns_resolve_listener_t *resolve_listener
                                                                , const kaa_dns_resolve_info_t *resolve_props
                                                                , kaa_sockaddr_t *result
                                                                , kaa_socklen_t *result_size)
{
    KAA_RETURN_IF_NIL4(resolve_props, result, result_size, resolve_props->hostname, RET_STATE_VALUE_ERROR);

    struct sndc_hostent *hostent;

    char hostname_str[resolve_props->hostname_length + 1];
    memcpy(hostname_str, resolve_props->hostname, resolve_props->hostname_length);
    hostname_str[resolve_props->hostname_length] = '\0';

    hostent = sndc_dns_gethostbyname(hostname_str);
    KAA_RETURN_IF_NIL(hostent, RET_STATE_VALUE_ERROR);

    KAA_RETURN_IF_NIL4(
            hostent->h_name,
            hostent->h_length,
            hostent->h_addr_list,
            *hostent->h_addr_list,
            RET_STATE_VALUE_ERROR);

    kaa_sockaddr_storage_t *result_sockaddr = (kaa_sockaddr_storage_t *)result;
    memset(result_sockaddr,0,sizeof(kaa_sockaddr_storage_t));

    sndc_in_addr_t *addr = (sndc_in_addr_t *) *hostent->h_addr_list;

    result_sockaddr->sin_family = SNDC_AF_INET;
    result_sockaddr->sin_port = sndc_htons(resolve_props->port);
    result_sockaddr->sin_addr.s_addr = *addr;
    result_sockaddr->sin_len = hostent->h_length;

    *result_size = sizeof(kaa_sockaddr_storage_t);

    return RET_STATE_VALUE_READY;
}

kaa_error_t ext_tcp_utils_set_sockaddr_port(kaa_sockaddr_t *addr, uint16_t port)
{
    KAA_RETURN_IF_NIL2(addr,port,KAA_ERR_BADPARAM);

    if (addr->sa_family == SNDC_AF_INET) {
        ((struct sndc_sockaddr_in *)addr)->sin_port = sndc_htons(port);
        return KAA_ERR_NONE;
    } else {
        return KAA_ERR_SOCKET_INVALID_FAMILY;
    }
}

kaa_error_t ext_tcp_utils_open_tcp_socket(kaa_fd_t *fd
                                        , const kaa_sockaddr_t *destination
                                        , kaa_socklen_t destination_size)
{
    KAA_RETURN_IF_NIL3(fd, destination, destination_size, KAA_ERR_BADPARAM);

    *fd = -1;

    if (destination->sa_family != SNDC_AF_INET) {
        return KAA_ERR_SOCKET_INVALID_FAMILY;
    }

    kaa_fd_t sock = sndc_sock_socket(SNDC_PF_INET, SNDC_SOCK_STREAM, SNDC_IPPROTO_TCP);
    if (sock < 0)
        return KAA_ERR_SOCKET_ERROR;
    int status = 0;
    int iteration = 10;
    struct sndc_sockaddr_in bind_sockaddr;
    do {
        memset(&bind_sockaddr, 0, sizeof(bind_sockaddr));

        bind_sockaddr.sin_family = SNDC_AF_INET;
        bind_sockaddr.sin_len = sizeof(bind_sockaddr);
        bind_sockaddr.sin_addr.s_addr = SNDC_INADDR_ANY;

        uint16_t bind_port = START_BASE_PORT + sndc_sys_getRandom() % PORT_RANG;
        bind_sockaddr.sin_port = sndc_htons(bind_port);

        status = sndc_sock_bind(sock, (struct sndc_sockaddr*) &bind_sockaddr, sizeof(bind_sockaddr));
        iteration--;
    } while (status && iteration);

    bool_t t = true;
    if (sndc_sock_ioctl(sock, SNDC_FIONBIO, (void *)&t) < 0) {
        ext_tcp_utils_tcp_socket_close(sock);
        return KAA_ERR_SOCKET_ERROR;
    }

    if (sndc_sock_fcntl(sock, SNDC_F_SETFL, SNDC_O_NONBLOCK) < 0) {
        ext_tcp_utils_tcp_socket_close(sock);
        return KAA_ERR_SOCKET_ERROR;
    }

    status = sndc_sock_connect(sock, destination, destination_size);

    if (status) {
        if (get_sock_error(sock, &status)) {
            ext_tcp_utils_tcp_socket_close(sock);
            return KAA_ERR_SOCKET_ERROR;
        }
        if (!(status == EINPROGRESS
                || status == EAGAIN)) {
            sndc_printf("Socket %d connect error %d\n", sock, status);
            ext_tcp_utils_tcp_socket_close(sock);
            return KAA_ERR_SOCKET_CONNECT_ERROR;
        }
    }
    *fd = sock;
    return KAA_ERR_NONE;
}

ext_tcp_socket_state_t ext_tcp_utils_tcp_socket_check(kaa_fd_t fd
                                                    , const kaa_sockaddr_t *destination
                                                    , kaa_socklen_t destination_size)
{
    int status = 0;
    kaa_error_t error = get_sock_error(fd, &status);
    if (error) {
        return KAA_TCP_SOCK_ERROR;
    }
    switch (status) {
        case EINPROGRESS:
        case EALREADY:
            return KAA_TCP_SOCK_CONNECTING;
        case 0:
        case EISCONN:
            return KAA_TCP_SOCK_CONNECTED;
        default:
            return KAA_TCP_SOCK_ERROR;
    }
    return KAA_TCP_SOCK_CONNECTED;
}

ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_write(kaa_fd_t fd
                                                        , const char *buffer
                                                        , size_t buffer_size
                                                        , size_t *bytes_written)
{
    KAA_RETURN_IF_NIL2(buffer, buffer_size, KAA_TCP_SOCK_IO_ERROR);
    int write_result = sndc_sock_write(fd, (void *)buffer, buffer_size);
    if (write_result < 0) {
        int status = 0;
        kaa_error_t error = get_sock_error(fd, &status);
        KAA_RETURN_IF_ERR(error);
        if (status != EAGAIN)
            return KAA_TCP_SOCK_IO_ERROR;
    }
    if (bytes_written)
        *bytes_written = (write_result > 0) ? write_result : 0;
    return KAA_TCP_SOCK_IO_OK;
}

ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_read(kaa_fd_t fd
                                                       , char *buffer
                                                       , size_t buffer_size
                                                       , size_t *bytes_read)
{
    KAA_RETURN_IF_NIL2(buffer, buffer_size, KAA_TCP_SOCK_IO_ERROR);
    int read_result = sndc_sock_read(fd, buffer, buffer_size);
    if (!read_result)
        return KAA_TCP_SOCK_IO_EOF;
    if (read_result < 0) {
        int status = 0;
        kaa_error_t error = get_sock_error(fd, &status);
        KAA_RETURN_IF_ERR(error);
        if (status != EAGAIN)
            return KAA_TCP_SOCK_IO_ERROR;
    }
    if (bytes_read)
        *bytes_read = (read_result > 0) ? read_result : 0;
    return KAA_TCP_SOCK_IO_OK;
}



kaa_error_t ext_tcp_utils_tcp_socket_close(kaa_fd_t fd)
{
    return (sndc_sock_close(fd) < 0) ? KAA_ERR_SOCKET_ERROR : KAA_ERR_NONE;
}
