#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

void exit(int status) {
    vTaskDelete(NULL);
}
