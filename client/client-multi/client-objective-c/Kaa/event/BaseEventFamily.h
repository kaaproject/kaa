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

#ifndef Kaa_BaseEventFamily_h
#define Kaa_BaseEventFamily_h

#import <Foundation/Foundation.h>

/**
 * Interface for Event Family.
 * Each EventFamily should be accessed through <EventFamilyFactory>
 */
@protocol BaseEventFamily

/**
 * Returns set of supported incoming events in event family
 *
 * @return Set of supported events presented as set of event fully qualified names
 */
- (NSSet *)getSupportedEventFQNs;

/**
* Generic handler of event received from server.
*
* @param eventFQN Fully qualified name of an event
* @param data     Event data
* @param source   Event source
*/
- (void)onGenericEvent:(NSString *)eventFQN withData:(NSData *)data fromSource:(NSString *)source;

@end

#endif
