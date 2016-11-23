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

/*
* @file ext_sha.h
* @brief External SHA functions.
*/

#ifndef EXT_SHA_H_
#define EXT_SHA_H_

#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif



#define SHA_1_DIGEST_LENGTH 20
typedef unsigned char kaa_digest[SHA_1_DIGEST_LENGTH];
typedef unsigned char* kaa_digest_p;

/*
 * @brief SHA1 hash calculation function.
 * SHA1 hash calculation function.
 * @param[in]   data        Date for which SHA1 calculates.
 * @param[in]   data_size   Size of data.
 * @param[out]  digest      SHA1 calculated digest.
 *
 * @return kaa_error_t Error code.
 */
kaa_error_t ext_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest);

/*
 * @brief Copy SHA1 digest.
 * Copy SHA1 digest from src to dst.
 * @param[out]  dst     Destination SHA1 digest.
 * @param[in]   src     Source SHA1 digest.
 *
 * @return Error code.
 */
kaa_error_t ext_copy_sha_hash(kaa_digest_p dst, const kaa_digest_p src);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* EXT_SHA_H_ */
