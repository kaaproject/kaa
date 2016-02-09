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
 * @file esp8266.c
 *
 *  Created on: Mar 19, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

typedef unsigned char uint8;
typedef unsigned int uint32;

#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "esp8266.h"
#include "chip_specififc.h"

#define AT_RST_TIMEOUT 5000
#define AT_CWLAP_TIMEOUT 15000
#define AT_CWJAP_TIMEOUT 30000
#define AT_CONN_TIMEOUT 10000
#define AT_GENERIC_TIMEOUT 1000
#define AT_CIPSEND_TIMEOUT 5000
#define AT_IPD_TIMEOUT 5000

#define TMP_ARRAY_SIZE 16
#define COMM_BUFFER_SIZE 64

#define TCP_CONNECTION_LIMIT 5

#define DEFAULT_STATUS_CHECK_TIMEOUT 2000

#define SIZEOF_COMMAND(command) (sizeof(command)-1)

/* __FLASH__ define used to pack constant string into flash memory */
#define __attr_flash __attribute__((section (".USER_FLASH")))
#define __FLASH__ __attr_flash

/* AT commands and success/error responses - ESP8266 firmware ver.0020000903 */

static const char AT_RST[]                      __FLASH__ = "AT+RST";
static const char AT_RST_SUCCESS[]              __FLASH__ = "ready";
static const char AT_CWLAP[]                    __FLASH__ = "AT+CWLAP";
static const char AT_CWLAP_RESP_LINE_START[]    __FLASH__ = "+CWLAP:";
static const char AT_SUCCESS_OK[]               __FLASH__ = "\r\nOK";
static const char AT_SUCCESS_OK1[]              __FLASH__ = "OK";
static const char AT_ERROR[]                    __FLASH__ = "\r\nERROR";
static const char AT_FAIL[]                     __FLASH__ = "\r\nFAIL";
static const char AT_ECHO_ON[]                  __FLASH__ = "ATE1";
static const char AT_ECHO_OFF[]                 __FLASH__ = "ATE0";
static const char AT_CIPMUX[]                   __FLASH__ = "AT+CIPMUX";
static const char AT_CWJAP[]                    __FLASH__ = "AT+CWJAP";
static const char AT_CWQAP[]                    __FLASH__ = "AT+CWQAP";
static const char AT_CIPSTART[]                 __FLASH__ = "AT+CIPSTART";
static const char AT_OK_LINKED[]                __FLASH__ = "CONNECT\r\n\r\nOK";
static const char AT_DNS_FAIL[]                 __FLASH__ = "DNS Fail";
static const char AT_CIPCLOSE[]                 __FLASH__ = "AT+CIPCLOSE";
static const char AT_UNLINKED[]                 __FLASH__ = "CLOSED\r\n\r\nOK";
static const char AT_LINK_IS_NOT[]              __FLASH__ = "link is not";
static const char AT_CIPSEND[]                  __FLASH__ = "AT+CIPSEND";
static const char AT_CIPSEND_TERM               __FLASH__ = '>';
static const char AT_SEND_CONFIRM[]             __FLASH__ = " \r\nSEND OK";
static const char AT_SEND_IPD[]                 __FLASH__ = "+IPD";
static const char AT_SEND_IPD_OK[]              __FLASH__ = "\r\nOK\r\n";
static const char AT_CIPSTATUS[]                __FLASH__ = "AT+CIPSTATUS";
static const char AT_STATUS_N[]                 __FLASH__ = "STATUS:";
static const char AT_STATUS_CON[]               __FLASH__ = "+CIPSTATUS:";


typedef struct {
	void *callback_context;
	on_esp8266_comand_complete_fn command_callback;
	time_t command_start_milis;
	time_t timeout_milis;
	char *command;
	char *success;
	size_t success_size;
	char *error;
	size_t error_size;
	char *error_alternative;
	size_t error_alternative_size;
	int index;
} command_callback_t;


typedef enum {
	ESP8266_STATE_UNDEF = 0,
	ESP8266_STATE_INITILIZING = 1,
	ESP8266_STATE_INIT_OK = 2,
	ESP8266_STATE_INIT_FAILED = 3,
	ESP8266_STATE_SCANNING = 4,
	ESP8266_STATE_CONNECTED = 5,
} esp8266_state_t;

typedef enum {
	ESP8266_TCP_CONN_UNUSED = 0,
	ESP8266_TCP_CONN_CONNECTING = 1,
	ESP8266_TCP_CONN_CONNECTED = 2,
	ESP8266_TCP_CONN_DNS_FAILD = 3,
	ESP8266_TCP_CONN_ERROR = 4,
} esp8266_tcp_conn_state_t;

typedef enum {
	ESP8266_COMMAND_UNDEF = 0,
	ESP8266_COMMAND_RESPONCE_WAIT = 1,
	ESP8266_COMMAND_RESPONCE_FAIL = 2,
	ESP8266_COMMAND_RESPONCE_TIMEOUT = 3,
	ESP8266_COMMAND_RESPONCE_OK = 4,
} esp8266_command_state_t;

typedef enum {
	ESP8266_IPD_UNDEF = 0,
	ESP8266_IPD_START = 1,
	ESP8266_IPD_WAIT_CH = 2,
	ESP8266_IPD_WAIT_SIZE = 3,
	ESP8266_IPD_READ_BYTES = 4,
	ESP8266_IPD_WAIT_FINISH = 5,
} esp8266_ipd_read_state_t;

struct esp8266_t {
	esp8266_serial_t *esp8266_serial;
	unsigned char *rx_buffer;
	size_t rx_buffer_size;
	uint32_t rx_pointer;
	command_callback_t current_command;
	esp8266_state_t state;
	esp8266_command_state_t command_state;;
	esp8266_wifi_ap_t *ap_list;
	size_t ap_list_size;
	esp8266_tcp_conn_state_t tcp_id[TCP_CONNECTION_LIMIT];
	esp8266_ipd_read_state_t ipd; //if other than UNDEF, reading (+IPD:)
	size_t ipd_counter; //number of bytes which waiting for read
	int ipd_conn; //connection number
	uint32_t ipd_start; //start position in rx_buffer of incoming bytes
	on_esp8266_tcp_receive_fn receive_callback;
	void *receive_context;
	uint32_t status_check_timeout;
	uint32_t last_status_check;

