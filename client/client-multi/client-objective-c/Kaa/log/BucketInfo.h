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

@interface BucketInfo : NSObject

/**
 * Returns the timestamp in milliseconds indicating when log bucket was scheduled for delivery.
 */
@property (nonatomic) double scheduledBucketTimestamp;

/**
 * Returns the total time in milliseconds spent to deliver log bucket.
 */
@property (nonatomic) double bucketDeliveryDuration;


@property (nonatomic, readonly) int32_t bucketId;
@property (nonatomic, readonly) int32_t logCount;

- (instancetype)initWithBucketId:(int32_t)bucketId logCount:(int32_t)logCount;

@end
