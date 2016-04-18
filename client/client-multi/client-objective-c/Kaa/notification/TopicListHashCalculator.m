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

#import "TopicListHashCalculator.h"
#import "EndpointGen.h"

#define EMPTRY_LIST_HASH    1

@implementation TopicListHashCalculator

+ (int32_t)calculateTopicListHash:(NSArray *)topics {
    if (topics.count == 0) {
        return NIL_LIST_HASH;
    }
    
    uint32_t result = EMPTRY_LIST_HASH;
    if (topics.count != 0) {
        NSArray *newTopics = [topics sortedArrayUsingComparator:^NSComparisonResult(Topic *o1, Topic *o2) {
            if (o1.id > o2.id)
                return NSOrderedDescending;
            if (o1.id < o2.id)
                return NSOrderedAscending;
            else
                return NSOrderedSame;
        }];
        
        for (Topic *topic in newTopics) {
            uint64_t topicId = topic.id;
            result = 31 * result + (uint32_t) (topicId ^ (topicId >> 32));
        }
    }
    return (int32_t)result;
}

@end