	//Some reserved for calculation values, which used instead of stack memory
	uint16_t tmp_array[TMP_ARRAY_SIZE];
	uint16_t tmp_array_used;
	//Used for commands
	char *com_buffer;
};

inline int get_channel(uint8 read_byte)
{
	if (read_byte == '0') {
		return 0;
	} else if (read_byte == '1') {
		return 1;
	} else if (read_byte == '2') {
		return 2;
	} else if (read_byte == '3') {
		return 3;
	} else if (read_byte == '4') {
		return 4;
	}
	return -1;
}

int get_next_tcp_id(esp8266_t *controler)
{
	ESP8266_RETURN_IF_NIL(controler, -1);

	int id = 0;
	for (; id < TCP_CONNECTION_LIMIT; id++) {
		if (controler->tcp_id[id] == ESP8266_TCP_CONN_UNUSED) {
			return id;
		}
	}
	return -1;
}

bool is_have_active_connections(esp8266_t *controler)
{
    ESP8266_RETURN_IF_NIL(controler, false);

    int id = 0;
    for (; id < TCP_CONNECTION_LIMIT; id++) {
        if (controler->tcp_id[id] == ESP8266_TCP_CONN_CONNECTED) {
            return true;
        }
    }
    return false;
}

void clear_current_command(esp8266_t *controler)
{
	if(!controler)
		return;

	memset(&controler->current_command, 0, sizeof(command_callback_t));
	controler->rx_pointer = 0;
	memset(controler->rx_buffer, 0, controler->rx_buffer_size);

	controler->last_status_check = get_sys_milis();
}

int indexOf(const char *buffer, const uint32_t buffer_size, const char *sequence, const uint32_t sequence_size)
{
	ESP8266_RETURN_IF_NIL4(buffer, buffer_size, sequence, sequence_size, -1);
	if (sequence_size > buffer_size) {
		return -1;
	}
	uint32_t offset = 0;
	for(;offset <= (buffer_size - sequence_size);offset++) {
		if (buffer[offset] == sequence[0]) {
			if (sequence_size == 1) {
				return offset;
			}
			if (strncmp(buffer + offset, sequence, sequence_size) == 0) {
				return offset;
			}
		}
	}
	return -1;
}

bool find_chars(const char *buffer, uint32_t buffer_size, const char *sequence, uint32_t sequence_size, int *index_p)
{
	if (sequence_size > buffer_size) {
		return false;
	}
	uint32_t offset = buffer_size - sequence_size;
	for(;offset > 0; offset--) {
		if (buffer[offset] == sequence[0]) {
			if (sequence_size == 1) {
				if (index_p)
					*index_p = offset;
				return true;
			}
			if (strncmp(buffer + offset, sequence, sequence_size) == 0) {
				if (index_p)
					*index_p = offset;
				return true;
			}
		}
	}

	return false;
}

esp8266_error_t esp8266_create(esp8266_t **controler, esp8266_serial_t *hw_serial, size_t rx_buffer_size)
{
	ESP8266_RETURN_IF_NIL3(controler, hw_serial, rx_buffer_size, ESP8266_ERR_BAD_PARAM);

	esp8266_t *self = (esp8266_t *)calloc(1, sizeof(esp8266_t));
	if (!self)
		return ESP8266_ERR_NOMEM;

	self->esp8266_serial = hw_serial;
	self->rx_buffer = (unsigned char *)malloc(rx_buffer_size);
	if (!self->rx_buffer) {
		esp8266_destroy(self);
		return ESP8266_ERR_NOMEM;
	}

	self->rx_buffer_size = rx_buffer_size;

	self->com_buffer = malloc(COMM_BUFFER_SIZE);
	if (!self->com_buffer) {
		esp8266_destroy(self);
		return ESP8266_ERR_NOMEM;
	}

	self->status_check_timeout = DEFAULT_STATUS_CHECK_TIMEOUT;
	self->last_status_check = get_sys_milis();

	*controler = self;
	return ESP8266_ERR_NONE;
}

void esp8266_destroy(esp8266_t *controler)
{
	if (!controler)
		return;

	if (controler->rx_buffer) {
		free(controler->rx_buffer);
		controler->rx_buffer = NULL;
	}

	if (controler->com_buffer) {
		free(controler->com_buffer);
		controler->com_buffer = NULL;
	}

	controler->esp8266_serial = NULL;

	free(controler);
}



esp8266_error_t esp8266_send_command_al(esp8266_t *controler,
		on_esp8266_comand_complete_fn command_callback,
		void *callback_context,
		const char *command,
		const char *success, size_t success_size,
		const char *error, size_t error_size,
		const char *error_alternative, size_t error_alternative_size,
		time_t timeout_milis)
{
	ESP8266_RETURN_IF_NIL2(controler, command, ESP8266_ERR_BAD_PARAM);

	if (controler->current_command.command) {
		return ESP8266_ERR_COMMAND_BUSY;
	}

	if (controler->ipd != ESP8266_IPD_UNDEF)
	    return ESP8266_ERR_COMMAND_BUSY;

	controler->current_command.command = (char *)command;
	controler->current_command.callback_context = callback_context;
	controler->current_command.command_callback = command_callback;
	controler->current_command.command_start_milis = get_sys_milis();
	controler->current_command.error = (char *)error;
	controler->current_command.error_size = error_size;
	controler->current_command.error_alternative = (char *)error_alternative;
	controler->current_command.error_alternative_size = error_alternative_size;
	controler->current_command.success = (char *)success;
	controler->current_command.success_size = success_size;
	controler->current_command.timeout_milis = timeout_milis;
	controler->current_command.index = -1;

	controler->rx_pointer = 0;
	esp8266_serial_write_command(controler->esp8266_serial, command);
	return ESP8266_ERR_NONE;
}

