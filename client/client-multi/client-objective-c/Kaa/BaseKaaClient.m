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

#import "BaseKaaClient.h"
#import "KAADummyLog.h"
#import "KAADummyConfiguration.h"

@implementation BaseKaaClient

- (BucketRunner *)addLogRecord:(KAADummyLog *)record {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    return [self.logCollector addLogRecord:record];
}

- (KAADummyConfiguration *)getConfiguration {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    return [self.configurationManager getConfiguration];
}

@end
