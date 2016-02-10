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
 * esp8266.h
 *
 *  Created on: Mar 18, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef LIBRARIES_ESP8266_ESP8266_H_
#define LIBRARIES_ESP8266_ESP8266_H_

#include "esp8266_serial.h"

#ifdef __cplusplus
extern "C" {
#endif

/** Maximum size of bytes which can be send by one call */
#define TCP_SEND_MAX_BUFFER 2048

struct esp8266_t;
typedef struct esp8266_t esp8266_t;

typedef enum {
	ESP8266_ERR_NONE = 0,
	ESP8266_ERR_NOMEM = -1,
	ESP8266_ERR_BAD_PARAM = -2,
	ESP8266_ERR_COMMAND_BUSY = -3,
	ESP8266_ERR_INIT_FAILED = -4,
	ESP8266_ERR_COMMAND_ERROR = -5,
	ESP8266_ERR_COMMAND_TIMEOUT = -6,
	ESP8266_ERR_NOT_CONNECTED = -7,
	ESP8266_ERR_TCP_CONN_LIMIT = -8,
	ESP8266_ERR_TCP_CONN_DNS = -9,
	ESP8266_ERR_TCP_CONN_UNCONNECTED = -10,
	ESP8266_ERR_TCP_SEND_TIMEOUT = -11,
	ESP8266_ERR_TCP_SEND_FAILED = -12,
	ESP8266_ERR_TCP_SEND_BUFFER_TO_BIG = -13,
	ESP8266_ERR_IPD_PROTO = -14,
} esp8266_error_t;

typedef enum {
	ESP8266_WIFI_AP_OPEN = 0,
	ESP8266_WIFI_AP_WEP = 1,
	ESP8266_WIFI_AP_WPA_PSK = 2,
	ESP8266_WIFI_AP_WPA2_PSK = 3,
	ESP8266_WIFI_AP_WPA_WPA2_PSK = 4,
	ESP8266_WIFI_AP_UNKNOWN = 5,
} esp8266_wifi_ap_type_t;

typedef struct {
	esp8266_wifi_ap_type_t ap_type;
	char *ssid;
	uint32_t rssi;
	uint8_t mac[6];
	uint32_t channel;
} esp8266_wifi_ap_t;

typedef void (*on_esp8266_comand_complete_fn)(void *context
                                                , const bool result
												, const bool timeout_expired
												, const char* command
												, int index
                                                , time_t comand_complete_milis);

typedef void (*on_esp8266_tcp_receive_fn)(void *context
												, int id
												, const uint8 *buffer
												, const int receive_size);


esp8266_error_t esp8266_create(esp8266_t **controler, esp8266_serial_t *hw_serial, size_t rx_buffer_size);

void esp8266_destroy(esp8266_t *controler);

esp8266_error_t esp8266_send_command(esp8266_t *controler,
		on_esp8266_comand_complete_fn command_callback,
		void *callback_context,
		const char *command,
		const char *success, size_t success_size,
		const char *error, size_t error_size,
		time_t timeout_milis);

esp8266_error_t esp8266_init(esp8266_t *controler);

esp8266_error_t esp8266_echo(esp8266_t *controler, bool echo_enabled);

esp8266_error_t esp8266_scan(esp8266_t *controler, esp8266_wifi_ap_t **ap_array, size_t *ap_array_size);

esp8266_error_t esp8266_process(esp8266_t *controler, time_t limit_timeout_milis);

esp8266_error_t esp8266_connect_wifi(esp8266_t *controler, const char *SSID, const char *pwd);

esp8266_error_t esp8266_mux_mod(esp8266_t *controler, bool mux_mod_enabled);

esp8266_error_t esp8266_disconnect_wifi(esp8266_t *controler);

esp8266_error_t esp8266_connect_tcp(esp8266_t *controler, const char* hostname, const size_t hostname_size, const uint16_t port, int *id);

esp8266_error_t esp8266_send_tcp(esp8266_t *controler, int id, const uint8* buffer, const size_t size);

esp8266_error_t esp8266_tcp_register_receive_callback(esp8266_t *controler, on_esp8266_tcp_receive_fn receive_callback, void *receive_context);

esp8266_error_t esp8266_disconnect_tcp(esp8266_t *controler, int id);

esp8266_error_t esp8266_check_status(esp8266_t *controler);

/*
 * Standard error handling macros
 */
#define ESP8266_RETURN_IF_ERR(E) \
    { if (E) return E; }

#define ESP8266_RETURN_IF_NIL(p, E) \
    { if (!(p)) return E; }

#define ESP8266_RETURN_IF_NIL2(p1, p2, E) \
    { if (!(p1) || !(p2)) return E; }

#define ESP8266_RETURN_IF_NIL3(p1, p2, p3, E) \
    { if (!(p1) || !(p2) || !(p3)) return E; }

#define ESP8266_RETURN_IF_NIL4(p1, p2, p3, p4, E) \
    { if (!(p1) || !(p2) || !(p3) || !(p4)) return E; }

#define ESP8266_RETURN_IF_NIL5(p1, p2, p3, p4, p5,E) \
    { if (!(p1) || !(p2) || !(p3) || !(p4) || !(p5)) return E; }

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* LIBRARIES_ESP8266_ESP8266_H_ */
