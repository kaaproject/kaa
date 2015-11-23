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
#include <stdbool.h>
#include <stdlib.h>
#include "kaa_plugin_gen.h"

extern kaa_plugin_t *kaa_bootstrap_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_meta_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_configuration_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_event_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_logging_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_notification_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_user_plugin_create(kaa_context_t *context);

kaa_plugin_info_t kaa_available_plugins[] = {   kaa_bootstrap_plugin_create
                                              , kaa_meta_plugin_create
                                              , kaa_configuration_plugin_create
                                              , kaa_event_plugin_create
                                              , kaa_logging_plugin_create
                                              , kaa_notification_plugin_create
                                              , kaa_user_plugin_create
                                            };

size_t kaa_available_plugins_count = sizeof(kaa_available_plugins) / sizeof(kaa_plugin_info_t);
