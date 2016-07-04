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


#ifndef CC32XX_DEFAULTS_H_
#define CC32XX_DEFAULTS_H_

#define KAA_TCP_CHANNEL_IN_BUFFER_SIZE      2048
#define KAA_TCP_CHANNEL_OUT_BUFFER_SIZE     8192

#define KAA_TCP_CHANNEL_MAX_TIMEOUT         200u
#define KAA_TCP_CHANNEL_PING_TIMEOUT        (KAA_TCP_CHANNEL_MAX_TIMEOUT / 2)

#define KAATCP_PARSER_MAX_MESSAGE_LENGTH    1024
#define KAA_MAX_LOG_MESSAGE_LENGTH          512

#endif /* CC32XX_DEFAULTS_H_ */
