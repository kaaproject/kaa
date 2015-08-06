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
