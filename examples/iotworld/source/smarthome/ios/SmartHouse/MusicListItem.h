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
//  MusicListItem.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/17/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <kaa/gen/kaa_device_event_class_family_definitions.h>
#include <kaa/gen/kaa_music_event_class_family_definitions.h>

@interface MusicListItem : NSObject

@property(strong,nonatomic) NSString *name;
@property(assign,nonatomic) kaa_music_event_class_family_playback_info_t *playbackInfo;
@property(assign,nonatomic) kaa_endpoint_id_p *endpointId;

@end
