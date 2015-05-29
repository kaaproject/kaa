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
#include <kaa/utilities/kaa_mem.h>
#include <kaa/kaa_user.h>
#include <kaa/gen/kaa_thermostat_event_class_family.h>



#define KAA_USER_ID            "user@email.com"
#define KAA_USER_ACCESS_TOKEN  "token"

#define THERMO_REQUEST_FQN          "org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest"
#define CHANGE_DEGREE_REQUEST_FQN   "org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest"



static kaa_client_t *kaa_client = NULL;
static bool is_shutdown = false;



#define KAA_DEMO_UNUSED(x) (void)(x);

#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

/*
 * Event callback-s.
 */

void kaa_on_thermostat_info_request(void *context
                                  , kaa_thermostat_event_class_family_thermostat_info_request_t *event
                                  , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    printf("ThermostatInfoRequest event received!\n");

    kaa_thermostat_event_class_family_thermostat_info_response_t *response =
            kaa_thermostat_event_class_family_thermostat_info_response_create();

    response->thermostat_info = kaa_thermostat_event_class_family_union_thermostat_info_or_null_branch_0_create();

    kaa_thermostat_event_class_family_thermostat_info_t *info = kaa_thermostat_event_class_family_thermostat_info_create();
    response->thermostat_info->data = info;

    int32_t *current_degree = (int32_t *) KAA_MALLOC(sizeof(int32_t));
    *current_degree = -5;

    int32_t *target_degree = (int32_t *) KAA_MALLOC(sizeof(int32_t));
    *target_degree = 10;

    info->degree = kaa_thermostat_event_class_family_union_int_or_null_branch_0_create();
    info->degree->data = current_degree;

    info->target_degree = kaa_thermostat_event_class_family_union_int_or_null_branch_0_create();
    info->target_degree->data = target_degree;

    info->is_set_manually = kaa_thermostat_event_class_family_union_boolean_or_null_branch_1_create();

    kaa_event_manager_send_kaa_thermostat_event_class_family_thermostat_info_response(
                                kaa_client_get_context(kaa_client)->event_manager, response, NULL);

    response->destroy(response); // Destroying event that was successfully sent

    event->destroy(event);
}


void kaa_on_thermostat_info_response(void *context
                                   , kaa_thermostat_event_class_family_thermostat_info_response_t *event
                                   , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    printf("ThermostatInfoResponse event received!\n");

    if (event->thermostat_info->type == KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0) {
        kaa_thermostat_event_class_family_thermostat_info_t *info =
                (kaa_thermostat_event_class_family_thermostat_info_t *) event->thermostat_info->data;

        if (info->degree->type == KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0) {
            int32_t *degree = (int32_t *) info->degree->data;
            printf("Degree=%d\n", *degree);
        }
        if (info->target_degree->type == KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0) {
            int32_t *target_degree = (int32_t *) info->target_degree->data;
            printf("Target degree=%d\n", *target_degree);
        }
        if (info->is_set_manually->type == KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0) {
            int8_t *is_set_manually = (int8_t *) info->is_set_manually->data;
            printf("Is_set_manually=%s\n", (*is_set_manually) ? "true" : "false");
        }
    }

    event->destroy(event);
}

void kaa_on_change_degree_request(void *context
                                , kaa_thermostat_event_class_family_change_degree_request_t *event
                                , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    printf("ChangeDegreeRequest event received!\n");

    if (event->degree->type == KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0) {
        int32_t *degree = (int32_t *) event->degree->data;
        printf("Change temperature by %d degrees\n", *degree);
    }

    event->destroy(event);
}



/*
 * Callback-s which receive result of event listener requests.
 */

