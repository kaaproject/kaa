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

#ifndef KAA_UUID_H_
#define KAA_UUID_H_

#include <stdint.h>

#define KAA_UUID_T          uint32_t
#define KAA_UUID_VALUE_T    uint32_t

typedef KAA_UUID_T kaa_uuid_t;

void        kaa_uuid_fill(kaa_uuid_t *dst, KAA_UUID_VALUE_T src);
void        kaa_uuid_copy(kaa_uuid_t *dst, kaa_uuid_t *src);
void        kaa_uuid_to_string(char **dst, kaa_uuid_t *uuid);
void        kaa_uuid_from_string(const char *src, kaa_uuid_t *uuid);
int         kaa_uuid_compare(kaa_uuid_t *uuid1, kaa_uuid_t *uuid2);

#endif /* KAA_UUID_H_ */
