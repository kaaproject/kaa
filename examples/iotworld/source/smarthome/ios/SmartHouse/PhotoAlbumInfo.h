//
//  PhotoAlbumInfo.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/29/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#include <kaa/gen/kaa_photo_event_class_family.h>
#import "Utils.h"

@interface PhotoAlbumInfo : NSObject

@property(strong,nonatomic) NSString *albumId;
@property(strong,nonatomic) NSString *title;
@property(nonatomic) NSInteger size;
@property(strong,nonatomic) UIImage *thumbnail;

- (instancetype)initWithStruct:(kaa_photo_event_class_family_photo_album_info_t *)album;

@end
