/*
 * Copyright 2015 CyberVision, Inc.
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

/*
sha.h
 Created on: Jan 15, 2015
     Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
*/

#ifndef SHA_H_
#define SHA_H_

#include "../kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif


/*
 * SHA1 hash
 */
#define SHA_1_DIGEST_LENGTH 20
typedef unsigned char kaa_digest[SHA_1_DIGEST_LENGTH];
typedef const unsigned char* kaa_digest_p;

kaa_error_t kaa_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* SHA_H_ */
