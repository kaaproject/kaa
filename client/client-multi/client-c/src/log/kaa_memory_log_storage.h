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
#endif

#include "kaa_logging.h"

#include "utilities/kaa_log.h"
void                          set_memory_log_storage_logger(kaa_logger_t *);


kaa_log_storage_t           * get_memory_log_storage();

#ifdef __cplusplus
} // extern "C"
#endif

#endif

#endif /* KAA_MEMORY_LOG_STORAGE_H_ */
