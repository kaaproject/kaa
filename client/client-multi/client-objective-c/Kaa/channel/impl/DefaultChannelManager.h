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
#import "KaaInternalChannelManager.h"
#import "BootstrapManager.h"
#import "ExecutorContext.h"
#import "FailureDelegate.h"

@interface DefaultChannelManager : NSObject <KaaInternalChannelManager>

- (instancetype)initWithBootstrapManager:(id<BootstrapManager>)bootstrapMgr
                        bootstrapServers:(NSDictionary *)servers
                                 context:(id<ExecutorContext>)context
                         failureDelegate:(id<FailureDelegate>)delegate;

@end

@interface SyncWorker : NSThread

@property (nonatomic, strong) id<KaaDataChannel> channel;
@property (nonatomic, weak) DefaultChannelManager *manager;
@property (nonatomic) volatile BOOL isStopped;

- (instancetype)initWithChannel:(id<KaaDataChannel>)channel
                        manager:(DefaultChannelManager *)manager;

@end
