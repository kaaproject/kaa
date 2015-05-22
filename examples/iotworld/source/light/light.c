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
#include <stdlib.h>
#include <errno.h>
#include <execinfo.h>
#include <stddef.h>
#include <sys/select.h>
#include <unistd.h>
#include <stdbool.h>
#include <unistd.h>
#include <kaa/kaa_profile.h>

#include <pthread.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_configuration_manager.h>
#include <kaa/kaa_event.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_defaults.h>
#include <kaa/gen/kaa_device_event_class_family.h>
#include <kaa/gen/kaa_geo_fencing_event_class_family.h>
#include <kaa/gen/kaa_light_event_class_family.h>

#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>

#define KAA_USER_ID            "kaa"
#define KAA_USER_ACCESS_TOKEN  "token"
#define KAA_ENDPOINT_ACCESS_TOKEN  "LIGHTS_CONTROLLER_ACCESS_CODE"

static kaa_client_t *kaa_client = NULL;

static char gpio_nums[] = { 66, 67, 68, 69 };
#define gpios_count (sizeof(gpio_nums) / sizeof(gpio_nums[0]))
int32_t brightness[gpios_count];
char bulbs_state[gpios_count];
static char gpio_indexes[gpios_count];
static const int FILE_NAME_SIZE = 50;
static kaa_geo_fencing_event_class_family_geo_fencing_position_t last_position = ENUM_GEO_FENCING_POSITION_AWAY;
static kaa_geo_fencing_event_class_family_operation_mode_t operation_mode = ENUM_OPERATION_MODE_OFF;
pthread_t pth[gpios_count];

pthread_mutex_t brightness_mutex;
pthread_mutex_t bulb_states_mutex;

static char *device_name = "Home lights";
static const char *device_model = "BeagleBone Black";
static const char *bulb_names[] = {"Doorstep", "Kitchen", "Living room", "Bedroom"};

static const char *geofencing_state = "geofencing_state";
static const char *brightness_state = "brightness_state";
static const char *name_state = "name_state";

kaa_list_t *subscribers_list = kaa_list_create();

void send_bulb_list_status_update(kaa_endpoint_id_p source, bool is_status_request);
void persist_geofencing_state();

void log_operation_mode() {
       switch (operation_mode) {
            case ENUM_OPERATION_MODE_ON:
                printf("Operation mode: ON\n");
                break;
            case ENUM_OPERATION_MODE_OFF:
                printf("Operation mode: OFF\n");
                break;
            case ENUM_OPERATION_MODE_GEOFENCING:
                printf("Operation mode: GEOFENCING\n");
                break;
            default:
                printf("Operation mode: UNKNOWN\n");
                break;
        }
}

void log_position() {
       switch (last_position) {
            case ENUM_GEO_FENCING_POSITION_AWAY:
                printf("Position: AWAY\n");
                break;
            case ENUM_GEO_FENCING_POSITION_HOME:
                printf("Position: HOME\n");
                break;
            case ENUM_GEO_FENCING_POSITION_NEAR:
                printf("Position: NEAR\n");
                break;
            default:
                printf("Position: UNKNOWN\n");
                break;
        }
}

void process_operation_mode(kaa_geo_fencing_event_class_family_geo_fencing_position_t new_position,
                            kaa_geo_fencing_event_class_family_operation_mode_t new_mode) {
    bool persist = false;
    if (new_mode != operation_mode) {
        persist = true;
        operation_mode = new_mode;
    }
    if (last_position != new_position) {
        persist = true;
        last_position = new_position;
    }
    if (persist) {
        persist_geofencing_state();
        kaa_geo_fencing_event_class_family_geo_fencing_status_response_t *response =
                kaa_geo_fencing_event_class_family_geo_fencing_status_response_create();
        response->position = last_position;
        response->mode = operation_mode;
        send_bulb_list_status_update(NULL, true);
        kaa_event_manager_send_kaa_geo_fencing_event_class_family_geo_fencing_status_response(kaa_client_get_context(kaa_client)->event_manager,
                response,
                NULL);
        response->destroy(response);
    }

    printf("Process operation mode:\n");
    log_operation_mode();
    log_position();
}