kaa_error_t kaa_on_event_listeners(void *context, const kaa_endpoint_id listeners[], size_t listeners_count)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(listeners);

    printf("%zu event listeners received\n", listeners_count);

    // Creating Change degree request
    kaa_thermostat_event_class_family_change_degree_request_t *change_degree_request =
            kaa_thermostat_event_class_family_change_degree_request_create();

    change_degree_request->degree = kaa_thermostat_event_class_family_union_int_or_null_branch_0_create();
    int32_t *new_degree = (int32_t *) KAA_MALLOC(sizeof(int32_t));
    *new_degree = 10;
    change_degree_request->degree->data = new_degree;

    // Creating Thermo info request
    kaa_thermostat_event_class_family_thermostat_info_request_t *info_request =
            kaa_thermostat_event_class_family_thermostat_info_request_create();

    // Creating and sending the event block which consists of 2 events
    kaa_event_block_id trx_id = 0;
    kaa_error_t error_code = kaa_event_create_transaction(kaa_client_get_context(kaa_client)->event_manager, &trx_id);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_add_kaa_thermostat_event_class_family_change_degree_request_event_to_block(
            kaa_client_get_context(kaa_client)->event_manager, change_degree_request, NULL, trx_id);
    KAA_RETURN_IF_ERR(error_code);

    change_degree_request->destroy(change_degree_request); // Destroying event that was successfully added

    error_code = kaa_event_manager_add_kaa_thermostat_event_class_family_thermostat_info_request_event_to_block(
            kaa_client_get_context(kaa_client)->event_manager, info_request, NULL, trx_id);
    KAA_RETURN_IF_ERR(error_code);

    info_request->destroy(info_request); // Destroying event that was successfully added

    error_code = kaa_event_finish_transaction(kaa_client_get_context(kaa_client)->event_manager, trx_id);
    KAA_RETURN_IF_ERR(error_code);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_event_listeners_failed(void *context)
{
    KAA_DEMO_UNUSED(context);
    printf("Kaa Demo event listeners not found\n");
    return KAA_ERR_NONE;
}



/*
 * Callback-s which receive endpoint attach status.
 */
kaa_error_t kaa_on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    KAA_DEMO_UNUSED(context);
    printf("Kaa Demo attached to user %s, access token %s\n", user_external_id, endpoint_access_token);
    return KAA_ERR_NONE;
}


kaa_error_t kaa_on_detached(void *context, const char *endpoint_access_token)
{
    KAA_DEMO_UNUSED(context);
    printf("Kaa Demo detached from user access token %s\n", endpoint_access_token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_success(void *context)
{
    KAA_DEMO_UNUSED(context);

    printf("Kaa Demo attach success\n");

    const char *fqns[] = { THERMO_REQUEST_FQN
                         , CHANGE_DEGREE_REQUEST_FQN };

    kaa_event_listeners_callback_t listeners_callback = { NULL
                                                        , &kaa_on_event_listeners
                                                        , &kaa_on_event_listeners_failed };

    kaa_error_t error_code = kaa_event_manager_find_event_listeners(kaa_client_get_context(kaa_client)->event_manager, fqns, 2, &listeners_callback);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client_get_context(kaa_client)->logger, error_code, "Failed to find event listeners");
    }

    return error_code;
}

kaa_error_t kaa_on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    KAA_DEMO_UNUSED(context);

    printf("Kaa Demo attach failed: error %d, reason '%s'\n", error_code, (reason ? reason : "null"));

    is_shutdown = true;
    return KAA_ERR_NONE;
}

int main(/*int argc, char *argv[]*/)
{
    printf("Event demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    kaa_attachment_status_listeners_t listeners = { NULL
                                                  , &kaa_on_attached
                                                  , &kaa_on_detached
                                                  , &kaa_on_attach_success
                                                  , &kaa_on_attach_failed };

    error_code = kaa_user_manager_set_attachment_listeners(kaa_client_get_context(kaa_client)->user_manager, &listeners);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                       , KAA_USER_ID
                                                       , KAA_USER_ACCESS_TOKEN);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_set_kaa_thermostat_event_class_family_change_degree_request_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                      , &kaa_on_change_degree_request
                                                                                                      , NULL);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_set_kaa_thermostat_event_class_family_thermostat_info_request_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                        , &kaa_on_thermostat_info_request
                                                                                                        , NULL);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_set_kaa_thermostat_event_class_family_thermostat_info_response_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                         , &kaa_on_thermostat_info_response
                                                                                                         , NULL);
    KAA_RETURN_IF_ERR(error_code);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Event demo stopped\n");

    return error_code;
}

