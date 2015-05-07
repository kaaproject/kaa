//
//  MusicDeviceController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/24/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "MusicDeviceController.h"

@interface MusicDeviceController ()

@property(assign,nonatomic) kaa_context_t *kaa_context;

- (void)shutDownIfError:(kaa_error_t)error;

@end

@implementation MusicDeviceController

- (instancetype)initWithKaa:(KaaController *)kaa {
    self = [super init];
    if (self) {
        self.kaa = kaa;
        self.kaa_context = kaa.kaa_context_;
        kaa_error_t error_code = kaa_event_manager_set_kaa_music_event_class_family_playback_status_update_listener(self.kaa_context->event_manager, &on_playback_status_update, (__bridge void *)(self));
        [self shutDownIfError:error_code];
        error_code = kaa_event_manager_set_kaa_music_event_class_family_play_list_response_listener(self.kaa_context->event_manager, &on_playlist_response, (__bridge void *)(self));
        [self shutDownIfError:error_code];
    }
    return self;
}

- (void)requestPlayList:(kaa_endpoint_id_p)endpointId {
    kaa_music_event_class_family_play_list_request_t *playListRequest = kaa_music_event_class_family_play_list_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_music_event_class_family_play_list_request(self.kaa_context->event_manager, playListRequest, endpointId);
    if (error_code) {
        NSLog(@"Error sending play list request, error code: %i", error_code);
    }
    playListRequest->destroy(playListRequest);
}

- (void)subscribeForUpdatesRequest:(kaa_endpoint_id_p)endpointId {
    [self.kaa subscribeForUpdates:endpointId];
}

- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId {
    [self.kaa requestGeoFencingInfo:endpointId delegate:self.geoFencingDelegate];
}

- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    [self.kaa changeGeoFencingMode:mode forEndpoint:endpointId delegate:self.geoFencingDelegate];
}

- (void)requestVolumeChange:(int)value forEndpoint:(kaa_endpoint_id_p)endpointId {
    kaa_music_event_class_family_change_volume_request_t *request = kaa_music_event_class_family_change_volume_request_create();
    request->volume = value;
    kaa_event_manager_send_kaa_music_event_class_family_change_volume_request(_kaa_context->event_manager, request, endpointId);
    request->destroy(request);
}

- (void)requestTimeChange:(int)value forEndpoint:(kaa_endpoint_id_p)endpointId {
    kaa_music_event_class_family_seek_request_t *request = kaa_music_event_class_family_seek_request_create();
    request->time = value;
    kaa_event_manager_send_kaa_music_event_class_family_seek_request(_kaa_context->event_manager, request, endpointId);
    request->destroy(request);
}

- (void)play:(NSString *)url onEndpoint:(kaa_endpoint_id_p)endpointId {
    kaa_music_event_class_family_play_request_t *request = kaa_music_event_class_family_play_request_create();
    request->url = kaa_music_event_class_family_union_string_or_null_branch_0_create();
    request->url->data = kaa_string_copy_create([url cStringUsingEncoding:NSUTF8StringEncoding]);
    kaa_event_manager_send_kaa_music_event_class_family_play_request(_kaa_context->event_manager, request, endpointId);
    request->destroy(request);
}

- (void)pause:(kaa_endpoint_id_p)endpointId {
    kaa_music_event_class_family_pause_request_t *request = kaa_music_event_class_family_pause_request_create();
    kaa_event_manager_send_kaa_music_event_class_family_pause_request(_kaa_context->event_manager, request, endpointId);
    request->destroy(request);
}

- (void)findEventListeners {
    const char *fqns[] = { PLAY_LIST_REQUEST_FQN, PLAY_REQUEST_FQN, PAUSE_REQUEST_FQN, STOP_REQUEST_FQN,
        CHANGE_VOLUME_REQUEST_FQN, SEEK_REQUEST_FQN };
    kaa_event_listeners_callback_t listeners_callback = { (__bridge void *)(self), &on_music_event_listeners, &on_music_event_listeners_failed };
    [self.kaa findEventListeners:fqns size:6 listeners:&listeners_callback];
}

- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId {
    [self.kaa requestDeviceInfo:endpointId delegate:self];
}

kaa_error_t on_music_event_listeners_failed(void *context) {
    NSLog(@"Kaa listeners not found");
    MusicDeviceController *controller = (__bridge MusicDeviceController*)context;
    if (controller.deviceDelegate && [controller.deviceDelegate respondsToSelector:@selector(noDevicesFound)]) {
        [controller.deviceDelegate noDevicesFound];
    }
    return KAA_ERR_NONE;
}

kaa_error_t on_music_event_listeners(void *context, const kaa_endpoint_id listeners[], size_t listeners_count) {
    MusicDeviceController *controller = (__bridge MusicDeviceController*)context;
    for (int i = 0; i < listeners_count; i++) {
        [controller.kaa.deviceTypesRegistry setObject:[NSNumber numberWithInt:MusicPlayerType] forKey:[Utils endpointIdToData:listeners[i]]];
        [controller.kaa requestDeviceInfo:listeners[i] delegate:controller];
        [controller requestPlayList:listeners[i]];
        [controller requestGeoFencingInfo:listeners[i]];
    }
    return KAA_ERR_NONE;
}

- (void)noDevicesFound {
    if (self.deviceDelegate && [self.deviceDelegate respondsToSelector:@selector(noDevicesFound)]) {
        [self.deviceDelegate noDevicesFound];
    }
}

- (void)foundDevice:(NSString *)name forEndpoint:(kaa_endpoint_id_p)endpoinId {
    if (self.deviceDelegate && [self.deviceDelegate respondsToSelector:@selector(foundDevice:forEndpoint:)]) {
        [self.deviceDelegate foundDevice:name forEndpoint:endpoinId];
    }
}

void on_playback_status_update(void *context, kaa_music_event_class_family_playback_status_update_t *event, kaa_endpoint_id_p source) {
    
    MusicDeviceController *controller = (__bridge MusicDeviceController *)context;
    if (controller.musicDelegate && [controller.musicDelegate respondsToSelector:@selector(onPlaybackReceived:forEndpoint:)]) {
        PlaybackStatus *playback = [[PlaybackStatus alloc] initWithStruct:event->playback_info];
        [controller.musicDelegate onPlaybackReceived:playback forEndpoint:source];
    }
    event->destroy(event);
}

void on_playlist_response(void *context, kaa_music_event_class_family_play_list_response_t *event, kaa_endpoint_id_p source) {
    MusicDeviceController *controller = (__bridge MusicDeviceController*)context;
    if (controller.musicDelegate && [controller.musicDelegate respondsToSelector:@selector(onPlayListReceived:forEndpoint:)]) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        kaa_list_t *albums = event->albums;
        kaa_music_event_class_family_album_info_t *album = NULL;
        while (albums) {
            album = (kaa_music_event_class_family_album_info_t *)kaa_list_get_data(albums);
            [array addObject:[[AlbumInfo alloc] initWithStruct:album]];
            albums = kaa_list_next(albums);
        }
        [controller.musicDelegate onPlayListReceived:array forEndpoint:source];
    }
    event->destroy(event);
}

- (void)shutDownIfError:(kaa_error_t)error {
    if (error) {
        [NSException raise:@"Error setting up music ecf listeners" format:@" Error code: %i", error];
    }
}

@end