#ifndef ESP8266_TIME_H
#define ESP8266_TIME_H

#include <stdint.h>
#include <time.h>

typedef uint32_t kaa_time_t;

#define KAA_TIME() (system_get_time()/1000000)

#endif /* ESP8266_TIME_H */
