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
//  AlbumInfoWrap.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "AlbumInfo.h"
#import "SongInfo.h"

@interface AlbumInfo ()

- (NSMutableArray *)reversedArray:(NSArray *)input;

@end

@implementation AlbumInfo

- (instancetype)initWithStruct:(kaa_music_event_class_family_album_info_t *)info {
    self = [super init];
    if (self) {
        self.title = [NSString stringWithCString:info->title->data encoding:NSUTF8StringEncoding];
        self.artist = [NSString stringWithCString:info->artist->data encoding:NSUTF8StringEncoding];
        self.albumId = [NSString stringWithCString:info->album_id->data encoding:NSUTF8StringEncoding];
        self.songs = [[NSMutableArray alloc] init];
        if (info->cover) {
            self.cover = [Utils imageFromBytes:info->cover];
        } else {
            self.cover = [UIImage imageNamed:@"no_image"];
        }
        kaa_list_t *songList = info->songs;
        kaa_music_event_class_family_song_info_t *song = NULL;
        while (songList) {
            song = (kaa_music_event_class_family_song_info_t *)kaa_list_get_data(songList);
            [self.songs addObject:[[SongInfo alloc] initWithStruct:song]];
            songList = kaa_list_next(songList);
        }
        self.songs = [self reversedArray:self.songs];
    }
    return self;
}

- (NSMutableArray *)reversedArray:(NSArray *)input {
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:[input count]];
    NSEnumerator *enumerator = [input reverseObjectEnumerator];
    for (id element in enumerator) {
        [array addObject:element];
    }
    return array;
}

@end