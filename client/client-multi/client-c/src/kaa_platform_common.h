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

#ifndef KAA_PLATFORM_COMMON_H_
#define KAA_PLATFORM_COMMON_H_

#ifdef __cplusplus
extern "C" {
#endif

#define KAA_ALIGNMENT               4

#define KAA_MESSAGE_HEADER_SIZE     8 // in bytes
#define KAA_EXTENSION_HEADER_SIZE   8 // in bytes

/**
 * Extension ID description
 */
#define KAA_META_DATA_EXTENSION_TYPE       1
#define KAA_PROFILE_EXTENSION_TYPE         2
#define KAA_USER_EXTENSION_TYPE            3
#define KAA_LOGGING_EXTENSION_TYPE         4
#define KAA_EVENT_EXTENSION_TYPE           7
/*
 * Not yet implemented
 */
#define KAA_CONFIGURATION_EXTENSION_TYPE   5
#define KAA_NOTIFICATION_EXTENSION_TYPE    6
#define KAA_REDIRECT_EXTENSION_TYPE        8

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* SRC_KAA_PLATFORM_COMMON_H_ */
