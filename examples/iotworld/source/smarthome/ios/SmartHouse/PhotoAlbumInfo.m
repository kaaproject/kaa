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
//  PhotoAlbumInfo.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/29/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PhotoAlbumInfo.h"

@implementation PhotoAlbumInfo

- (instancetype)initWithStruct:(kaa_photo_event_class_family_photo_album_info_t *)album {
    self = [super init];
    if (self) {
        self.albumId = [NSString stringWithCString:album->id->data encoding:NSUTF8StringEncoding];
        self.title = [NSString stringWithCString:album->title->data encoding:NSUTF8StringEncoding];
        self.size = album->size;
        self.thumbnail = [Utils imageFromBytes:album->thumbnail];
    }
    return self;
}

@end