esp8266_error_t esp8266_send_command(esp8266_t *controler,
		on_esp8266_comand_complete_fn command_callback,
		void *callback_context,
		const char *command,
		const char *success, size_t success_size,
		const char *error, size_t error_size,
		time_t timeout_milis)
{
	return esp8266_send_command_al(controler
			, command_callback
			, callback_context
			, command
			, success, success_size
			, error, error_size
			, NULL, 0
			, timeout_milis);
}




#define CHECK_SEQUENCE(sequence, sequence_size, command_result) \
		if (sequence && sequence_size) { \
			if (find_chars(controler->rx_buffer, \
					controler->rx_pointer, \
					sequence, \
					sequence_size, \
					&controler->current_command.index)) { \
				time_t command_processing_time = get_sys_milis() - controler->current_command.command_start_milis; \
				if (controler->current_command.command_callback) { \
					controler->current_command.command_callback(controler->current_command.callback_context, \
							command_result,  \
							false, \
							controler->current_command.command, \
							controler->current_command.index, \
							command_processing_time); \
				} \
				clear_current_command(controler); \
			} \
		}

esp8266_error_t esp8266_ipd_process(esp8266_t *controler, uint8 read_byte)
{
    ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

    switch (controler->ipd) {
        case ESP8266_IPD_UNDEF:
            if (is_have_active_connections(controler)) {
                if (read_byte == AT_SEND_IPD[0]) {
                    controler->ipd = ESP8266_IPD_START;
                    controler->ipd_start = controler->rx_pointer - 1;
                }
            }
            break;
        case ESP8266_IPD_START:
            if ((controler->rx_pointer - controler->ipd_start) >= SIZEOF_COMMAND(AT_SEND_IPD)) {
                if (strncmp(controler->rx_buffer + controler->ipd_start, AT_SEND_IPD, SIZEOF_COMMAND(AT_SEND_IPD)) == 0) {
                    controler->ipd = ESP8266_IPD_WAIT_CH;
                    controler->rx_pointer = 0;
                } else {
                    controler->rx_buffer[controler->rx_pointer] = '\0';
                    debug("IPD PROTO ERROR:%d:%d:%s:\r\n",
                            controler->rx_pointer,
                            controler->ipd_start,
                            controler->rx_buffer + controler->ipd_start);
                    controler->ipd = ESP8266_IPD_UNDEF;
                }

            }
            break;
        case ESP8266_IPD_WAIT_CH:
            //Found in rx_buffer +IPD and position point to '+', rx_pointer point to unread next to 'D'
            //Format +IPD,CH,SIZE: CH - one number from 0 to 4
            if (read_byte == ',') {
                //wait next char
            } else {
                controler->ipd_conn = get_channel(read_byte);
                if (controler->ipd_conn >= 0 && controler->ipd_conn < TCP_CONNECTION_LIMIT) {
                    controler->ipd = ESP8266_IPD_WAIT_SIZE;
                    controler->ipd_start = 0;
                } else {
                    //Proto error
                    controler->ipd = ESP8266_IPD_UNDEF;
                    return ESP8266_ERR_IPD_PROTO;
                }
            }
            break;
        case ESP8266_IPD_WAIT_SIZE:
            //Channel number is read, and now rx_pointer - 1 points to ',' before size
            //Store pointer and read until ':', then transform to digits.
            if (controler->ipd_start == 0)
                controler->ipd_start = controler->rx_pointer;
            if (read_byte == ':') {
                //Now buffer between controler->ipd_start and controler->rx_pointer-1 contains size
                int l = controler->rx_pointer - controler->ipd_start;
                memcpy(controler->com_buffer,
                        controler->rx_buffer + controler->ipd_start,
                        l);

                controler->com_buffer[l] = '\0';
                controler->ipd_counter = atoi(controler->com_buffer);
                if (controler->ipd_counter > 0) {
                    controler->ipd = ESP8266_IPD_READ_BYTES;
                    controler->rx_pointer = 0;
                    controler->ipd_start = 0;
                } else {
                    //Proro error
                    controler->ipd = ESP8266_IPD_UNDEF;
                    return ESP8266_ERR_IPD_PROTO;
                }
            }
            break;
        case ESP8266_IPD_READ_BYTES:
            controler->ipd_counter--;
            if (controler->ipd_counter <= 0) {
                //Push received buffer
                if (controler->receive_callback) {
                    controler->receive_callback(controler->receive_context,
                            controler->ipd_conn,
                            (controler->rx_buffer + controler->ipd_start),
                            (controler->rx_pointer - controler->ipd_start));
                }
                //reset rx_pointer
                controler->rx_pointer = 0;
                //switch off ipd
                controler->ipd_counter = 0;
                controler->ipd_start = 0;
                controler->ipd = ESP8266_IPD_WAIT_FINISH;
            }
            break;
        case ESP8266_IPD_WAIT_FINISH:
            if (controler->rx_pointer >= SIZEOF_COMMAND(AT_SEND_IPD_OK)) {
                if (strncmp(controler->rx_buffer, AT_SEND_IPD_OK, SIZEOF_COMMAND(AT_SEND_IPD_OK)) == 0) {
                    controler->ipd = ESP8266_IPD_UNDEF;
                    controler->last_status_check = get_sys_milis();
                    controler->rx_pointer = 0;
                } else {
                    controler->ipd = ESP8266_IPD_UNDEF;
                    return ESP8266_ERR_IPD_PROTO;
                }
            }
            break;
    }
    return ESP8266_ERR_NONE;
}


