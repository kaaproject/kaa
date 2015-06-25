//
//  SongInfo.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <kaa/gen/kaa_music_event_class_family.h>

@interface SongInfo : NSObject

@property(strong,nonatomic) NSString* title;
@property(nonatomic) NSInteger duration;
@property(strong,nonatomic) NSString* url;
@property(strong,nonatomic) NSString* albumId;

- (instancetype)initWithStruct:(kaa_music_event_class_family_song_info_t*)info;

@end
