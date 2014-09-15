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

#ifndef KAA_TEST_EXTERNAL_H_
#define KAA_TEST_EXTERNAL_H_

#include <stddef.h>
#include "kaa_external.h"

#ifdef KAA_TEST_USE_SPECIFIC_kaa_read_status_ext
#define NO_kaa_read_status_ext
#endif

#ifdef KAA_TEST_USE_SPECIFIC_kaa_store_status_ext
#define NO_kaa_store_status_ext
#endif

#ifdef KAA_TEST_USE_SPECIFIC_kaa_get_endpoint_public_key
#define NO_kaa_get_endpoint_public_key
#endif

#endif /* KAA_TEST_EXTERNAL_H_ */
