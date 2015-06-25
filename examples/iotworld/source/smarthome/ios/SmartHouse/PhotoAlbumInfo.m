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
