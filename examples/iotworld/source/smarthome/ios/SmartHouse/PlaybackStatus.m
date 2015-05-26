//
//  PlaybackStatus.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PlaybackStatus.h"

@implementation PlaybackStatus

- (instancetype)initWithStruct:(kaa_music_event_class_family_playback_info_t *)playback {
    self = [super init];
    if (self) {
        if (playback->song->type == KAA_MUSIC_EVENT_CLASS_FAMILY_UNION_SONG_INFO_OR_NULL_BRANCH_0) {
            kaa_music_event_class_family_song_info_t *song = (kaa_music_event_class_family_song_info_t*)playback->song->data;
            self.song = [[SongInfo alloc] initWithStruct: song];
        }
        self.time = playback->time;
        self.volume = playback->volume;
        self.maxVolume = playback->max_volume;
        self.ignoreTimeUpdate = playback->ignore_time_update ? YES : NO;
        self.ignoreVolumeUpdate = playback->ignore_volume_update ? YES : NO;
        self.status = playback->status;
    }
    return self;
}

- (NSString *)formattedTimeProgress {
    return [NSString stringWithFormat:@"%@/%@", [Utils millisToMinutesTime:self.time], [Utils millisToMinutesTime:self.song.duration]];
}

@end