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
 * @file kaa_base64.h
 *
 * @breaf Decode base64 encoded data
 *
 */

#ifndef KAA_BASE64_H_
#define KAA_BASE64_H_

#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Decode base64 encoded data
 *
 * @param[in]       encoded_data        Pointer to encoded data buffer
 * @param[in]       encoded_data_length Size of encoded data buffer
 * @param[out]      decoded_data        Pointer to output decoded data buffer
 * @param[in,out]   decoded_data_length Size of output buffer on [in] and size of decoded data on [out]
 *                                      base64_decode() checks that buffer for decoded data have enough length
 *                                      length ration is 3:4 (decoded/encoded)
 *
 * @return Error code.
 */
kaa_error_t kaa_base64_decode(const char *encoded_data, size_t encoded_data_length, char *decoded_data, size_t *decoded_data_length);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_BASE64_H_ */
