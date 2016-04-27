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
#import "KaaClientState.h"
#import "KAABase64.h"
#import "KaaClientProperties.h"

#define STATE_FILE_DEFAULT  @"state.properties"

/**
 * Default implementation of KaaClientState protocol that uses file system
 * to store Kaa client state.
 */
@interface KaaClientPropertiesState : NSObject <KaaClientState>

- (instancetype)initWithBase64:(id<KAABase64>)base64 clientProperties:(KaaClientProperties *)properties;

@end
