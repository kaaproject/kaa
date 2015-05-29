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
