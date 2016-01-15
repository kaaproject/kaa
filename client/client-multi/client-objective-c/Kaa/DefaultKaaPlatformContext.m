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

#import "DefaultKaaPlatformContext.h"
#import "DefaultHttpClient.h"
#import "SimpleExecutorContext.h"

@interface DefaultKaaPlatformContext ()

@property (nonatomic,strong) KaaClientProperties *props;
@property (nonatomic,strong) id<ExecutorContext> exContext;

@end

@implementation DefaultKaaPlatformContext

- (instancetype)init {
    self = [super init];
    if (self) {
        _props = nil;
        _exContext = [[SimpleExecutorContext alloc] init];
    }
    return self;
}

- (instancetype)initWith:(KaaClientProperties *)properties andExecutor:(id<ExecutorContext>)executor {
    self = [super init];
    if (self) {
        _props = properties;
        _exContext = executor;
    }
    return self;
}

- (AbstractHttpClient *)createHttpClient:(NSString *)url
                              privateKey:(SecKeyRef)privateK
                               publicKey:(SecKeyRef)publicK
                               remoteKey:(NSData *)remoteK {
    return [[DefaultHttpClient alloc] initWith:url privateKey:privateK publicKey:publicK remoteKey:remoteK];
}

- (id<KAABase64>)getBase64 {
    return [[CommonBase64 alloc] init];
}

- (ConnectivityChecker *)createConnectivityChecker {
    return [[ConnectivityChecker alloc] init];
}

- (id<ExecutorContext>)getExecutorContext {
    return _exContext;
}

- (KaaClientProperties *)getProperties {
    return _props;
}

@end