esp8266_error_t esp8266_process(esp8266_t *controler, time_t limit_timeout_milis)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	time_t start = 0;
	if (limit_timeout_milis > 0) {
		start = get_sys_milis();
	}

	esp8266_error_t e = esp8266_check_status(controler);
	if (e)
		return e;

	int position = 0;
	uint8 read_byte = 0;
	while (esp8266_serial_available(controler->esp8266_serial)) {
		read_byte = esp8266_serial_read(controler->esp8266_serial);
		controler->rx_buffer[controler->rx_pointer++] = read_byte;


		if (controler->ipd == ESP8266_IPD_UNDEF
		        && controler->current_command.command) {
            CHECK_SEQUENCE(controler->current_command.success, controler->current_command.success_size, true);
            CHECK_SEQUENCE(controler->current_command.error, controler->current_command.error_size, false);
            CHECK_SEQUENCE(controler->current_command.error_alternative, controler->current_command.error_alternative_size, false);
        } else {
            e = esp8266_ipd_process(controler, read_byte);
            if (e)
                return e;
        }

//
//		if (controler->ipd == ESP8266_IPD_READ_BYTES) {
//
//			controler->ipd_counter--;
//			if (controler->ipd_counter <= 0) {
//				//Push received buffer
//			    //debug("IPD Finish\r\n");
//				if (controler->receive_callback) {
//					controler->receive_callback(controler->receive_context,
//							controler->ipd_conn,
//							(controler->rx_buffer + controler->ipd_start),
//							(controler->rx_pointer - controler->ipd_start));
//				}
//				//reset rx_pointer
//				controler->rx_pointer = 0;
//				//switch off ipd
//				controler->ipd = false;
//				//controler->ipd_conn = -1;
//				controler->ipd_counter = 0;
//				controler->ipd_start = 0;
//				controler->ipd = ESP8266_IPD_WAIT_FINISH;
//			}
//		} else if (controler->ipd == ESP8266_IPD_WAIT_FINISH) {
//			if (controler->rx_pointer >= SIZEOF_COMMAND(AT_SEND_IPD_OK)) {
//				int sh = controler->rx_pointer - SIZEOF_COMMAND(AT_SEND_IPD_OK);
//				if (sh >= 0 && strncmp(controler->rx_buffer+sh, AT_SEND_IPD_OK, SIZEOF_COMMAND(AT_SEND_IPD_OK)) == 0) {
//
//					ipd_command_complete(controler,true,false,AT_SEND_IPD,0,0);
//				}
//				sh = controler->rx_pointer - SIZEOF_COMMAND(AT_SEND_IPD_OK_UNLINK);
//				if (sh >= 0 && strncmp(controler->rx_buffer+sh, AT_SEND_IPD_OK_UNLINK, SIZEOF_COMMAND(AT_SEND_IPD_OK_UNLINK)) == 0) {
//					ipd_command_complete(controler,true,false,AT_SEND_IPD,0,0);
//					if (controler->receive_callback) {
//						controler->receive_callback(controler->receive_context,controler->ipd_conn,0,-1); //Indicate connection close
//					}
//				}
//
//			}
//
//		} else if (controler->current_command.command) {
//			CHECK_SEQUENCE(controler->current_command.success, controler->current_command.success_size, true);
//			CHECK_SEQUENCE(controler->current_command.error, controler->current_command.error_size, false);
//			CHECK_SEQUENCE(controler->current_command.error_alternative, controler->current_command.error_alternative_size, false);
//		} else if (controler->ipd == ESP8266_IPD_UNDEF) {
//			//Check if IPD received
//			if (read_byte == '+') {
//				controler->ipd = ESP8266_IPD_START;
//				controler->rx_pointer = 0;
//				//debug("IPD start\r\n");
//			}
//		} else if (controler->ipd == ESP8266_IPD_START) {
//			if (controler->rx_pointer >= 4) {
//				debug("PROTO ERROR\r\n");
//				controler->ipd = ESP8266_IPD_UNDEF;
//			}
//			if (read_byte == 'D') {
//				controler->ipd = ESP8266_IPD_WAIT_CH;
//			}
//		} else if (controler->ipd == ESP8266_IPD_WAIT_CH) {
//			//Found in rx_buffer +IPD and position point to '+', rx_pointer point to unread next to 'D'
//			//Format +IPD,CH,SIZE: CH - one number from 0 to 4
//			if (read_byte == ',') {
//				//wait next char
//			} else {
//				controler->ipd_conn = get_channel(read_byte);
//				if (controler->ipd_conn >= 0 && controler->ipd_conn < TCP_CONNECTION_LIMIT) {
//					controler->ipd = ESP8266_IPD_WAIT_SIZE;
//				} else {
//					//Proto error
//					debug("PROTO ERROR 1, %d : \'%c\'\r\n", controler->ipd_conn, read_byte);
//					controler->ipd = ESP8266_IPD_UNDEF;
//				}
//			}
//		} else if (controler->ipd == ESP8266_IPD_WAIT_SIZE) {
//			//Channel number is read, and now rx_pointer - 1 points to ',' before size
//			//Store pointer and read until ':', then transform to digits.
//			if (controler->ipd_start == 0)
//				controler->ipd_start = controler->rx_pointer;
//			if (read_byte == ':') {
//				//Now buffer between controler->ipd_start and controler->rx_pointer-1 contains size
//				int l = controler->rx_pointer - controler->ipd_start;
//				memcpy(controler->com_buffer,
//						controler->rx_buffer + controler->ipd_start,
//						l);
//
//				controler->com_buffer[l] = '\0';
//				controler->ipd_counter = atoi(controler->com_buffer);
//				controler->ipd = ESP8266_IPD_READ_BYTES;
//				controler->rx_pointer = 0;
//				controler->ipd_start = 0;
//			}
//		}

		if (controler->rx_pointer >= controler->rx_buffer_size) {
			if (controler->ipd == ESP8266_IPD_READ_BYTES) {
				//Push existing bytes, and start from begin
				if (controler->receive_callback) {
					controler->receive_callback(controler->receive_context,
							controler->ipd_conn,
							(controler->rx_buffer + controler->ipd_start),
							(controler->rx_pointer - controler->ipd_start));
				}
				controler->ipd_start = 0;
				controler->rx_pointer = 0;
			} else if (controler->current_command.command) {
				//If we still wait response, need to copy last max(error_size,success_size) to begin of buffer
				//and rollover rx buffer pointer
				size_t m = get_sys_max(controler->current_command.success_size, controler->current_command.error_size);
				if (m < (controler->rx_buffer_size / 2)) {
					//Support block mem copy
					memcpy(controler->rx_buffer, (controler->rx_buffer + (controler->rx_buffer_size - m)), m);
				} else {
					//need byte by byte copy
					size_t i=0;
					uint32_t cp_s = controler->rx_buffer_size - m;
					for(;i<m;i++) {
						controler->rx_buffer[i] = controler->rx_buffer[cp_s + i];
					}
				}
				controler->rx_pointer = m;
			} else {
				//rollover rx buffer pointer from start
				controler->rx_pointer = 0;
			}
		}
		//if we out of timeout limit, don't read to end of available bytes
		//'0' as limit mean unlimited
		if (limit_timeout_milis > 0) {
			time_t processing_time = get_sys_milis() - start;
			if (processing_time >= limit_timeout_milis) {
				break;
			}
		}
	}
	//Check timeout if command enabled, command checked independently from previous check, because command may be reset
	if (controler->current_command.command) {
		time_t command_processing_time = get_sys_milis() - controler->current_command.command_start_milis;
		if ( command_processing_time >= controler->current_command.timeout_milis) {
			//Timeout fired
			if (controler->current_command.command_callback) {
				controler->current_command.command_callback(controler->current_command.callback_context,
						false,
						true,
						controler->current_command.command,
						-1,
						command_processing_time);
			}
			clear_current_command(controler);
		}
	}

	return ESP8266_ERR_NONE;
}


