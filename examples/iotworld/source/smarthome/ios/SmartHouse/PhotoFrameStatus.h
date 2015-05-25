//
//  PhotoFrameStatus.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/29/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#include <kaa/gen/kaa_photo_event_class_family.h>
#import "Utils.h"

@interface PhotoFrameStatus : NSObject

@property(strong,nonatomic) NSString* albumId;
@property(nonatomic)kaa_photo_event_class_family_slide_show_status_t status;
@property(nonatomic) NSInteger photoNumber;
@property(strong,nonatomic) UIImage *thumbnail;

- (instancetype)initWithStruct:(kaa_photo_event_class_family_photo_frame_status_update_t *)status;

@end
