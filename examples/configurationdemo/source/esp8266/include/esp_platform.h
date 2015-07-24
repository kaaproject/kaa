#ifndef ESP_PLATFORM_H
#define ESP_PLATFORM_H

#include <stdbool.h>

/* UART */
void uart_init();

/* WIFI */
bool wifi_init();
bool wifi_connect(const char *ssid, const char *pwd);
void wifi_print_opmode();
void wifi_print_station_config();

/* Main task */
void main_task(void *pvParameters);

#endif /* ESP_PLATOFRM_H */