void load_previous_state_if_any() {

    FILE *f = fopen(name_state, "rb");
    if (f) {
        fseek(f, 0, SEEK_END);
        size_t size = ftell(f);
        fseek(f, 0, SEEK_SET);
        device_name = (char *) KAA_MALLOC(size);
        KAA_RETURN_IF_NIL(device_name,);
        fread(device_name, size, 1, f);
        fclose(f);
    }

    f = fopen(brightness_state, "rb");
    if (f) {
        fseek(f, 0, SEEK_END);
        size_t size = ftell(f);
        fseek(f, 0, SEEK_SET);
        if (size) {
            fread(brightness, sizeof(uint32_t), gpios_count, f);
            fseek(f, sizeof(uint32_t) * gpios_count, SEEK_SET);
            fread(bulbs_state, sizeof(char), gpios_count, f);
            fclose(f);
        }
    }

    kaa_geo_fencing_event_class_family_geo_fencing_position_t persisted_position = last_position;
    kaa_geo_fencing_event_class_family_operation_mode_t persisted_operation_mode = operation_mode;

    f = fopen(geofencing_state, "rb");
    if (f) {
        fseek(f, 0, SEEK_END);
        size_t size = ftell(f);
        fseek(f, 0, SEEK_SET);
        if (size) {
            fread(&persisted_position, sizeof(persisted_position), 1, f);
            fseek(f, sizeof(last_position), SEEK_SET);
            fread(&persisted_operation_mode, sizeof(persisted_operation_mode), 1, f);
            fclose(f);
        }
    }
    process_operation_mode(persisted_position, persisted_operation_mode);
}

void persist_bulbs_state() {
    FILE *f = fopen(brightness_state, "wb");
    if (f) {
        pthread_mutex_lock(&brightness_mutex);
        fwrite(brightness, sizeof(int32_t), gpios_count, f);
        pthread_mutex_unlock(&brightness_mutex);

        pthread_mutex_lock(&bulb_states_mutex);
        fseek(f, sizeof(uint32_t) * gpios_count, SEEK_SET);
        fwrite(bulbs_state, sizeof(char), gpios_count, f);
        pthread_mutex_unlock(&bulb_states_mutex);
        fclose(f);
        printf("Brightness state persisted successfully\n");
    }
}

void persist_geofencing_state() {
    FILE *f = fopen(geofencing_state, "wb");
    if (f) {
        fwrite(&last_position, sizeof(last_position), 1, f);
        fseek(f, sizeof(last_position), SEEK_SET);
        fwrite(&operation_mode, sizeof(operation_mode), 1, f);
        fclose(f);
    }
    printf("Geofencing state persisted successfully\n");
}

void updateGpioValue(char *fileName, bool enable) {
    FILE *f = fopen(fileName, "w");
    enable ? fprintf(f, "1") : fprintf(f, "0");
    fclose(f);
}

void exportGpios(bool exportGpios)
{
    int i = gpios_count;

    while (i--) {
        FILE *f;
        if (exportGpios) {
            printf("Exporting gpio for bulb with id %d\n", i);
            f = fopen("/sys/class/gpio/export", "w");
        } else {
            printf("Unexporting gpio for bulb with id %d\n", i);
            f = fopen("/sys/class/gpio/unexport", "w");
        }

        if (f == NULL) {
            perror("Error opening gpio (un)export file");
        }

        fprintf(f, "%d", gpio_nums[i]);

        if (ferror(f)) {
            printf("Error writing to gpio (un)export file\n");
        }

        fclose(f);

        printf("gpio for bulb with id %d successfully exported\n", i);
    }

}

