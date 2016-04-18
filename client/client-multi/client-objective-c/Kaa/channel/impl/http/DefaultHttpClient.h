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

#import "AbstractHttpClient.h"

typedef enum {
    HTTP_ERROR_CODE_CANT_SERIALIZE_REQUEST  = -1,
    HTTP_ERROR_CODE_CANT_READ_RESPONSE      = -2,
    HTTP_ERROR_CODE_CANT_VERIFY_RESPONSE    = -3,
    HTTP_ERROR_CODE_CLIEN_IS_DOWN           = -4
} HttpErrorCode;

/**
 * Default implementation of HTTP client.
 */
@interface DefaultHttpClient : AbstractHttpClient

@end
