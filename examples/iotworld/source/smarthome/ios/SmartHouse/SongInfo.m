//
//  SongInfo.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "SongInfo.h"

@implementation SongInfo

- (instancetype)initWithStruct:(kaa_music_event_class_family_song_info_t *)info {
    self = [super init];
    if (self) {
        self.title = [NSString stringWithCString:info->title->data encoding:NSUTF8StringEncoding];
        self.url = [NSString stringWithCString:info->url->data encoding:NSUTF8StringEncoding];
        self.duration = info->duration;
        self.albumId = [NSString stringWithCString:info->album_id->data encoding:NSUTF8StringEncoding];
    }
    return self;
}

@end