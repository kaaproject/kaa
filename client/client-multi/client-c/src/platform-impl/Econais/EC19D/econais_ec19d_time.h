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


#ifndef ECONAIS_EC19D_TIME_H_
#define ECONAIS_EC19D_TIME_H_

#include <sndc_sys_api.h>

typedef uint32_t kaa_time_t;

#define KAA_TIME() (kaa_time_t)sndc_sys_getTimestamp_msec()

#endif /* ECONAIS_EC19D_TIME_H_ */
