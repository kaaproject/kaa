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

# ifndef KAA_NOTIFICATION_DEFINITIONS_H_
# define KAA_NOTIFICATION_DEFINITIONS_H_

# include "kaa_notification_gen.h"

# ifdef __cplusplus
extern "C" {
# endif

typedef kaa_notification_notification_t kaa_notification_t;
# define KAA_NOTIFICATION_DESERIALIZE(reader)  kaa_notification_notification_deserialize(reader)

# ifdef __cplusplus
}      /* extern "C" */
# endif

# endif /* KAA_NOTIFICATION_DEFINITIONS_H_ */