void init_command_complete(void *context
								, const bool result
								, const bool timeout_expired
								, const char* command
								, int index
								, time_t comand_complete_milis)
{
	if (!context)
		return;

	esp8266_t *controler = (esp8266_t *)context;



	if (result) {
		controler->state = ESP8266_STATE_INIT_OK;
	} else {
		controler->state = ESP8266_STATE_INIT_FAILED;
	}
}


esp8266_error_t esp8266_init(esp8266_t *controler)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	esp8266_error_t error = esp8266_send_command(controler ,
							init_command_complete ,
							(void*)controler,
							AT_RST,
							AT_RST_SUCCESS,
							SIZEOF_COMMAND(AT_RST_SUCCESS),
							NULL,
							0,
							AT_RST_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->state = ESP8266_STATE_INITILIZING;

	while(controler->state == ESP8266_STATE_INITILIZING) {
		error = esp8266_process(controler, AT_RST_TIMEOUT*2);
		ESP8266_RETURN_IF_ERR(error);
	}

	if (controler->state == ESP8266_STATE_INIT_OK)
		return esp8266_echo(controler, false);
	else
		return ESP8266_ERR_INIT_FAILED;
}

bool fill_wifi_ap(esp8266_t *controler, esp8266_wifi_ap_t *wifi_ap, uint16_t start_pos)
{
	ESP8266_RETURN_IF_NIL2(controler, wifi_ap, false);

	unsigned char *buffer = controler->rx_buffer;
	int buffer_length = controler->rx_buffer_size - start_pos;
	buffer += start_pos;
	int type_pos = indexOf(buffer, buffer_length, ",",1);
	if (type_pos < 0) {
		return false;
	}
	type_pos += buffer - controler->rx_buffer;
	buffer = controler->rx_buffer + type_pos + 1;
	buffer_length = controler->rx_buffer_size - type_pos -1;
	int rssi_pos = indexOf(buffer, buffer_length, ",",1);
	if (rssi_pos < 0) {
		return false;
	}
	rssi_pos += buffer - controler->rx_buffer;
	buffer = controler->rx_buffer + rssi_pos + 1;
	buffer_length = controler->rx_buffer_size - rssi_pos -1;
	int ch_pos = indexOf(buffer, buffer_length, ",",1);
	if(ch_pos < 0) {
		return false;
	}
	ch_pos += buffer - controler->rx_buffer;

	unsigned char type_char = controler->rx_buffer[type_pos - 1];
	if (type_char == '0') {
		wifi_ap->ap_type = ESP8266_WIFI_AP_OPEN;
	} else if (type_char == '1') {
		wifi_ap->ap_type = ESP8266_WIFI_AP_WEP;
	} else if (type_char == '2') {
		wifi_ap->ap_type = ESP8266_WIFI_AP_WPA_PSK;
	} else if (type_char == '3') {
		wifi_ap->ap_type = ESP8266_WIFI_AP_WPA2_PSK;
	} else if (type_char == '4') {
		wifi_ap->ap_type = ESP8266_WIFI_AP_WPA_WPA2_PSK;
	} else {
		wifi_ap->ap_type = ESP8266_WIFI_AP_UNKNOWN;
	}

	int ssid_length = rssi_pos - type_pos - 3;
	if (ssid_length > 0) {
		wifi_ap->ssid = strndup(controler->rx_buffer + type_pos + 2, ssid_length);
	}

	return true;
}

void scan_command_complete(void *context
								, const bool result
								, const bool timeout_expired
								, const char* command
								, int end_offset
								, time_t comand_complete_milis)
{
	if (!context)
		return;

	esp8266_t *controler = (esp8266_t *)context;
	/*
	 * Scan example:
	 * AT+CWLAP
	 * +CWLAP:(4,"Genesis(SF2)",-89,"b0:48:7a:f8:71:e0",1)
	 * +CWLAP:(3,"cyber1",-90,"cc:5d:4e:98:31:8c",2)
	 * +CWLAP:(4,"VODA2",-91,"90:f6:52:41:23:06",4)
	 * +CWLAP:(4,"cyber_dev1",-71,"64:70:02:f8:df:b4",5)
	 * +CWLAP:(3,"cyber9",-50,"ac:22:0b:88:3e:00",6)
	 * +CWLAP:(3,"cyber6_1",-81,"ac:22:0b:88:40:50",6)
	 * +CWLAP:(4,"Econais",-40,"84:c9:b2:5c:ef:84",11)
	 *
	 * OK
	 *
	 * index points to 'O'K
	 */

	unsigned char *buffer = controler->rx_buffer;
	int buffer_end = end_offset;
	int line_start = 0;
	line_start = indexOf(buffer,buffer_end,AT_CWLAP_RESP_LINE_START, SIZEOF_COMMAND(AT_CWLAP_RESP_LINE_START));
	controler->tmp_array_used = 0;
	while(line_start >= 0) {
		buffer += line_start + 1;
		buffer_end -= line_start + 1;
		controler->tmp_array[controler->tmp_array_used++] = buffer - controler->rx_buffer;
		if (controler->tmp_array_used >= TMP_ARRAY_SIZE) {
			break;
		}
		if (buffer_end > 0) {
			line_start = indexOf(buffer,buffer_end,AT_CWLAP_RESP_LINE_START, SIZEOF_COMMAND(AT_CWLAP_RESP_LINE_START));
		} else {
			line_start = -1;
		}
	}
	controler->state = ESP8266_STATE_INIT_OK;

	if (controler->tmp_array_used == 0) {
		return;
	}

	//TODO reuse existing array
	if (controler->ap_list) {
		free(controler->ap_list);
	}

	controler->ap_list = (esp8266_wifi_ap_t*)calloc(controler->tmp_array_used, sizeof(esp8266_wifi_ap_t));

	int i,j=0;
	for (i = 0; i < controler->tmp_array_used; i++) {
		if(fill_wifi_ap(controler, &controler->ap_list[j], controler->tmp_array[i])) {
			j++;
		} else {
			print_msg("Fill: ");
			print_numb(i);
			print_msg(" failed");
			println();
		}
	}
	controler->ap_list_size = j;

}

