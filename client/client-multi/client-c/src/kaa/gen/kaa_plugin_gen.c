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
#include "../kaa_error.h"
#include "../platform/kaa_client.h"
#include "../plugins/kaa_plugin.h"
#include "kaa_plugin_gen.h"

#define KAA_PLUGIN_BOOTSTRAP_GEN 0
#define KAA_PLUGIN_META_DATA_GEN 1
#define KAA_PLUGIN_PROFILE_GEN 2
#define KAA_PLUGIN_USER_GEN 3
#define KAA_PLUGIN_LOGGING_GEN 4
#define KAA_PLUGIN_CONFIGURATION_GEN 5
#define KAA_PLUGIN_NOTIFICATION_GEN 6
#define KAA_PLUGIN_EVENT_GEN 7

extern kaa_plugin_t *kaa_bootstrap_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_meta_plugin_create(kaa_context_t *context);
extern kaa_plugin_t *kaa_profile_plugin_create(kaa_context_t *context);
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
extern kaa_plugin_t *kaa_configuration_plugin_create(kaa_context_t *context);
#endif
#ifndef KAA_DISABLE_FEATURE_EVENTS
extern kaa_plugin_t *kaa_event_plugin_create(kaa_context_t *context);
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_plugin_t *kaa_logging_plugin_create(kaa_context_t *context);
#endif
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
extern kaa_plugin_t *kaa_notification_plugin_create(kaa_context_t *context);
#endif
extern kaa_plugin_t *kaa_user_plugin_create(kaa_context_t *context);

kaa_plugin_info_t kaa_available_plugins[] = {   { KAA_PLUGIN_BOOTSTRAP_GEN, kaa_bootstrap_plugin_create }
                                              , { KAA_PLUGIN_META_DATA_GEN, kaa_meta_plugin_create }
                                              , { KAA_PLUGIN_PROFILE_GEN, kaa_profile_plugin_create }
                                                #ifndef KAA_DISABLE_FEATURE_CONFIGURATION
                                              , { KAA_PLUGIN_CONFIGURATION_GEN, kaa_configuration_plugin_create }
                                                #endif
                                                #ifndef KAA_DISABLE_FEATURE_EVENTS
                                              , { KAA_PLUGIN_EVENT_GEN, kaa_event_plugin_create }
                                                #endif
                                                #ifndef KAA_DISABLE_FEATURE_LOGGING
                                              , { KAA_PLUGIN_LOGGING_GEN, kaa_logging_plugin_create }
                                                #endif
                                                #ifndef KAA_DISABLE_FEATURE_NOTIFICATION
                                              , { KAA_PLUGIN_NOTIFICATION_GEN, kaa_notification_plugin_create }
                                                #endif
                                              , { KAA_PLUGIN_USER_GEN, kaa_user_plugin_create }
                                            };

size_t kaa_available_plugins_count = sizeof(kaa_available_plugins) / sizeof(kaa_plugin_info_t);


kaa_plugin_t *kaa_get_bootstrap_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_BOOTSTRAP_GEN, &plugin);
    }
    return plugin;
}


kaa_plugin_t *kaa_get_profile_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_PROFILE_GEN, &plugin);
    }
    return plugin;
}

kaa_plugin_t *kaa_get_user_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_USER_GEN, &plugin);
    }
    return plugin;
}

kaa_plugin_t *kaa_get_logging_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_LOGGING_GEN, &plugin);
    }
    return plugin;
}

kaa_plugin_t *kaa_get_configuration_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_CONFIGURATION_GEN, &plugin);
    }
    return plugin;
}

kaa_plugin_t *kaa_get_notification_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_NOTIFICATION_GEN, &plugin);
    }
    return plugin;
}

kaa_plugin_t *kaa_get_event_plugin(kaa_client_t *client)
{
    static kaa_plugin_t *plugin = NULL;
    if (!plugin) {
        kaa_context_t *context = kaa_client_get_context(client);
        kaa_plugin_find_by_type(context, KAA_PLUGIN_EVENT_GEN, &plugin);
    }
    return plugin;
}
