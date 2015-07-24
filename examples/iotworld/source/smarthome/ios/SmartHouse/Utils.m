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

//
//  Utils.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/20/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "Utils.h"

@implementation Utils

+ (UIImage *)imageFromBytes:(kaa_bytes_t *)bytes {
    if (!bytes) {
        return nil;
    }
    NSData *data = [NSData dataWithBytes:bytes->buffer length:bytes->size];
    return [UIImage imageWithData:data];
}

+ (NSString *)millisToMinutesTime:(long)millis {
    long minutes = millis / 60000;
    long secods = (millis / 1000) % 60;
    return [NSString stringWithFormat:@"%lu:%02lu", minutes, secods];
}

+ (NSData *)endpointIdToData:(kaa_endpoint_id_p)endpointId {
    return [NSData dataWithBytes:endpointId length:KAA_ENDPOINT_ID_LENGTH];
}

+ (kaa_endpoint_id_p)dataToEndpointId:(NSData *)data {
    return (kaa_endpoint_id_p)[data bytes];
}

+ (NSString *)geoFencingModeToString:(kaa_geo_fencing_event_class_family_operation_mode_t)mode {
    switch (mode) {
        case ENUM_OPERATION_MODE_ON:
            return @"On";
        case ENUM_OPERATION_MODE_OFF:
            return @"Off";
        case ENUM_OPERATION_MODE_GEOFENCING:
            return @"Auto";
        default:
            return @"On";
    }
}

+ (NSArray *)geoFencingModesWithCurrent:(kaa_geo_fencing_event_class_family_operation_mode_t)mode {
    NSString* array[3] = {@"Auto", @"Off", @"On"};
    array[mode] = [NSString stringWithFormat:@"     %@ \uE235", array[mode]];
    NSMutableArray *modes = [[NSMutableArray alloc] init];
    for (int i = 0; i < 3; i++) {
        [modes addObject:array[i]];
    }
    return modes;
}

@end