esp8266_error_t esp8266_scan(esp8266_t *controler, esp8266_wifi_ap_t **ap_array, size_t *ap_array_size)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	if (controler->state !=  ESP8266_STATE_INIT_OK)
		return ESP8266_ERR_COMMAND_BUSY;

	esp8266_error_t error = esp8266_send_command(controler,
								scan_command_complete,
								(void*)controler,
								AT_CWLAP,
								AT_SUCCESS_OK,
								SIZEOF_COMMAND(AT_SUCCESS_OK),
								AT_ERROR,
								SIZEOF_COMMAND(AT_ERROR),
								AT_CWLAP_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->state =  ESP8266_STATE_SCANNING;

	while(controler->state == ESP8266_STATE_SCANNING) {
		error = esp8266_process(controler, AT_CWLAP_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}

	if (ap_array && ap_array_size) {
		*ap_array = controler->ap_list;
		*ap_array_size = controler->ap_list_size;
	}

	controler->state =  ESP8266_STATE_INIT_OK;

	return ESP8266_ERR_NONE;
}


void generic_command_complete(void *context
								, const bool result
								, const bool timeout_expired
								, const char* command
								, int end_offset
								, time_t comand_complete_milis)
{
	if (!context)
		return;

	esp8266_t *controler = (esp8266_t *)context;

	if (result) {
		controler->command_state = ESP8266_COMMAND_RESPONCE_OK;
	} else {
		if (timeout_expired)
			controler->command_state = ESP8266_COMMAND_RESPONCE_TIMEOUT;
		else
			controler->command_state = ESP8266_COMMAND_RESPONCE_FAIL;

	}

}

esp8266_error_t esp8266_echo(esp8266_t *controler, bool echo_enabled)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	const char *echo = AT_ECHO_OFF;
	if (echo_enabled)
		echo = AT_ECHO_ON;

	esp8266_error_t error = esp8266_send_command(controler,
								generic_command_complete,
								(void*)controler,
								echo,
								AT_SUCCESS_OK,
								SIZEOF_COMMAND(AT_SUCCESS_OK),
								AT_ERROR,
								SIZEOF_COMMAND(AT_ERROR),
								AT_GENERIC_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_GENERIC_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}


	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}

esp8266_error_t esp8266_mux_mod(esp8266_t *controler, bool mux_mod_enabled)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	int n = 0;
	if (mux_mod_enabled) {
		n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=1", AT_CIPMUX);
	} else {
		n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=0", AT_CIPMUX);
	}

	controler->com_buffer[n+1] = 0;

	esp8266_error_t error = esp8266_send_command(controler,
								generic_command_complete,
								(void*)controler,
								controler->com_buffer,
								AT_SUCCESS_OK1,
								SIZEOF_COMMAND(AT_SUCCESS_OK1),
								AT_ERROR,
								SIZEOF_COMMAND(AT_ERROR),
								AT_GENERIC_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_GENERIC_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}


	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}



esp8266_error_t esp8266_connect_wifi(esp8266_t *controler, const char *ssid, const char *pwd)
{
	ESP8266_RETURN_IF_NIL3(controler, ssid, pwd, ESP8266_ERR_BAD_PARAM);

	int n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=\"%s\",\"%s\"", AT_CWJAP,ssid,pwd);
	controler->com_buffer[n+1] = 0;

	esp8266_error_t error = esp8266_send_command(controler,
								generic_command_complete,
								(void*)controler,
								controler->com_buffer,
								AT_SUCCESS_OK,
								SIZEOF_COMMAND(AT_SUCCESS_OK),
								AT_FAIL,
								SIZEOF_COMMAND(AT_FAIL),
								AT_CWJAP_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, (AT_CWJAP_TIMEOUT*2));
		ESP8266_RETURN_IF_ERR(error);
	}

	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			controler->state = ESP8266_STATE_CONNECTED;
			return esp8266_mux_mod(controler, true);
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}

esp8266_error_t esp8266_disconnect_wifi(esp8266_t *controler)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	if (controler->state != ESP8266_STATE_CONNECTED)
		return ESP8266_ERR_NOT_CONNECTED;

	esp8266_error_t error = esp8266_send_command(controler,
								generic_command_complete,
								(void*)controler,
								AT_CWQAP,
								AT_SUCCESS_OK1,
								SIZEOF_COMMAND(AT_SUCCESS_OK1),
								AT_FAIL,
								SIZEOF_COMMAND(AT_FAIL),
								AT_GENERIC_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_GENERIC_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}

	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			controler->state = ESP8266_STATE_CONNECTED;
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}


