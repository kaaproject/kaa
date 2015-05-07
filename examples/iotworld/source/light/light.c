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
#include <stdbool.h>
#include <unistd.h>

#include <pthread.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_configuration_manager.h>
#include <kaa/kaa_event.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_defaults.h>
#include <kaa/gen/kaa_device_event_class_family.h>
#include <kaa/gen/kaa_geo_fencing_event_class_family.h>
#include <kaa/gen/kaa_light_event_class_family.h>

#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>

#include <kaa/platform/ext_sha.h>
#include <kaa/platform/ext_transport_channel.h>
#include <kaa/platform-impl/kaa_tcp_channel.h>

#define KAA_USER_ID            "kaa"
#define KAA_USER_ACCESS_TOKEN  "token"
#define KAA_ENDPOINT_ACCESS_TOKEN  "LIGHTS_CONTROLLER_ACCESS_CODE"

static kaa_context_t *kaa_context_ = NULL;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE, KAA_SERVICE_USER, KAA_SERVICE_EVENT };
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

static kaa_transport_channel_interface_t bootstrap_channel;
static kaa_transport_channel_interface_t operations_channel;

static bool is_shutdown = false;

static char gpio_nums[] = { 68, 69 };
#define gpios_count (sizeof(gpio_nums) / sizeof(gpio_nums[0]))
int32_t brightness[gpios_count];
char bulbs_state[gpios_count];
static char gpio_indexes[gpios_count];
static const int FILE_NAME_SIZE = 50;
static kaa_geo_fencing_event_class_family_geo_fencing_position_t last_position;
static kaa_geo_fencing_event_class_family_operation_mode_t operation_mode;
pthread_t pth[gpios_count];

pthread_mutex_t brightness_mutex;
pthread_mutex_t bulb_states_mutex;

static char *device_name;
static const char *device_model = "BeagleBone Black";

static const char *brightness_state = "brightness_state";
static const char *name_state = "name_state";

kaa_list_t *subscribers_list = NULL;

extern int usleep(__useconds_t                       __useconds);

int kaa_demo_event_loop();
void send_bulb_list_status_update(kaa_endpoint_id_p source, bool is_status_request);

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

void updateGpioValue(char *fileName, bool enable) {
	FILE *f = fopen(fileName, "w");
	enable ? fprintf(f, "1") : fprintf(f, "0");
	fclose(f);
}

