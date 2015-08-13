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

#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

#include <stdio.h>
#include <mem.h>

#include <osapi.h>
#include <espressif/esp_wifi.h>
#include <espressif/esp_sta.h>

#include "driver/uart.h"
#include "esp_platform.h"

#define MAIN_STACK_SIZE 512

extern int main(void);

void main_task(void *pvParameters) {
    (void)pvParameters;
    printf("main_task() started\r\n");
    if (!wifi_connect(SSID,PWD)) {
        printf("Couldn't connect to \"%s\" with password \"%s\"\r\n2",
                SSID, PWD);
        goto loop;     
    }
    int ret = main();
    printf("main() exited, returned %d\r\n", ret);
loop:
    while(1);    
}

void ICACHE_FLASH_ATTR user_init() {
    uart_init();
    if (!wifi_init()) {
        printf("Error initialising wifi!\r\n");
        while(1);
    }
    portBASE_TYPE error = xTaskCreate(main_task, "main_task", 
                                      MAIN_STACK_SIZE, NULL, 2, NULL );
    if (error<0)
        printf("Error creating main_task! Error code: %d\r\n", error);
}

void uart_init() {
    uart_init_new();
    UART_SetBaudrate(UART0, 115200);
    UART_SetPrintPort(UART0);
}

bool wifi_init() {
    printf("\r\nInitialising wifi station\r\n");
    wifi_print_opmode();
    
    if (!wifi_set_opmode_current(0x01)) {
        printf("Error setting wifi opmode to station mode!\r\n");
        return false;
    }
    printf("Changed wifi mode to station\r\n");
    return true;
}

bool wifi_connect(const char *ssid, const char *pwd) {
    struct station_config sta_cfg;
    memset(&sta_cfg, 0, sizeof(sta_cfg));
    strcpy(sta_cfg.ssid, ssid);
    strcpy(sta_cfg.password, pwd);
    if (!wifi_station_set_config_current(&sta_cfg)) {
        printf("Error setting wifi station config!\r\n");
        return false;
    }
    wifi_print_station_config();
    printf("Connecting to %s...\r\n ", ssid);
    if (!wifi_station_connect()) {
        printf("FAIL!\r\n");
        return false;
    }
    uint8 status = wifi_station_get_connect_status();
    while (status==STATION_CONNECTING) {
        status = wifi_station_get_connect_status();
        switch (status) {
            case STATION_WRONG_PASSWORD:
                printf("Error connecting to \"%s\": wrong password!\r\n", ssid);
                goto conn_error;
            case STATION_NO_AP_FOUND:
                printf("Error connecting to \"%s\": no AP found with this ssid\r\n", ssid);
                goto conn_error;
            case STATION_CONNECT_FAIL:
                printf("Failed to connect\r\n");
                goto conn_error;
            case STATION_GOT_IP:
                printf("OK\r\n");
                return true;
            case STATION_IDLE:
                printf("OK\r\n");
                return true;
            case STATION_CONNECTING:
                break;
            default:
                printf ("Connection status: %d\r\n", status);
                return true;
        }
    }
conn_error:
    wifi_station_disconnect();
    return false;
}

void wifi_print_opmode() {
    uint8 opmode = wifi_get_opmode();
    switch (opmode) {
        case 0x01: /* Station mode */
            printf("Current wifi opmode is station\r\n");
            break;
        case 0x02: /* soft-AP */
            printf("Current wifi opmode is soft-AP\r\n");
            break;
        case 0x03: /* station+soft-AP */
            printf("Current wifi opmode is station+soft-AP\r\n");
            break;
        default:
            printf("Error getting wifi opmode!\r\n");
    }
}

void wifi_print_station_config() {
    struct station_config sta_cfg;
    if (!wifi_station_get_config(&sta_cfg)) {
        printf("Error getting current wifi station config!\r\n");
        return;
    }
    printf("Current wifi station conifguration:\r\n"
           "\tssid: %s\r\n\tpassword: %s\r\n\tbssid_set: %s\r\n",
           sta_cfg.ssid,sta_cfg.password, sta_cfg.bssid_set?"yes":"no");
}

/* Required, don't touch */
void ets_putc(char c) {
    os_putc(c);
 }

