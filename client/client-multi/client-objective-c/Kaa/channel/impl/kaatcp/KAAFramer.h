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

#import <Foundation/Foundation.h>
#import "KAATcpDelegates.h"

/**
 * KaaTcp Framer class.
 * Used to cut incoming byte stream into MQTT frames, and deliver frames to MqttFrameDelegate.
 * Framer Class typically used from MessageFactory class.
 */
@interface KAAFramer : NSObject

- (void)registerFrameDelegate:(id<MqttFrameDelegate>)delegate;

/**
 * Process incoming bytes stream.
 * Assumes that data is unprocessed.
 * In case of previous call eaten not all data on next iterations
 *  data should start from unprocessed bytes.
 * @param data Data to push
 * @return Number of bytes processed from this data.
 * @exception KaaTcpProtocolException In case of protocol errors.
 */
- (int32_t)pushBytes:(NSMutableData *)data;

/**
 * Reset Framer state by dropping current frame.
 */
- (void)flush;

@end
