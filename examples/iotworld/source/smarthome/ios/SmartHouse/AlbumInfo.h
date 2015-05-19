//
//  AlbumInfoWrap.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/22/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <kaa/gen/kaa_music_event_class_family.h>
#import "Utils.h"

@interface AlbumInfo : NSObject

@property(strong, nonatomic) NSString* title;
@property(strong, nonatomic) NSString* artist;
@property(strong, nonatomic) NSString* albumId;
@property(strong, nonatomic) UIImage* cover;
@property(strong, nonatomic) NSMutableArray* songs;

- (instancetype)initWithStruct:(kaa_music_event_class_family_album_info_t*)info;

@end