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

/*
 * ext_tcp_utils.h
 *
 *  Created on: Jan 23, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef EXT_TCP_UTILS_H_
#define EXT_TCP_UTILS_H_

typedef int kaa_fd;
#define KAA_TCP_SOCKET_NOT_SET -1

typedef struct {
    uint32_t ip_addr;
    uint16_t port;
} kaa_sockaddr_v4_t;

typedef enum {
    RET_STATE_VALUE_ERROR, RET_STATE_VALUE_READY, RET_STATE_VALUE_IN_PROGRESS
} kaa_function_return_state_t;

typedef enum {
    KAA_TCP_SOCK_ERROR, KAA_TCP_SOCK_CONNECTING, KAA_TCP_SOCK_CONNECTED
} kaa_tcp_socket_state_t;

typedef enum {
    KAA_TCP_SOCK_IO_OK, KAA_TCP_SOCK_IO_EOF, KAA_TCP_SOCK_IO_ERROR
} kaa_tcp_socket_io_errors_t;

#define NTOHL(V) ntohl(V)

typedef kaa_error_t (*kaa_tcp_utils_value_complete_fn)(void *context);

/**
 * Hostname DNS resolving helper function
 * @return kaa_function_return_state_t enum,
 *          RET_STATE_VALUE_READY - mean that ip_v4 value is ready and may be used.
 *          RET_STATE_VALUE_IN_PROGRESS - mean that DNS name resolving in progress
 *          and need to wait until hostresolved_callback is invoked.
 *          RET_STATE_VALUE_ERROR - mean error.
 */
kaa_function_return_state_t kaa_tcp_utils_gethostbyaddr_v4(void *context, kaa_tcp_utils_value_complete_fn hostresolved_callback, uint32_t * ip_v4, const char * hostname, size_t hostname_size);


/**
 * Create and open non blocking TCP V4 connection.
 */
kaa_error_t kaa_tcp_utils_open_v4_tcp_socket(kaa_fd * fd, kaa_sockaddr_v4_t * destination);

/**
 * Check state of connecting TCP V4 socket
 * @return kaa_tcp_socket_state_t
 *        KAA_TCP_SOCK_ERROR - error occurred with socket, connection failed.
 *        KAA_TCP_SOCK_CONNECTING - still connecting
 *        KAA_TCP_SOCK_CONNECTED - connect successful.
 */
kaa_tcp_socket_state_t kaa_tcp_utils_v4_tcp_socket_check(kaa_fd fd);


/**
 * Write bytes from buffer to TCP socket
 * @param [in] char * buffer, start of bytes to write
 * @param [in] size_t buffer_size, number of bytes ready to write
 * @param [out] size_t * bytes_written, number of bytes written from buffer
 * @return kaa_tcp_socket_io_errors_t
 *       KAA_TCP_SOCK_IO_OK - write successful.
 *       KAA_TCP_SOCK_IO_ERROR - write failed, socket write return -1
 */
kaa_tcp_socket_io_errors_t kaa_tcp_utils_v4_tcp_socket_write(kaa_fd fd, const char * buffer, size_t buffer_size, size_t * bytes_written);


/**
 * Read bytes from TCP socket to buffer
 * @param [in] char * buffer, start of bytes for read
 * @param [in] size_t buffer_size, number of bytes which socket may read
 * @param [out] size_t * bytes_read, number of bytes realy read from socket
 * @return kaa_tcp_socket_io_errors_t
 *       KAA_TCP_SOCK_IO_OK - read successful.
 *       KAA_TCP_SOCK_IO_EOF - read return 0 bytes read, mostly mean FIN/ACK received.
 *       KAA_TCP_SOCK_IO_ERROR - read failed, socket read return -1
 */
kaa_tcp_socket_io_errors_t kaa_tcp_utils_v4_tcp_socket_read(kaa_fd fd, const char * buffer, size_t buffer_size, size_t * bytes_read);

/**
 * Close socket.
 */
kaa_error_t kaa_tcp_utils_v4_tcp_socket_close(kaa_fd fd);

#endif /* EXT_TCP_UTILS_H_ */
