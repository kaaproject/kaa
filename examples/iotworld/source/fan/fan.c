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
#include <unistd.h>
#include <stdlib.h>
#include <stdbool.h>
#include <errno.h>
#include <execinfo.h>
#include <stddef.h>
#include <sys/select.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_profile.h>
#include <kaa/kaa_event.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_defaults.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/gen/kaa_fan_event_class_family.h>
#include <kaa/gen/kaa_device_event_class_family.h>

#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>


#define KAA_USER_ID            "kaa"
#define KAA_USER_ACCESS_TOKEN  "token"
#define KAA_ENDPOINT_ACCESS_TOKEN  "FAN_CONTROLLER_ACCESS_CODE"

static kaa_client_t *kaa_client = NULL;

static const int FILE_NAME_SIZE = 50;

static int GPIO_NUM = 44;

static const char *device_name = "Fan";
static const char *device_model = "BeagleBone Black";

static kaa_fan_event_class_family_fan_status_t current_status;

void exportGpio(bool exportGpio)
{
    FILE *f;
    if (exportGpio) {
        printf("Exporting fan gpio");
        f = fopen("/sys/class/gpio/export", "w");
    } else {
        printf("Unexporting fan gpio");
        f = fopen("/sys/class/gpio/unexport", "w");
    }

    if (f == NULL) {
        perror("Error opening gpio (un)export file");
    }

    fprintf(f, "%d", GPIO_NUM);

    if (ferror(f)) {
        printf("Error writing to gpio (un)export file\n");
    }

    fclose(f);

    printf("gpio exported successfully\n");
}

void setDirection(bool out)
{
    char direction[FILE_NAME_SIZE];
    memset(direction, 0, FILE_NAME_SIZE);
    sprintf(direction, "/sys/class/gpio/gpio%d/direction", GPIO_NUM);
    FILE *f = fopen(direction, "w");
    if (f == NULL) {
        perror("Error opening gpio direction file\n");
    }
    out ? fprintf(f, "out") : fprintf(f, "in");

    if (ferror(f)) {
        printf("Error writing to gpio direction file\n");
    }

    fclose(f);
}

void changeFanState(kaa_fan_event_class_family_fan_status_t status)
{
    char value[50];
    sprintf(value, "/sys/class/gpio/gpio%d/value", GPIO_NUM);
    FILE *f = fopen(value, "w");
    if (f == NULL) {
        perror("Error occured while changing fan state\n");
    }
    fprintf(f, (status == ENUM_FAN_STATUS_ON ? "1" : "0"));
    if (ferror(f)) {
        perror("Error occured while changing fan state\n");
    }
    fclose(f);
}

void initFan(bool init) {
    if (init) {
        exportGpio(true);
        sleep(1);
        setDirection(true);
        changeFanState(ENUM_FAN_STATUS_OFF);
    } else {
        changeFanState(ENUM_FAN_STATUS_OFF);
        exportGpio(false);
    }
}


void kaa_on_device_change_name_request(void *context
                                     , kaa_device_event_class_family_device_change_name_request_t *event
                                     , kaa_endpoint_id_p source)
{
    printf("change_name_request recieved");
    event->destroy(event);
    printf("Name changed\n");
}

void kaa_on_device_status_subscription_request(void *context
                                            , kaa_device_event_class_family_device_status_subscription_request_t *event
                                            , kaa_endpoint_id_p source)
{
    printf("status_subscription_request received");

    event->destroy(event);
}


void kaa_on_device_info_request(void *context
                              , kaa_device_event_class_family_device_info_request_t *event
                              , kaa_endpoint_id_p source)
{
    printf("DeviceInfoRequest event received!\n");

    kaa_device_event_class_family_device_info_response_t *response =
            kaa_device_event_class_family_device_info_response_create();
    kaa_device_event_class_family_device_info_t *info = kaa_device_event_class_family_device_info_create();

    info->name = kaa_string_copy_create(device_name);
    info->model = kaa_string_copy_create(device_model);

    response->device_info = info;

    kaa_event_manager_send_kaa_device_event_class_family_device_info_response(kaa_client_get_context(kaa_client)->event_manager, response,
    source);

    printf("DeviceInfoResponse sent!\n");
    response->destroy(response);

    event->destroy(event);
}

void kaa_on_switch_request(void *context, kaa_fan_event_class_family_switch_request_t *event, kaa_endpoint_id_p source) {
    printf("SwitchRequest event received!\n");
    kaa_fan_event_class_family_fan_status_update_t *response = kaa_fan_event_class_family_fan_status_update_create();

    changeFanState(event->status);

    response->status = current_status;
    kaa_event_manager_send_kaa_fan_event_class_family_fan_status_update(kaa_client_get_context(kaa_client)->event_manager, response, source);

    response->destroy(response); // Destroying event that was successfully sent

    event->destroy(event);
}

kaa_error_t kaa_on_event_listeners(void *context, const kaa_endpoint_id listeners[], size_t listeners_count) {
    printf("%zu event listeners received\n", listeners_count);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_event_listeners_failed(void *context) {
    printf("Kaa Demo event listeners not found\n");
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attached(void *context, const char *user_external_id, const char *endpoint_access_token) {
    printf("Kaa Demo attached to user %s, access token %s\n", user_external_id, endpoint_access_token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_detached(void *context, const char *endpoint_access_token) {
    printf("Kaa Demo detached from user access token %s\n", endpoint_access_token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_success(void *context) {
    printf("Kaa Demo attach success\n");
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason) {
    printf("Kaa Demo attach failed\n");
    kaa_client_stop(kaa_client);
    return KAA_ERR_NONE;
}

#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

int main(/*int argc, char *argv[]*/)
{
    printf("Fan demo started\n");

    initFan(true);

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
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set user attach status listener");

    error_code = kaa_profile_manager_set_endpoint_access_token(kaa_client_get_context(kaa_client)->profile_manager
                                                                                    , KAA_ENDPOINT_ACCESS_TOKEN);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set endpoint access token");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_info_request_listener(
                        kaa_client_get_context(kaa_client)->event_manager, &kaa_on_device_info_request, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device info request listener");

    error_code = kaa_event_manager_set_kaa_fan_event_class_family_switch_request_listener(
                        kaa_client_get_context(kaa_client)->event_manager, &kaa_on_switch_request, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set switch request listener");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_change_name_request_listener(
                        kaa_client_get_context(kaa_client)->event_manager, &kaa_on_device_change_name_request, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device change name request listener");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_status_subscription_request_listener(
                        kaa_client_get_context(kaa_client)->event_manager, &kaa_on_device_status_subscription_request, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device status subscription request listener");

    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                                              , KAA_USER_ID
                                                                              , KAA_USER_ACCESS_TOKEN);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to attach to user");

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);
    initFan(false);

    printf("Fan demo stopped\n");

    return error_code;
}
