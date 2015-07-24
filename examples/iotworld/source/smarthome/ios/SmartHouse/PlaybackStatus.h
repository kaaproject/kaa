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
