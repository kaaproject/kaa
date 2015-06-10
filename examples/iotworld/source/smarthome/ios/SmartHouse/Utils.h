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