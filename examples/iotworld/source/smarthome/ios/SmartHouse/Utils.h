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
//  Utils.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/20/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#include <kaa/gen/kaa_geo_fencing_event_class_family.h>

@interface Utils : NSObject

+ (UIImage *)imageFromBytes:(kaa_bytes_t *)bytes;
+ (NSString *)millisToMinutesTime:(long)millis;
+ (NSData *)endpointIdToData:(kaa_endpoint_id_p)endpointId;
+ (kaa_endpoint_id_p)dataToEndpointId:(NSData *)data;
+ (NSString *)geoFencingModeToString:(kaa_geo_fencing_event_class_family_operation_mode_t)mode;
+ (NSArray *)geoFencingModesWithCurrent:(kaa_geo_fencing_event_class_family_operation_mode_t)mode;

@end

typedef enum {
    MusicPlayerType,
    PhotoFrameType,
    LightControlType,
    UnknownType
} DeviceType;