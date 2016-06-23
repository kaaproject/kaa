#include <stddef.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>

static void loop_fn(void *context)
{
    printf("Hello, Kaa!\r\n");

    kaa_client_stop(context);
}

int main(void)
{
    printf("Initializing Kaa client\r\n");

    kaa_client_t *kaa_client;

    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed to create Kaa client\r\n");
        return 1;
    }

    error_code = kaa_client_start(kaa_client, loop_fn, (void*)kaa_client, 0);
    if (error_code) {
        printf("Failed to start Kaa main loop\r\n");
        return 1;
    }

    kaa_client_destroy(kaa_client);
    return 0;
}