void tcp_connect_command_complete(void *context
								, const bool result
								, const bool timeout_expired
								, const char* command
								, int end_offset
								, time_t comand_complete_milis)
{
	if (!context)
		return;

	esp8266_t *controler = (esp8266_t *)context;

	//find which tcp connecting
	int id = 0;
	for (; id < TCP_CONNECTION_LIMIT; id++) {
		if (controler->tcp_id[id] == ESP8266_TCP_CONN_CONNECTING) {
			break;
		}
	}
	if (id >= TCP_CONNECTION_LIMIT) {
		//Error connection don't found
		controler->command_state = ESP8266_COMMAND_RESPONCE_FAIL;
		return;
	}


	if (result) {
		controler->command_state = ESP8266_COMMAND_RESPONCE_OK;
		controler->tcp_id[id] = ESP8266_TCP_CONN_CONNECTED;
	} else {
		if (timeout_expired) {
			controler->command_state = ESP8266_COMMAND_RESPONCE_TIMEOUT;
			controler->tcp_id[id] = ESP8266_TCP_CONN_ERROR;
		} else {
			controler->command_state = ESP8266_COMMAND_RESPONCE_FAIL;
			//find which failed
			if (controler->rx_buffer[end_offset] == AT_DNS_FAIL[0]) {
				controler->tcp_id[id] = ESP8266_TCP_CONN_DNS_FAILD;
			} else {
				controler->tcp_id[id] = ESP8266_TCP_CONN_ERROR;
			}
		}
	}
}

esp8266_error_t esp8266_connect_tcp(esp8266_t *controler, const char* hostname, const size_t hostname_size, const uint16_t port, int *id)
{
	ESP8266_RETURN_IF_NIL3(controler, hostname, id, ESP8266_ERR_BAD_PARAM);

	if (controler->state != ESP8266_STATE_CONNECTED)
		return ESP8266_ERR_NOT_CONNECTED;

	int w_id = get_next_tcp_id(controler);
	if (w_id == -1)
		return ESP8266_ERR_TCP_CONN_LIMIT;


	controler->tcp_id[w_id] = ESP8266_TCP_CONN_CONNECTING;

	char *hostname_str = calloc(1,hostname_size+1);
	if (!hostname_str)
	    return ESP8266_ERR_NOMEM;

	memcpy(hostname_str, hostname, hostname_size);

	int n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=%d\"TCP\",\"%s\",%d", AT_CIPSTART,w_id,hostname_str,port);
	controler->com_buffer[n+1] = 0;

	free(hostname_str);
	hostname_str = NULL;

	esp8266_error_t error = esp8266_send_command_al(controler,
								tcp_connect_command_complete,
								(void*)controler,
								controler->com_buffer,
								AT_OK_LINKED,
								SIZEOF_COMMAND(AT_OK_LINKED),
								AT_ERROR,
								SIZEOF_COMMAND(AT_ERROR),
								AT_DNS_FAIL,
								SIZEOF_COMMAND(AT_DNS_FAIL),
								AT_CONN_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_CONN_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}

	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			controler->state = ESP8266_STATE_CONNECTED;
			controler->tcp_id[w_id] = ESP8266_TCP_CONN_CONNECTED;
			*id = w_id;
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			controler->tcp_id[w_id] = ESP8266_TCP_CONN_UNUSED;
			*id = -1;
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			error = ESP8266_ERR_COMMAND_ERROR;
			if (controler->tcp_id[w_id] == ESP8266_TCP_CONN_DNS_FAILD) {
				error = ESP8266_ERR_TCP_CONN_DNS;
			}
			controler->tcp_id[w_id] = ESP8266_TCP_CONN_UNUSED;
			*id = -1;
			return error;
			break;
	}
}


esp8266_error_t esp8266_disconnect_tcp(esp8266_t *controler, int id)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	if (id < 0 || id >= TCP_CONNECTION_LIMIT)
		return ESP8266_ERR_TCP_CONN_LIMIT;

	if (controler->tcp_id[id] == ESP8266_TCP_CONN_UNUSED)
		return ESP8266_ERR_TCP_CONN_UNCONNECTED;


	int n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=%d", AT_CIPCLOSE,id);
	controler->com_buffer[n+1] = 0;

	esp8266_error_t error = esp8266_send_command_al(controler,
								generic_command_complete,
								(void*)controler,
								controler->com_buffer,
								AT_UNLINKED,
								SIZEOF_COMMAND(AT_UNLINKED),
								AT_LINK_IS_NOT,
								SIZEOF_COMMAND(AT_LINK_IS_NOT),
								AT_ERROR,
								SIZEOF_COMMAND(AT_ERROR),
								AT_GENERIC_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_GENERIC_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}

	controler->tcp_id[id] = ESP8266_TCP_CONN_UNUSED;

	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}

esp8266_error_t esp8266_send_tcp(esp8266_t *controler, int id, const uint8* buffer, const size_t size)
{
	ESP8266_RETURN_IF_NIL3(controler, buffer, size, ESP8266_ERR_BAD_PARAM);

	if (id < 0 || id >= TCP_CONNECTION_LIMIT)
		return ESP8266_ERR_TCP_CONN_LIMIT;

	if (controler->tcp_id[id] != ESP8266_TCP_CONN_CONNECTED)
		return ESP8266_ERR_TCP_CONN_UNCONNECTED;

	if (size > TCP_SEND_MAX_BUFFER)
		return ESP8266_ERR_TCP_SEND_BUFFER_TO_BIG;

	if (controler->ipd != ESP8266_IPD_UNDEF)
	    return ESP8266_ERR_COMMAND_BUSY;

	if (controler->current_command.command) {
        return ESP8266_ERR_COMMAND_BUSY;
    }

	int n = snprintf(controler->com_buffer, COMM_BUFFER_SIZE, "%s=%d,%d", AT_CIPSEND,id,size);
	controler->com_buffer[n+1] = 0;

	controler->rx_pointer = 0;

	esp8266_serial_write_command(controler->esp8266_serial, controler->com_buffer);

	time_t start = get_sys_milis();
	uint8_t l = 0;
	int position = -1;
	bool buff_write = false;

	while(get_sys_milis() - start < AT_CIPSEND_TIMEOUT) {
		if (esp8266_serial_available(controler->esp8266_serial)) {
			l = esp8266_serial_read(controler->esp8266_serial);
			if (!buff_write) {
				if (l == AT_CIPSEND_TERM) {
					//Write buffer
					esp8266_serial_write_buffer(controler->esp8266_serial, buffer, size);
					buff_write = true;
					controler->rx_pointer = 0;
				}
			} else {
				//Wait send confirm
				controler->rx_buffer[controler->rx_pointer++] = l;
				if (controler->rx_pointer > SIZEOF_COMMAND(AT_SEND_CONFIRM)) {
				    if (strncmp(controler->rx_buffer, AT_SEND_CONFIRM, SIZEOF_COMMAND(AT_SEND_CONFIRM)) == 0) {
				        //Send confirmed
				        controler->rx_pointer = 0;
                        controler->last_status_check = get_sys_milis();
                        return ESP8266_ERR_NONE;
				    } else {
				        //Send Failed
				        controler->rx_pointer = 0;
				        controler->last_status_check = get_sys_milis();
				        return ESP8266_ERR_TCP_SEND_FAILED;
				    }
				}
			}
		}
	}
	return ESP8266_ERR_TCP_SEND_TIMEOUT;

}

