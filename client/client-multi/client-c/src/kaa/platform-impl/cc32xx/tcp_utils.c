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

#include "platform/ext_tcp_utils.h"
#include <platform/stdio.h>
#include "kaa_common.h"
#include <errno.h>

//Driverlib includes
#include "hw_types.h"
#include "hw_ints.h"
#include "rom.h"
#include "rom_map.h"
#include "interrupt.h"
#include "prcm.h"

//Common interface includes
#include "common.h"
#include "uart_if.h"

kaa_error_t ext_tcp_utils_set_sockaddr_port(kaa_sockaddr_t *addr, uint16_t port)
{    
    KAA_RETURN_IF_NIL2(addr, port, KAA_ERR_BADPARAM);
    switch (addr->sa_family) {
        case AF_INET: {
            struct sockaddr_in *s_in = (struct sockaddr_in *) addr;
            s_in->sin_port = KAA_HTONS(port);
            break;
        }
        default:
            return KAA_ERR_SOCKET_INVALID_FAMILY;
    }
    return KAA_ERR_NONE;
}



ext_tcp_utils_function_return_state_t ext_tcp_utils_getaddrbyhost(kaa_dns_resolve_listener_t *resolve_listener
                                                                , const kaa_dns_resolve_info_t *resolve_props
                                                                , kaa_sockaddr_t *result
                                                                , kaa_socklen_t *result_size)
{
    (void)resolve_listener;
    KAA_RETURN_IF_NIL4(resolve_props, resolve_props->hostname, result, result_size, RET_STATE_VALUE_ERROR);
    if (*result_size < sizeof(struct sockaddr_in))
        return RET_STATE_BUFFER_NOT_ENOUGH;

    unsigned long out_ip = 0;
    int ai_family;
    int resolve_error = 0;
    struct sockaddr_in tmp_addr;

    ai_family = AF_INET;
    *result_size = sizeof(struct sockaddr_in);

    char hostname_str[resolve_props->hostname_length + 1];
    memcpy(hostname_str, resolve_props->hostname, resolve_props->hostname_length);
    hostname_str[resolve_props->hostname_length] = '\0';

    if (strcmp(hostname_str, "localhost"))
        resolve_error = sl_NetAppDnsGetHostByName((signed char*)hostname_str, resolve_props->hostname_length, &out_ip, ai_family);
    else
        out_ip = 0x7F000001;

    memset(&tmp_addr, 0, *result_size);
    tmp_addr.sin_family = ai_family;
    tmp_addr.sin_addr.s_addr = sl_Htonl((unsigned int)out_ip);
    tmp_addr.sin_port = sl_Htons((unsigned short)resolve_props->port);

    if (resolve_error || !out_ip)
        return RET_STATE_VALUE_ERROR;

    memcpy(result, (struct sockaddr*)&tmp_addr, *result_size);
    return RET_STATE_VALUE_READY;
}



kaa_error_t ext_tcp_utils_open_tcp_socket(kaa_fd_t *fd
                                        , const kaa_sockaddr_t *destination
                                        , kaa_socklen_t destination_size)
{
    KAA_RETURN_IF_NIL3(fd, destination, destination_size, KAA_ERR_BADPARAM);    

    long lNonBlocking = 1;
    int status;

    kaa_fd_t sock = socket(destination->sa_family, SOCK_STREAM, 0);
    if (sock < 0)
        return KAA_ERR_SOCKET_ERROR;

    status = setsockopt(sock, SL_SOL_SOCKET, SL_SO_NONBLOCKING,
                            &lNonBlocking, sizeof(lNonBlocking));
    if ( status < 0 ) {
        ext_tcp_utils_tcp_socket_close(sock);
        return KAA_ERR_SOCKET_ERROR;
    }

    int err = connect(sock, destination, destination_size);
    if ( (err < 0 && err != SL_EALREADY)  && errno != EINPROGRESS) {
        ext_tcp_utils_tcp_socket_close(sock);
        return KAA_ERR_SOCKET_CONNECT_ERROR;
    }

    *fd = sock;
    return KAA_ERR_NONE;
}



ext_tcp_socket_state_t ext_tcp_utils_tcp_socket_check(kaa_fd_t fd
                                                    , const kaa_sockaddr_t *destination
                                                    , kaa_socklen_t destination_size)
{
    if (connect(fd, destination, destination_size) < 0 ) {
        switch (errno) {
            case EINPROGRESS:
            case EALREADY:
                return KAA_TCP_SOCK_CONNECTING;
            case EISCONN:
                return KAA_TCP_SOCK_CONNECTED;
            default:
                return KAA_TCP_SOCK_ERROR;
        }
    }
    return KAA_TCP_SOCK_CONNECTED;
}



ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_write(kaa_fd_t fd
                                                        , const char *buffer
                                                        , size_t buffer_size
                                                        , size_t *bytes_written)
{
    KAA_RETURN_IF_NIL2(buffer, buffer_size, KAA_TCP_SOCK_IO_ERROR);
    ssize_t write_result = send(fd, buffer, buffer_size, 0);
    if (write_result < 0 && errno != EAGAIN)
        return KAA_TCP_SOCK_IO_ERROR;
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
    ssize_t read_result = recv(fd, buffer, buffer_size, 0);
    if (!read_result)
        return KAA_TCP_SOCK_IO_EOF;
    if (read_result < 0 && errno != EAGAIN)
        return KAA_TCP_SOCK_IO_ERROR;
    if (bytes_read)
        *bytes_read = (read_result > 0) ? read_result : 0;
    return KAA_TCP_SOCK_IO_OK;
}



kaa_error_t ext_tcp_utils_tcp_socket_close(kaa_fd_t fd)
{
    return (close(fd) < 0) ? KAA_ERR_SOCKET_ERROR : KAA_ERR_NONE;
}
