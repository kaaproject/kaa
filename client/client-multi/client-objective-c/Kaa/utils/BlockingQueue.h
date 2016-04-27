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

@interface BlockingQueue : NSObject

/**
 * Inserts the specified element into this queue.
 */
- (void)offer:(id)object;

/**
 * Retrieves and removes the head of this queue, 
 * waiting if necessary until an element becomes available.
 */
- (id)take;

/**
 * Removes all available elements from this queue and adds them
 * to the given collection.  This operation may be more
 * efficient than repeatedly polling this queue.
 */
- (void)drainTo:(NSMutableArray *)array;

/**
 * Returns amount of objects in blocking queue.
 */
- (NSUInteger)size;

@end
