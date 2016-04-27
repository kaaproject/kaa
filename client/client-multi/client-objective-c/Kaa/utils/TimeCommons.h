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

typedef enum {
    TIME_UNIT_MILLISECONDS,
    TIME_UNIT_SECONDS,
    TIME_UNIT_MINUTES
} TimeUnit;

@interface TimeUtils : NSObject

/**
 * Used to convert TimeUnit values
 * @return converted value or -1 if params were invalid
 */
+ (int64_t)convertValue:(int64_t)value fromTimeUnit:(TimeUnit)fromUnit toTimeUnit:(TimeUnit)toUnit;

@end
