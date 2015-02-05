/*
 * Copyright 2014-2015 CyberVision, Inc.
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

/**
 * @file ext_tcp_utils.h
 * @brief External interface for socket manipulations used by Kaa transport channel implementations.
 *
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 */


#ifndef EXT_TCP_UTILS_H_
#define EXT_TCP_UTILS_H_

#include "sock.h"
#include "../kaa_error.h"

#define KAA_TCP_SOCKET_NOT_SET -1


typedef enum {
    RET_STATE_VALUE_ERROR, RET_STATE_BUFFER_NOT_ENOUGH, RET_STATE_VALUE_READY, RET_STATE_VALUE_IN_PROGRESS
} ext_tcp_utils_function_return_state_t;


typedef enum {
    KAA_TCP_SOCK_ERROR, KAA_TCP_SOCK_CONNECTING, KAA_TCP_SOCK_CONNECTED
} ext_tcp_socket_state_t;


typedef enum {
    KAA_TCP_SOCK_IO_OK, KAA_TCP_SOCK_IO_EOF, KAA_TCP_SOCK_IO_ERROR
} ext_tcp_socket_io_errors_t;


/**
 * @brief Callback for successful DNS results. See @link ext_tcp_utils_gethostbyaddr @endlink.
 *
 * @param[in]   context     Callback's context.
 * @param[in]   addr        The sockaddr structure of the resolved target host.
 * @param[in]   addr_size   The size of the sockaddr structure.
 *
 * @return Error code.
 */
typedef kaa_error_t (*on_dns_resolve_complete_fn)(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size);


/**
 * @brief Callback for negative host resolve results. See @link ext_tcp_utils_gethostbyaddr @endlink.
 *
 * @param[in]   context     Callback's context.
 *
 * @return Error code.
 */
typedef kaa_error_t (*on_dns_resolve_failed_fn)(void *context);


/**
 * Interface for the deferred DNS results. See @link ext_tcp_utils_gethostbyaddr @endlink.
 */
typedef struct {
    void                       *context;             /**< Context to pass to all functions below. */
    on_dns_resolve_complete_fn  on_host_resolved;    /**< Called when the host is successfully resolved. */
    on_dns_resolve_failed_fn    on_resolve_failed;   /**< Called when the host resolve was failed. */
} kaa_dns_resolve_listener_t;


/**
 * The target host information which is used for DNS resolve needs. See @link ext_tcp_utils_gethostbyaddr @endlink.
 */
typedef struct {
    char    *hostname;               /**< The target's hostname. */
    size_t   hostname_length;        /**< The target's hostname length. */
    uint16_t port;                   /**< The target's port. */
} kaa_dns_resolve_info_t;


/**
 * @brief Resolves the hostname of the target host.
 *
 * @param[in]      resolve_listener    Listener's properties. Will be used in case when this function couldn't resolve a host right now.
 * @param[in]      resolve_props       Target host properties (like hostname and port).
 * @param[out]     result              The sockaddr_* structure to which the result will be copied.
 * @param[in,out]  result_size         The total size of the given storage. Will be updated by the actual result size.
 *
 * @return
 *      RET_STATE_VALUE_READY - address was successfully resolved.
 *      RET_STATE_VALUE_IN_PROGRESS - address will be resolved later. See @link kaa_dns_resolve_listener_t @endlink.
 *      RET_STATE_VALUE_ERROR - resolve failed.
 *      RET_STATE_BUFFER_NOT_ENOUGH - given buffer is not enough to store the result.
 */
ext_tcp_utils_function_return_state_t ext_tcp_utils_gethostbyaddr(kaa_dns_resolve_listener_t *resolve_listener, const kaa_dns_resolve_info_t *resolve_props, kaa_sockaddr_t *result, kaa_socklen_t *result_size);


/**
 * @brief Sets a new port value to the given sockaddr structure.
 *
 * @param[in]   addr    A valid pointer to a sockaddr structure.
 * @param[in]   port    A new port value in a host byte order.
 *
 * @return Error code.
 */
kaa_error_t ext_tcp_utils_set_sockaddr_port(kaa_sockaddr_t *addr, uint16_t port);


/**
 * @brief Creates a non blocking TCP socket and connects it to the given target host.
 *
 * @param[out]  fd                  A valid pointer to a socket descriptor where the result will be saved.
 * @param[in]   destination         The destination address of the target host.
 * @param[in]   destination_size    Size of the destination address.
 *
 * @return Error code.
 */
kaa_error_t ext_tcp_utils_open_tcp_socket(kaa_fd *fd, const kaa_sockaddr_t *destination, kaa_socklen_t destination_size);


/**
 * @brief Checks the state of the given socket descriptor.
 *
 * @param[in]   fd                  The socket descriptor which is going to be checked.
 * @param[in]   destination         The destination address of the target host.
 * @param[in]   destination_size    Size of the destination address.
 *
 * @return
 *      KAA_TCP_SOCK_ERROR - connection failed.
 *      KAA_TCP_SOCK_CONNECTING - socket is still connecting.
 *      KAA_TCP_SOCK_CONNECTED - socket was successfully connected.
 */
ext_tcp_socket_state_t ext_tcp_utils_tcp_socket_check(kaa_fd fd, const kaa_sockaddr_t *destination, kaa_socklen_t destination_size);


/**
 * @brief Writes the buffer into the given socket.
 *
 * @param[in]   fd               The socket descriptor.
 * @param[in]   buffer           The buffer which is going to be written.
 * @param[in]   buffer_size      The size of the given buffer.
 * @param[out]  bytes_written    The actual number of bytes which were successfully written.
 *
 * @return
 *       KAA_TCP_SOCK_IO_OK - bytes were successfully written.
 *       KAA_TCP_SOCK_IO_ERROR - write failed.
 */
ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_write(kaa_fd fd, const char *buffer, size_t buffer_size, size_t *bytes_written);


/**
 * @brief Reads bytes from the given socket.
 *
 * @param[in]   fd              The socket descriptor.
 * @param[in]   buffer          The buffer to which the read result will be saved.
 * @param[in]   buffer_size     The maximum number of bytes to be read.
 * @param[out]  bytes_read      The actual number of bytes which were read from the socket.
 *
 * @return
 *      KAA_TCP_SOCK_IO_OK - bytes were successfully obtained from the socket.
 *      KAA_TCP_SOCK_IO_EOF - EOF occurred.
 *      KAA_TCP_SOCK_IO_ERROR - read failed.
 */
ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_read(kaa_fd fd, char *buffer, size_t buffer_size, size_t *bytes_read);


/**
 * @brief Closes the given socket.
 *
 * @param[in]   fd      The socket descriptor which is going to be closed.
 *
 * @return Error code.
 */
kaa_error_t ext_tcp_utils_tcp_socket_close(kaa_fd fd);

#endif /* EXT_TCP_UTILS_H_ */
