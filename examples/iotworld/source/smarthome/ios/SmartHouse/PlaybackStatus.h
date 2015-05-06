//
//  PlaybackStatus.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <kaa/gen/kaa_music_event_class_family.h>
#import "SongInfo.h"
#import "Utils.h"

@interface PlaybackStatus : NSObject

@property(strong,nonatomic) SongInfo* song;
@property(nonatomic) NSInteger time;
@property(nonatomic) NSInteger volume;
@property(nonatomic) NSInteger maxVolume;
@property(nonatomic) BOOL ignoreTimeUpdate;
@property(nonatomic) BOOL ignoreVolumeUpdate;
@property(nonatomic) kaa_music_event_class_family_playback_status_t status;

- (instancetype)initWithStruct:(kaa_music_event_class_family_playback_info_t*)playback;
- (NSString *)formattedTimeProgress;

@end