esp8266_error_t esp8266_tcp_register_receive_callback(
		esp8266_t *controler,
		on_esp8266_tcp_receive_fn receive_callback,
		void *receive_context)
{
	ESP8266_RETURN_IF_NIL2(controler, receive_callback, ESP8266_ERR_BAD_PARAM);

	controler->receive_callback = receive_callback;
	controler->receive_context = receive_context;

	return ESP8266_ERR_NONE;
}

inline esp8266_error_t esp8266_drop_connection(esp8266_t *controler, int id)
{
	if (controler->receive_callback) {
		controler->receive_callback(controler->receive_context, id, NULL, -1); //Notify connection close
	}
	controler->tcp_id[id] = ESP8266_TCP_CONN_UNUSED;

	return ESP8266_ERR_NONE;
}

inline esp8266_error_t esp8266_drop_all_connection(esp8266_t *controler)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	int id = 0;
	for (; id < TCP_CONNECTION_LIMIT; id++) {
		if (controler->tcp_id[id] != ESP8266_TCP_CONN_UNUSED) {
			esp8266_drop_connection(controler, id);
		}
	}
	return ESP8266_ERR_NONE;
}


void status_command_complete(void *context
								, const bool result
								, const bool timeout_expired
								, const char* command
								, int end_offset
								, time_t comand_complete_milis)
{
	if (!context)
		return;

	esp8266_t *controler = (esp8266_t *)context;

	if (result) {
		controler->command_state = ESP8266_COMMAND_RESPONCE_OK;
		if (strncmp(controler->rx_buffer, AT_STATUS_N, SIZEOF_COMMAND(AT_STATUS_N)) == 0) {
			//Response starts from STATUS:
			/*
			 * STATUS:
			 * 	3 - Have TCP connections
			 * 	5 - WiFi Disconnected
			 * 	4 - WiFi Connected, but now one connection opened
			 */
			if (controler->rx_buffer[SIZEOF_COMMAND(AT_STATUS_N)] == '3') {
				/*
				 * STATUS:3
				 * +CIPSTATUS:0,"TCP","62.149.25.228",80,0
				 */
				int i=0;
				controler->tmp_array_used = 0;
				size_t offset = 0;
				int in = indexOf(controler->rx_buffer, end_offset, AT_STATUS_CON, SIZEOF_COMMAND(AT_STATUS_CON));
				while(in >= 0) {
					offset += in;
					uint8 ch = controler->rx_buffer[offset+SIZEOF_COMMAND(AT_STATUS_CON)];
					int id = get_channel(ch);
					if (id >= 0 && id < TCP_CONNECTION_LIMIT) {
						controler->tmp_array[i] = id;
						controler->tmp_array_used++;
					}
					offset += SIZEOF_COMMAND(AT_STATUS_CON);
					in = indexOf(controler->rx_buffer+offset, end_offset, AT_STATUS_CON, SIZEOF_COMMAND(AT_STATUS_CON));
				}

				if (controler->tmp_array_used > 0) {
					//Drop all connections not listed in status
					int i=0;
					int j=0;
					bool con_found;
					for(;i<TCP_CONNECTION_LIMIT;i++) {
					    con_found = false;
					    for(j=0;j<controler->tmp_array_used;j++) {
					        if (controler->tmp_array[j] == i) {
					            con_found = true;
					            break;
					        }
					    }
					    if (!con_found && controler->tcp_id[i] != ESP8266_TCP_CONN_UNUSED) {
					        esp8266_drop_connection(controler, controler->tmp_array[i]);
					    }
					}

				} else {
					esp8266_drop_all_connection(controler);
				}
			} else {
				//If status not 3, close all opened connections
				esp8266_drop_all_connection(controler);
			}
		}
	} else {
		if (timeout_expired)
			controler->command_state = ESP8266_COMMAND_RESPONCE_TIMEOUT;
		else
			controler->command_state = ESP8266_COMMAND_RESPONCE_FAIL;

	}

}

esp8266_error_t esp8266_check_status(esp8266_t *controler)
{
	ESP8266_RETURN_IF_NIL(controler, ESP8266_ERR_BAD_PARAM);

	if (controler->ipd != ESP8266_IPD_UNDEF || controler->current_command.command) {
		return ESP8266_ERR_NONE;
	}
	if ((get_sys_milis() - controler->last_status_check) < controler->status_check_timeout) {
		return ESP8266_ERR_NONE;
	}

	esp8266_error_t error = esp8266_send_command(controler ,
							status_command_complete ,
							(void*)controler,
							AT_CIPSTATUS,
							AT_SUCCESS_OK1,
							SIZEOF_COMMAND(AT_SUCCESS_OK1),
							NULL,
							0,
							AT_GENERIC_TIMEOUT);

	ESP8266_RETURN_IF_ERR(error);

	controler->command_state = ESP8266_COMMAND_RESPONCE_WAIT;

	while(controler->command_state == ESP8266_COMMAND_RESPONCE_WAIT) {
		error = esp8266_process(controler, AT_GENERIC_TIMEOUT);
		ESP8266_RETURN_IF_ERR(error);
	}

	switch (controler->command_state) {
		case ESP8266_COMMAND_RESPONCE_OK:
			return ESP8266_ERR_NONE;
			break;
		case ESP8266_COMMAND_RESPONCE_TIMEOUT:
			return ESP8266_ERR_COMMAND_TIMEOUT;
			break;
		default:
			return ESP8266_ERR_COMMAND_ERROR;
			break;
	}
}
