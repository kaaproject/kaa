//
//  MusicDeviceController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/24/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

@protocol DeviceEventDelegate;
@protocol MusicEventDelegate;
@protocol GeoFencingEventDelegate;

@class PlaybackStatus;
@class KaaController;

#import <Foundation/Foundation.h>
#import <kaa/gen/kaa_music_event_class_family.h>
#import "KaaController.h"
#import "PlaybackStatus.h"
#import "AlbumInfo.h"

#define PLAY_LIST_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.PlayListRequest"
#define PLAY_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.PlayRequest"
#define PAUSE_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.PauseRequest"
#define STOP_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.StopRequest"
#define CHANGE_VOLUME_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.ChangeVolumeRequest"
#define SEEK_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.music.SeekRequest"

@interface MusicDeviceController : NSObject <DeviceEventDelegate>

@property(weak,nonatomic) id<MusicEventDelegate> musicDelegate;
@property(weak,nonatomic) id<DeviceEventDelegate> deviceDelegate;
@property(weak,nonatomic) id<GeoFencingEventDelegate> geoFencingDelegate;
@property(strong,nonatomic) KaaController *kaa;

- (instancetype)initWithKaa:(KaaController*)kaa;
- (void)findEventListeners;
- (void)requestPlayList:(kaa_endpoint_id_p)endpointId;
- (void)subscribeForUpdatesRequest:(kaa_endpoint_id_p)endpointId;
- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId;
- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)requestVolumeChange:(int)value forEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)requestTimeChange:(int)value forEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)play:(NSString *)url onEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)pause:(kaa_endpoint_id_p)endpointId;
- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId;

@end

@protocol MusicEventDelegate <NSObject>

@optional
- (void)onPlayListReceived:(NSArray*)albums forEndpoint:(kaa_endpoint_id_p)endpointId;
@required
- (void)onPlaybackReceived:(PlaybackStatus*)status forEndpoint:(kaa_endpoint_id_p)endpointId;

@end