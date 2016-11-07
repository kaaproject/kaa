#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

#include "uart.h"

extern int main(void);
    
static void main_task(void *pvParameters)
{
    (void)pvParameters;
    main();
    for (;;);
}

void user_init(void)
{
    uart_init_new();
    UART_SetBaudrate(UART0, 115200);
    UART_SetPrintPort(UART0);
    
    portBASE_TYPE error = xTaskCreate(main_task, "main_task", 512, NULL, 2, NULL );
    if (error < 0) {
        printf("Error creating main_task! Error code: %ld\r\n", error);
    }
}