void exportGpios(bool exportGpios) {
	char i = gpios_count;

	short succes = 0;

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
	char i = gpios_count;
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

void set_brightness(char id, int32_t percent) {
	percent = percent < 0 ? 0 : percent;
	percent = percent > 100 ? 100 : percent;
	pthread_mutex_lock(&brightness_mutex);
	brightness[id] = percent;
	printf("Brightness was set to %d for bulb wit id %d\n", brightness[id], id);
	pthread_mutex_unlock(&brightness_mutex);
}

void set_bulb_state(char id, char state) {
	pthread_mutex_lock(&bulb_states_mutex);
	bulbs_state[id] = state;
	pthread_mutex_unlock(&bulb_states_mutex);
}

int32_t get_brightness(char id) {
	pthread_mutex_lock(&brightness_mutex);
	uint32_t percents = brightness[id];
	pthread_mutex_unlock(&brightness_mutex);
	return percents;
}

char get_bulb_state(char id) {
	pthread_mutex_lock(&bulb_states_mutex);
	id = bulbs_state[id];
	pthread_mutex_unlock(&bulb_states_mutex);
	return id;
}

void enable_all_lights(bool enable) {
	char i = gpios_count;
	if (enable) {
		while (--i) {
			set_bulb_state(i, ENUM_BULB_STATUS_ON);
		}
	} else {
		while (--i) {
			set_bulb_state(i, ENUM_BULB_STATUS_OFF);
		}
	}
	persist_bulbs_state();

}

void *brightness_loop(void *id_ptr) {
	char *id = (char *) id_ptr;
	char gpio_num = gpio_nums[*id];
	int period_microseconds = 10000;
	char value[FILE_NAME_SIZE];
	memset(value, 0, FILE_NAME_SIZE);
	sprintf(value, "/sys/class/gpio/gpio%d/value", gpio_num);
	printf("Starting bulb controller for bulb with id %d\n", *id);
	while (true) {
		int32_t shine_period = 0;
		int32_t percents = get_brightness(*id);

		char bulb_state = get_bulb_state(*id);

		if (bulb_state == ENUM_BULB_STATUS_ON) {
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

	char i = gpios_count;

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
	while (--i) {
		set_brightness(i, 0);
	}
	exportGpios(false);
}

void send_device_info_response() {
	kaa_device_event_class_family_device_info_response_t *response =
			kaa_device_event_class_family_device_info_response_create();
	kaa_device_event_class_family_device_info_t *info = kaa_device_event_class_family_device_info_create();

	const char *name = device_name;
	info->name = kaa_string_copy_create(name);
	info->model = kaa_string_copy_create(device_model);

	response->device_info = info;

	kaa_event_manager_send_kaa_device_event_class_family_device_info_response(kaa_context_->event_manager, response,
	NULL);

	printf("DeviceInfoResponse sent!\n");
	response->destroy(response);
}

void kaa_on_device_info_request(void *context, kaa_device_event_class_family_device_info_request_t *event,
		kaa_endpoint_id_p source) {
	printf("DeviceInfoRequest event received!\n");

	send_device_info_response();

	event->destroy(event);
}

void kaa_on_geo_fencing_position_update(void *context,
		kaa_geo_fencing_event_class_family_geo_fencing_position_update_t *event, kaa_endpoint_id_p source) {
	printf("GeoFencingPositionUpdate event received!\n");

	if (last_position != event->position) {
		if (event->position == ENUM_GEO_FENCING_POSITION_HOME) {
			enable_all_lights(true);
		} else {
			enable_all_lights(false);
		}
	}

	last_position = event->position;

	event->destroy(event);
}

void kaa_on_geo_fencing_event_class_family_operation_mode_update_request(void *context,
		kaa_geo_fencing_event_class_family_operation_mode_update_request_t *event, kaa_endpoint_id_p source) {
	printf("GeoFencingOperationModeUpdate event received!\n");
	operation_mode = event->mode;
	event->destroy(event);
}

void kaa_on_geo_fencing_event_class_family_geo_fencing_status_request(void *context,
		kaa_geo_fencing_event_class_family_geo_fencing_status_request_t *event, kaa_endpoint_id_p source) {
	printf("GeoFencingStatusRequest event received!\n");
	kaa_geo_fencing_event_class_family_geo_fencing_status_response_t *response =
			kaa_geo_fencing_event_class_family_geo_fencing_status_response_create();
	response->position = last_position;
	response->mode = operation_mode;
	kaa_event_manager_send_kaa_geo_fencing_event_class_family_geo_fencing_status_response(kaa_context_->event_manager,
			response,
			NULL);

	printf("GeoFencingStatusResponse sent!\n");
}

void kaa_on_bulb_brightness_request(void *context, kaa_light_event_class_family_change_bulb_brightness_request_t *event,
		kaa_endpoint_id_p source) {
	printf("BulbChangeBrightnessRequest event received!\n");
	printf("Sender id is %d \n", (*(event->bulb_id->data)));
	printf("Received brightness is %d \n", event->brightness);
	set_brightness(*(event->bulb_id->data), event->brightness);
	persist_bulbs_state();
	send_bulb_list_status_update(source, false);
	event->destroy(event);
}

void kaa_on_bulb_status_request(void *context, kaa_light_event_class_family_change_bulb_status_request_t *event,
		kaa_endpoint_id_p source) {
	printf("BulbChangeStatusRequest event received!\n");
	printf("Sender id is %d \n", (*(event->bulb_id->data)));
	printf("Received status is %d \n", event->status);
	set_bulb_state(*(event->bulb_id->data), event->status);
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
	send_device_info_response();
	event->destroy(event);
	printf("Name changed\n");
}

void kaa_on_device_status_subscription_request(void *context,
		kaa_device_event_class_family_device_status_subscription_request_t *event, kaa_endpoint_id_p source) {
	KAA_RETURN_IF_NIL(source,);
	kaa_list_t *test_subscr_list = NULL;
	kaa_endpoint_id_p source_p = NULL;
	if (subscribers_list == NULL) {
		source_p = (kaa_endpoint_id_p) KAA_MALLOC(KAA_ENDPOINT_ID_LENGTH);
		KAA_RETURN_IF_NIL(source_p,);
		memcpy(source_p, source, 20);
		test_subscr_list = kaa_list_create(source_p);
	} else {
		kaa_list_t *subs_node = subscribers_list;
		while (subs_node) {
			kaa_endpoint_id_p subscriber = (kaa_endpoint_id_p) kaa_list_get_data(subs_node);
			if (!memcmp(subscriber, source, KAA_ENDPOINT_ID_LENGTH)) {
				return;
			}
			subs_node = kaa_list_next(subs_node);
		}
		source_p = (kaa_endpoint_id_p) KAA_MALLOC(KAA_ENDPOINT_ID_LENGTH);
		KAA_RETURN_IF_NIL(source_p,);
		memcpy(source_p, source, 20);
		test_subscr_list = kaa_list_push_front(subscribers_list, source_p);
	}
	if (test_subscr_list) {
		subscribers_list = test_subscr_list;
	}
	event->destroy(event);
}

void send_bulb_list_status_update(kaa_endpoint_id_p source, bool is_status_request) {
	kaa_light_event_class_family_bulb_list_status_update_t *response =
			kaa_light_event_class_family_bulb_list_status_update_create();

	kaa_list_t *bulbs_info_list = NULL;

	char i = gpios_count;
	char bulb_id[2] = { 0, 0 };
	kaa_list_t *current_node_listener = subscribers_list;
	kaa_list_t *bulb_info_node = bulbs_info_list;
	while (i--) {
		kaa_light_event_class_family_bulb_info_t *bulb_info = kaa_light_event_class_family_bulb_info_create();
		bulb_info->brightness = get_brightness(i);
		bulb_info->max_brightness = 100;
		bulb_id[0] = i;
		bulb_info->bulb_id = kaa_string_copy_create(bulb_id);
		bulb_info->status = get_bulb_state(i);
		bulb_info->ignore_brightness_update = 0;

		kaa_list_t *test_info_list = NULL;
		if (bulbs_info_list == NULL) {
			test_info_list = kaa_list_create(bulb_info);
		} else {
			test_info_list = kaa_list_push_front(bulbs_info_list, bulb_info);
		}
		if (test_info_list) {
			bulbs_info_list = test_info_list;
		}
	}

	response->bulbs = kaa_light_event_class_family_union_array_bulb_info_or_null_branch_0_create();
	response->bulbs->data = bulbs_info_list;

	if (source) {
		while (current_node_listener) {
			kaa_endpoint_id_p endpoint = (kaa_endpoint_id_p) kaa_list_get_data(current_node_listener);
			if ((!memcmp(endpoint, source, KAA_ENDPOINT_ID_LENGTH)) && !is_status_request) {
				current_node_listener = kaa_list_next(current_node_listener);
				bulb_info_node = bulbs_info_list;
				continue;
			}
			kaa_event_manager_send_kaa_light_event_class_family_bulb_list_status_update(kaa_context_->event_manager,
					response, endpoint);
			current_node_listener = kaa_list_next(current_node_listener);
			bulb_info_node = bulbs_info_list;
		}
	} else {
		kaa_event_manager_send_kaa_light_event_class_family_bulb_list_status_update(kaa_context_->event_manager,
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
	return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason) {
	printf("Kaa Demo attach failed\n");
	is_shutdown = true;
	return KAA_ERR_NONE;
}

/*
 * Initializes Kaa SDK.
 */
kaa_error_t kaa_sdk_init() {
	printf("Initializing Kaa SDK...\n");

	kaa_error_t error_code = kaa_init(&kaa_context_);
	if (error_code) {
		printf("Error during kaa context creation %d\n", error_code);
		return error_code;
	}

	error_code = kaa_tcp_channel_create(&operations_channel, kaa_context_->logger, OPERATIONS_SERVICES,
			OPERATIONS_SERVICES_COUNT);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_tcp_channel_create(&bootstrap_channel, kaa_context_->logger, BOOTSTRAP_SERVICE,
			BOOTSTRAP_SERVICE_COUNT);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_channel_manager_add_transport_channel(kaa_context_->channel_manager, &bootstrap_channel,
	NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_channel_manager_add_transport_channel(kaa_context_->channel_manager, &operations_channel,
	NULL);
	KAA_RETURN_IF_ERR(error_code);

	kaa_attachment_status_listeners_t listeners = { NULL, &kaa_on_attached, &kaa_on_detached, &kaa_on_attach_success,
			&kaa_on_attach_failed };

	error_code = kaa_user_manager_set_attachment_listeners(kaa_context_->user_manager, &listeners);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_profile_manager_set_endpoint_access_token(kaa_context_->profile_manager,
			KAA_ENDPOINT_ACCESS_TOKEN);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_user_manager_default_attach_to_user(kaa_context_->user_manager, KAA_USER_ID,
	KAA_USER_ACCESS_TOKEN);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_device_event_class_family_device_info_request_listener(
			kaa_context_->event_manager, &kaa_on_device_info_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_device_event_class_family_device_status_subscription_request_listener(
			kaa_context_->event_manager, &kaa_on_device_status_subscription_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_device_event_class_family_device_change_name_request_listener(
			kaa_context_->event_manager, &kaa_on_device_change_name_request, NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_geo_fencing_position_update_listener(
			kaa_context_->event_manager, &kaa_on_geo_fencing_position_update, NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_light_event_class_family_change_bulb_status_request_listener(
			kaa_context_->event_manager, &kaa_on_bulb_status_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_light_event_class_family_change_bulb_brightness_request_listener(
			kaa_context_->event_manager, &kaa_on_bulb_brightness_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_light_event_class_family_bulb_list_request_listener(
			kaa_context_->event_manager, &kaa_on_bulb_list_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_operation_mode_update_request_listener(
			kaa_context_->event_manager, &kaa_on_geo_fencing_event_class_family_operation_mode_update_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);

	error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_geo_fencing_status_request_listener(
			kaa_context_->event_manager, &kaa_on_geo_fencing_event_class_family_geo_fencing_status_request,
			NULL);
	KAA_RETURN_IF_ERR(error_code);
	return KAA_ERR_NONE;
}

/*
 * Kaa demo lifecycle routine.
 */
kaa_error_t kaa_demo_init() {

	device_name = (char *) KAA_MALLOC(7);
	KAA_RETURN_IF_NIL(device_name, KAA_ERR_NOMEM);
	strcpy(device_name, "BORODA");
	memset(bulbs_state, 0, gpios_count);
	load_previous_state_if_any();

	kaa_error_t error_code = kaa_sdk_init();
	if (error_code) {
		printf("Failed to init Kaa SDK. Error code : %d\n", error_code);
		return error_code;
	}

	return KAA_ERR_NONE;
}

void kaa_demo_destroy() {
	kaa_tcp_channel_disconnect(&operations_channel);
	kaa_deinit(kaa_context_);
}

int kaa_demo_event_loop() {
	kaa_error_t error_code = kaa_start(kaa_context_);
	if (error_code) {
		printf("Failed to start Kaa workflow\n");
		return -1;
	}

	uint16_t select_timeout;
	error_code = kaa_tcp_channel_get_max_timeout(&operations_channel, &select_timeout);
	if (error_code) {
		printf("Failed to get Operations channel keepalive timeout\n");
		return -1;
	}

	if (select_timeout > 3) {
		select_timeout = 3;
	}

	fd_set read_fds, write_fds, except_fds;
	int ops_fd = 0, bootstrap_fd = 0;
	struct timeval select_tv = { 0, 0 };
	int max_fd = 0;

	while (!is_shutdown) {
		FD_ZERO(&read_fds);
		FD_ZERO(&write_fds);
		FD_ZERO(&except_fds);

		max_fd = 0;

		kaa_tcp_channel_get_descriptor(&operations_channel, &ops_fd);
		if (max_fd < ops_fd)
			max_fd = ops_fd;
		kaa_tcp_channel_get_descriptor(&bootstrap_channel, &bootstrap_fd);
		if (max_fd < bootstrap_fd)
			max_fd = bootstrap_fd;

		if (kaa_tcp_channel_is_ready(&operations_channel, FD_READ))
			FD_SET(ops_fd, &read_fds);
		if (kaa_tcp_channel_is_ready(&operations_channel, FD_WRITE))
			FD_SET(ops_fd, &write_fds);

		if (kaa_tcp_channel_is_ready(&bootstrap_channel, FD_READ))
			FD_SET(bootstrap_fd, &read_fds);
		if (kaa_tcp_channel_is_ready(&bootstrap_channel, FD_WRITE))
			FD_SET(bootstrap_fd, &write_fds);

		select_tv.tv_sec = select_timeout;
		select_tv.tv_usec = 0;

		int poll_result = select(max_fd + 1, &read_fds, &write_fds, NULL, &select_tv);
		if (poll_result == 0) {
			kaa_tcp_channel_check_keepalive(&operations_channel);
			kaa_tcp_channel_check_keepalive(&bootstrap_channel);
		} else if (poll_result > 0) {
			if (bootstrap_fd >= 0) {
				if (FD_ISSET(bootstrap_fd, &read_fds)) {
					KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,
							"Processing IN event for the Bootstrap client socket %d", bootstrap_fd);
					error_code = kaa_tcp_channel_process_event(&bootstrap_channel, FD_READ);
					if (error_code)
						KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NONE,
								"Failed to process IN event for the Bootstrap client socket %d", bootstrap_fd);
				}
				if (FD_ISSET(bootstrap_fd, &write_fds)) {
					KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,
							"Processing OUT event for the Bootstrap client socket %d", bootstrap_fd);
					error_code = kaa_tcp_channel_process_event(&bootstrap_channel, FD_WRITE);
					if (error_code)
						KAA_LOG_ERROR(kaa_context_->logger, error_code,
								"Failed to process OUT event for the Bootstrap client socket %d", bootstrap_fd);
				}
			}
			if (ops_fd >= 0) {
				if (FD_ISSET(ops_fd, &read_fds)) {
					KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,
							"Processing IN event for the Operations client socket %d", ops_fd);
					error_code = kaa_tcp_channel_process_event(&operations_channel, FD_READ);
					if (error_code)
						KAA_LOG_ERROR(kaa_context_->logger, error_code,
								"Failed to process IN event for the Operations client socket %d", ops_fd);
				}
				if (FD_ISSET(ops_fd, &write_fds)) {
					KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,
							"Processing OUT event for the Operations client socket %d", ops_fd);
					error_code = kaa_tcp_channel_process_event(&operations_channel, FD_WRITE);
					if (error_code)
						KAA_LOG_ERROR(kaa_context_->logger, error_code,
								"Failed to process OUT event for the Operations client socket %d", ops_fd);
				}
			}
		} else {
			KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to poll descriptors: %s", strerror(errno));
			return -1;
		}
	}
	return 0;
}

int main(/*int argc, char *argv[]*/) {

	printf("Event demo started\n");

	init_bulbs();

	printf("Initialization completed\n");

	kaa_error_t error_code = kaa_demo_init();
	if (error_code) {
		printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
		return error_code;
	}

	int rval = kaa_demo_event_loop();

	deinit_bulbs();

	kaa_demo_destroy();

	printf("Event demo stopped\n");

	return 0;
}

