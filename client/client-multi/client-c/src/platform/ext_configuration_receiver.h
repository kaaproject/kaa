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

#ifndef EXT_CONFIGURATION_RECEIVER_H_
#define EXT_CONFIGURATION_RECEIVER_H_

#include "../kaa_error.h"
#include "../gen/kaa_configuration_definitions.h"

#ifdef __cplusplus
extern "C" {
#endif


typedef kaa_error_t (*on_configuration_updated_fn)(void *context, const kaa_root_configuration_t *configuration);



typedef struct
{
    void *context;
    on_configuration_updated_fn on_configuration_updated;
} kaa_configuration_root_receiver_t;


#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* EXT_CONFIGURATION_RECEIVER_H_ */
