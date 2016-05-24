/*
 * Copyright 2014-2016 CyberVision, Inc.
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

/** @file
 * This file defines all extensions for the current client
 * instance. Currently, this file is static but will be auto-generated
 * in the future.
 *
 * kaa_extension.c MUST use this file. That's a requirement, so we can
 * easily replace this file with auto-generated one in the future.
 *
 * @warning this file is a private part of the kaa_extension.c and
 * must only be used from that file. If you need extension instance,
 * use interface defined in the kaa_extension.h. (That is the reason
 * why the file doesn't have include guard.)
 */
#include <kaa_common.h>

#include "kaa_private.h"

// Meta data extension is not really an extension. It doesn't have any
// context and its implementation is embedded in platfrom_protocol.

// They will be extern, thus prefixed.
static struct kaa_extension kaa_extension_bootstrap = {
    .id = KAA_EXTENSION_BOOTSTRAP,
    .init = kaa_extension_bootstrap_init,
    .deinit = kaa_extension_bootstrap_deinit,
    .request_serialize = kaa_extension_bootstrap_request_serialize,
    .server_sync = kaa_extension_bootstrap_server_sync,
};

#ifndef KAA_DISABLE_FEATURE_PROFILE
static struct kaa_extension kaa_extension_profile = {
    .id = KAA_EXTENSION_PROFILE,
    .init = kaa_extension_profile_init,
    .deinit = kaa_extension_profile_deinit,
    .request_serialize = kaa_extension_profile_request_serialize,
    .server_sync = kaa_extension_profile_server_sync,
};
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS
static struct kaa_extension kaa_extension_event = {
    .id = KAA_EXTENSION_EVENT,
    .init = kaa_extension_event_init,
    .deinit = kaa_extension_event_deinit,
    .request_serialize = kaa_extension_event_request_serialize,
    .server_sync = kaa_extension_event_server_sync,
};
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
static struct kaa_extension kaa_extension_logging = {
    .id = KAA_EXTENSION_LOGGING,
    .init = kaa_extension_logging_init,
    .deinit = kaa_extension_logging_deinit,
    .request_serialize = kaa_extension_logging_request_serialize,
    .server_sync = kaa_extension_logging_server_sync,
};
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
static struct kaa_extension kaa_extension_configuration = {
    .id = KAA_EXTENSION_CONFIGURATION,
    .init = kaa_extension_configuration_init,
    .deinit = kaa_extension_configuration_deinit,
    .request_serialize = kaa_extension_configuration_request_serialize,
    .server_sync = kaa_extension_configuration_server_sync,
};
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
static struct kaa_extension kaa_extension_notification = {
    .id = KAA_EXTENSION_NOTIFICATION,
    .init = kaa_extension_notification_init,
    .deinit = kaa_extension_notification_deinit,
    .request_serialize = kaa_extension_notification_request_serialize,
    .server_sync = kaa_extension_notification_server_sync,
};
#endif

#ifndef KAA_DISABLE_FEATURE_USER
static struct kaa_extension kaa_extension_user = {
    .id = KAA_EXTENSION_USER,
    .init = kaa_extension_user_init,
    .deinit = kaa_extension_user_deinit,
    .request_serialize = kaa_extension_user_request_serialize,
    .server_sync = kaa_extension_user_server_sync,
};
#endif

// I'm not sure in what order they should be, so just kept order from
// kaa_context_init().
static const struct kaa_extension *kaa_extensions[] = {
    &kaa_extension_bootstrap,

#ifndef KAA_DISABLE_FEATURE_PROFILE
    &kaa_extension_profile,
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS
    &kaa_extension_event,
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
    &kaa_extension_logging,
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
    &kaa_extension_configuration,
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
    &kaa_extension_notification,
#endif

#ifndef KAA_DISABLE_FEATURE_USER
    &kaa_extension_user,
#endif
};
