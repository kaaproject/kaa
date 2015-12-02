/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#import "ExecutorContext.h"
#import "TimeCommons.h"

@interface AbstractExecutorContext : NSObject <ExecutorContext>

@property (nonatomic) NSInteger timeOut;
@property (nonatomic) TimeUnit timeUnit;

- (instancetype)initWithTimeOut:(NSInteger)timeOut andTimeUnit:(TimeUnit)timeUnit;
- (void)shutDownExecutor:(NSOperationQueue*)queue;

@end
