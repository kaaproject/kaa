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

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/kaa_configuration_manager.h>



static kaa_client_t *kaa_client = NULL;



#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

void kaa_demo_print_configuration_message(const kaa_root_configuration_t *configuration)
{
    if (configuration->address_list->type == KAA_CONFIGURATION_UNION_ARRAY_LINK_OR_NULL_BRANCH_0) {
        printf("Configuration body:\n");

        kaa_list_node_t *it = kaa_list_begin((kaa_list_t*) configuration->address_list->data);
        while (it) {
            kaa_configuration_link_t* current_link = (kaa_configuration_link_t*) kaa_list_get_data(it);
            printf("%s - %s\n", current_link->label->data,current_link->url->data);
            it = kaa_list_next(it);
        }
    } else {
        printf("Configuration body: null\n");
    }
}

kaa_error_t kaa_demo_configuration_receiver(void *context, const kaa_root_configuration_t *configuration)
{
    (void) context;
    KAA_LOG_TRACE(kaa_client_get_context(kaa_client)->logger, KAA_ERR_NONE, "Received configuration data");
    kaa_demo_print_configuration_message(configuration);
    kaa_client_stop(kaa_client);
    return KAA_ERR_NONE;
}

int main(/*int argc, char *argv[]*/)
{
    printf("Configuration demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    kaa_configuration_root_receiver_t receiver = { NULL, &kaa_demo_configuration_receiver };
    error_code = kaa_configuration_manager_set_root_receiver(kaa_client_get_context(kaa_client)->configuration_manager, &receiver);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to add configuration receiver");

    kaa_demo_print_configuration_message(kaa_configuration_manager_get_configuration(kaa_client_get_context(kaa_client)->configuration_manager));

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Configuration demo stopped\n");
    return error_code;
}