void set_direction(bool out) {
    int i = gpios_count;
    while (i--) {
        char direction[FILE_NAME_SIZE];
        memset(direction, 0, FILE_NAME_SIZE);
        sprintf(direction, "/sys/class/gpio/gpio%d/direction", gpio_nums[i]);
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
}

char get_builb_index(const char* bulb_id) {
    int i = gpios_count;
    while(i--) {
        if (strcmp(bulb_names[i], bulb_id) == 0) {
            return i;
        }
    }
    return i;
}

void set_brightness(int id, int32_t percent) {
    percent = percent < 0 ? 0 : percent;
    percent = percent > 100 ? 100 : percent;
    pthread_mutex_lock(&brightness_mutex);
    brightness[id] = percent;
    printf("Brightness was set to %d for bulb with id %d\n", brightness[id], id);
    pthread_mutex_unlock(&brightness_mutex);
}

void set_bulb_state(int id, char state) {
    pthread_mutex_lock(&bulb_states_mutex);
    bulbs_state[id] = state;
    pthread_mutex_unlock(&bulb_states_mutex);
}

int32_t get_brightness(int id) {
    pthread_mutex_lock(&brightness_mutex);
    uint32_t percents = brightness[id];
    pthread_mutex_unlock(&brightness_mutex);
    return percents;
}

char get_bulb_state(int id) {
    pthread_mutex_lock(&bulb_states_mutex);
    id = bulbs_state[id];
    pthread_mutex_unlock(&bulb_states_mutex);
    return id;
}

bool is_operational() {
    return operation_mode == ENUM_OPERATION_MODE_ON ||
           (operation_mode == ENUM_OPERATION_MODE_GEOFENCING
           && last_position == ENUM_GEO_FENCING_POSITION_HOME);
}

void *brightness_loop(void *id_ptr) {
    int id = *((char *) id_ptr);
    int period_microseconds = 10000;
    char value[FILE_NAME_SIZE];
    memset(value, 0, FILE_NAME_SIZE);
    sprintf(value, "/sys/class/gpio/gpio%d/value", gpio_nums[id]);
    printf("Starting bulb controller for bulb with id %d\n", id);
    while (true) {
        int32_t shine_period = 0;
        int32_t percents = get_brightness(id);

        char bulb_state = get_bulb_state(id);

        if (bulb_state == ENUM_BULB_STATUS_ON && is_operational()) {
            if (percents <= 0) {
                updateGpioValue(value, false);
                usleep(period_microseconds);
            } else if (percents >= 100) {
                updateGpioValue(value, true);
                usleep(period_microseconds);
            } else {
                shine_period = (period_microseconds / 100) * percents;

                updateGpioValue(value, true);
                usleep(shine_period);

                updateGpioValue(value, false);
                usleep(period_microseconds - shine_period);
            }
        } else {
            updateGpioValue(value, false);
            usleep(period_microseconds);
        }
    }
}

void init_bulbs() {

    memset(brightness, 0, gpios_count * sizeof(int32_t));

    exportGpios(true);

    printf("All bulb gpios exported successfully\n");

    sleep(1);

    set_direction(true);

    printf("Out direction successfully set for all gpios\n");

    int i = gpios_count;

    printf("Starting bulb controller threads\n");

    while (i--) {
        set_brightness(i, 100);
        gpio_indexes[i] = i;

        int pthread_err = 0;
        pthread_err = pthread_create(&pth[i], NULL, brightness_loop, &gpio_indexes[i]);
        pthread_err = pthread_detach(pth[i]);

        if (!pthread_err) {
            printf("Bulb controller thread started for bulb with id %d \n", i);
        } else {
            printf("Bulb controller thread FAILED to start for bulb with id %d \n", i);
        }
    }

    printf("Bulb threads initialized\n");
}

void deinit_bulbs() {
    char i = gpios_count;
    while (i--) {
        set_brightness(i, 0);
    }
    exportGpios(false);
}

void send_device_info_response(kaa_endpoint_id_p source) {
    kaa_device_event_class_family_device_info_response_t *response =
            kaa_device_event_class_family_device_info_response_create();
    kaa_device_event_class_family_device_info_t *info = kaa_device_event_class_family_device_info_create();

    const char *name = device_name;
    info->name = kaa_string_copy_create(name);
    info->model = kaa_string_copy_create(device_model);

    response->device_info = info;

    kaa_event_manager_send_kaa_device_event_class_family_device_info_response(kaa_client_get_context(kaa_client)->event_manager, response,
            source);

    printf("DeviceInfoResponse sent!\n");
    response->destroy(response);
}

void kaa_on_device_info_request(void *context, kaa_device_event_class_family_device_info_request_t *event,
        kaa_endpoint_id_p source) {
    printf("DeviceInfoRequest event received!\n");

    send_device_info_response(source);

    event->destroy(event);
}

void kaa_on_geo_fencing_position_update(void *context,
        kaa_geo_fencing_event_class_family_geo_fencing_position_update_t *event, kaa_endpoint_id_p source) {
    printf("GeoFencingPositionUpdate event received!\n");
    process_operation_mode(event->position, operation_mode);
    event->destroy(event);
}

void kaa_on_geo_fencing_event_class_family_operation_mode_update_request(void *context,
        kaa_geo_fencing_event_class_family_operation_mode_update_request_t *event, kaa_endpoint_id_p source) {
    printf("GeoFencingOperationModeUpdate event received!\n");
    process_operation_mode(last_position, event->mode);
    event->destroy(event);
}

void kaa_on_geo_fencing_event_class_family_geo_fencing_status_request(void *context,
        kaa_geo_fencing_event_class_family_geo_fencing_status_request_t *event, kaa_endpoint_id_p source) {
    printf("GeoFencingStatusRequest event received!\n");
    kaa_geo_fencing_event_class_family_geo_fencing_status_response_t *response =
            kaa_geo_fencing_event_class_family_geo_fencing_status_response_create();
    response->position = last_position;
    response->mode = operation_mode;
    kaa_event_manager_send_kaa_geo_fencing_event_class_family_geo_fencing_status_response(kaa_client_get_context(kaa_client)->event_manager,
            response,
            source);
    event->destroy(event);
    response->destroy(response);
    printf("GeoFencingStatusResponse sent!\n");
}

void kaa_on_bulb_brightness_request(void *context, kaa_light_event_class_family_change_bulb_brightness_request_t *event,
        kaa_endpoint_id_p source) {
    printf("BulbChangeBrightnessRequest event received!\n");
    printf("Sender id is %s \n", event->bulb_id->data);
    printf("Received brightness is %d \n", event->brightness);
    char bulb_id = get_builb_index((const char*)event->bulb_id->data);
    set_brightness(bulb_id, event->brightness);
    persist_bulbs_state();
    send_bulb_list_status_update(source, false);
    event->destroy(event);
}

void kaa_on_bulb_status_request(void *context, kaa_light_event_class_family_change_bulb_status_request_t *event,
        kaa_endpoint_id_p source) {
    printf("BulbChangeStatusRequest event received!\n");
    printf("Sender id is %s \n", event->bulb_id->data);
    printf("Received status is %d \n", event->status);
    char bulb_id = get_builb_index((const char*)event->bulb_id->data);
    set_bulb_state(bulb_id, event->status);
    persist_bulbs_state();
    send_bulb_list_status_update(source, true);
    event->destroy(event);
}

void kaa_on_device_change_name_request(void *context, kaa_device_event_class_family_device_change_name_request_t *event,
        kaa_endpoint_id_p source) {
    if (device_name) {
        KAA_FREE(device_name);
    }
    device_name = event->name->data;
    size_t size = 0;
    while (*device_name++) {
        size++;
    }
    device_name = (char *) KAA_MALLOC(size + 1);
    KAA_RETURN_IF_NIL(device_name,);
    strcpy(device_name, event->name->data);
    FILE *f = fopen(name_state, "wb");
    if (f) {
        fwrite(device_name, size, 1, f);
        fclose(f);
    }
    send_bulb_list_status_update(NULL, true);
    send_device_info_response(NULL);
    event->destroy(event);
    printf("Name changed\n");
}

void kaa_on_device_status_subscription_request(void *context
                                             , kaa_device_event_class_family_device_status_subscription_request_t *event
                                             , kaa_endpoint_id_p source)
{
    KAA_RETURN_IF_NIL(source,);

    kaa_list_node_t *it = kaa_list_begin(subscribers_list);
    while (it) {
        kaa_endpoint_id_p subscriber = (kaa_endpoint_id_p) kaa_list_get_data(it);
        if (!memcmp(subscriber, source, KAA_ENDPOINT_ID_LENGTH)) {
            return;
        }
        it = kaa_list_next(it);
    }
    kaa_endpoint_id_p source_p = (kaa_endpoint_id_p) KAA_MALLOC(KAA_ENDPOINT_ID_LENGTH);
    KAA_RETURN_IF_NIL(source_p,);
    memcpy(source_p, source, KAA_ENDPOINT_ID_LENGTH);
    kaa_list_push_front(subscribers_list, source_p);

    event->destroy(event);
    send_bulb_list_status_update(source, true);
}

void send_bulb_list_status_update(kaa_endpoint_id_p source, bool is_status_request)
{
    kaa_light_event_class_family_bulb_list_status_update_t *response =
            kaa_light_event_class_family_bulb_list_status_update_create();

    kaa_list_t *bulbs_info_list = kaa_list_create();

    int i = gpios_count;
    char bulb_id[2] = { 0, 0 };

    while (i--) {
        kaa_light_event_class_family_bulb_info_t *bulb_info = kaa_light_event_class_family_bulb_info_create();
        bulb_info->brightness = get_brightness(i);
        bulb_info->max_brightness = 100;
        bulb_id[0] = i;
        bulb_info->bulb_id = kaa_string_copy_create(bulb_names[i]);
        bulb_info->status = get_bulb_state(i);
        bulb_info->ignore_brightness_update = 0;

        kaa_list_push_front(bulbs_info_list, bulb_info);
    }

    response->bulbs = kaa_light_event_class_family_union_array_bulb_info_or_null_branch_0_create();
    response->bulbs->data = bulbs_info_list;

    if (source) {
        kaa_list_node_t *current_node_listener = kaa_list_begin(subscribers_list);
        while (current_node_listener) {
            kaa_endpoint_id_p endpoint = (kaa_endpoint_id_p) kaa_list_get_data(current_node_listener);
            if ((!memcmp(endpoint, source, KAA_ENDPOINT_ID_LENGTH)) && !is_status_request) {
                current_node_listener = kaa_list_next(current_node_listener);
                continue;
            }
            kaa_event_manager_send_kaa_light_event_class_family_bulb_list_status_update(
                                                            kaa_client_get_context(kaa_client)->event_manager
                                                          , response
                                                          , endpoint);
            current_node_listener = kaa_list_next(current_node_listener);
        }
    } else {
        kaa_event_manager_send_kaa_light_event_class_family_bulb_list_status_update(kaa_client_get_context(kaa_client)->event_manager,
                response,
                NULL);
    }

    printf("BulbListStatusUpdate sent!\n");
    response->destroy(response);
}

void kaa_on_bulb_list_request(void *context, kaa_light_event_class_family_bulb_list_request_t *event,
        kaa_endpoint_id_p source) {
    printf("BulbListRequest event received!\n");

    send_bulb_list_status_update(source, true);

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
    load_previous_state_if_any();
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
    printf("Light control demo started\n");

    init_bulbs();

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

    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                                              , KAA_USER_ID
                                                                              , KAA_USER_ACCESS_TOKEN);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to attach to user");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_info_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_device_info_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device info request listener");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_status_subscription_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_device_status_subscription_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device status subscription request listener");

    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_change_name_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_device_change_name_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set device change name request listener");

    error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_geo_fencing_position_update_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_geo_fencing_position_update
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set geo_fencing_position_update listener ");

    error_code = kaa_event_manager_set_kaa_light_event_class_family_change_bulb_status_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_bulb_status_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set change_bulb_status_request listener");

    error_code = kaa_event_manager_set_kaa_light_event_class_family_change_bulb_brightness_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_bulb_brightness_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set change_bulb_brightness_request listener");

    error_code = kaa_event_manager_set_kaa_light_event_class_family_bulb_list_request_listener(
            kaa_client_get_context(kaa_client)->event_manager
          , &kaa_on_bulb_list_request
          , NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set bulb_list_request listener");

    error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_operation_mode_update_request_listener(
                                                        kaa_client_get_context(kaa_client)->event_manager
                                                      , &kaa_on_geo_fencing_event_class_family_operation_mode_update_request,
                                                        NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set operation_mode_update_request listener");

    error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_geo_fencing_status_request_listener(
                                                        kaa_client_get_context(kaa_client)->event_manager
                                                      , &kaa_on_geo_fencing_event_class_family_geo_fencing_status_request,
                                                        NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set geo_fencing_status_request listener");

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);
    deinit_bulbs();

    printf("Light control demo stopped\n");

    return error_code;
}
