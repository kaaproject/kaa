/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef KAA_MEMORY_LOG_STORAGE_H_
#define KAA_MEMORY_LOG_STORAGE_H_

#ifndef KAA_DISABLE_FEATURE_LOGGING

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_logging.h"

kaa_log_storage_t           * get_memory_log_storage();
kaa_storage_status_t        * get_memory_log_storage_status();
kaa_log_upload_properties_t * get_memory_log_upload_properties();

kaa_log_upload_decision_t memory_log_storage_is_upload_needed(kaa_storage_status_t *);

CLOSE_EXTERN

#endif

#endif /* KAA_MEMORY_LOG_STORAGE_H_ */
