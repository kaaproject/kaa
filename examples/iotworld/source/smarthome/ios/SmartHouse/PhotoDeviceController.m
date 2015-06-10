//
//  PhotoDeviceController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/28/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PhotoDeviceController.h"

@interface PhotoDeviceController ()

@property(assign,nonatomic) kaa_context_t *kaa_context;
@property(strong,nonatomic) KaaController *kaa;

- (void)shutDownIfError:(kaa_error_t)error;

@end

@implementation PhotoDeviceController

- (instancetype)initWithKaa:(KaaController *)kaa {
    self = [super init];
    if (self) {
        self.kaa = kaa;
        self.kaa_context = kaa.kaa_context_;
        kaa_error_t error_code = kaa_event_manager_set_kaa_photo_event_class_family_photo_albums_response_listener(self.kaa_context->event_manager, &on_photo_albums_response, (__bridge void *)(self));
        [self shutDownIfError:error_code];
        error_code = kaa_event_manager_set_kaa_photo_event_class_family_photo_frame_status_update_listener(self.kaa_context->event_manager, &on_photo_frame_status_update, (__bridge void *)(self));
        [self shutDownIfError:error_code];
    }
    return self;
}

- (void)findEventListeners {
    const char *fqns[] = { PHOTO_ALBUM_REQUEST_FQN, PHOTO_UPLOAD_REQUEST_FQN, START_SLIDESHOW_REQUEST_FQN, PAUSE_SLIDESHOW_REQUEST_FQN, DELETE_PHOTOS_REQUEST_FQN };
    kaa_event_listeners_callback_t listeners_callback = { (__bridge void *)(self), &on_photo_event_listeners, &on_photo_event_listeners_failed };
    [self.kaa findEventListeners:fqns size:5 listeners:&listeners_callback];
}

- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId {
    [self.kaa requestDeviceInfo:endpointId delegate:self];
}

kaa_error_t on_photo_event_listeners_failed(void *context) {
    NSLog(@"Kaa listeners not found");
    PhotoDeviceController *controller = (__bridge PhotoDeviceController*)context;
    if (controller.deviceDelegate && [controller.deviceDelegate respondsToSelector:@selector(noDevicesFound)]) {
        [controller.deviceDelegate noDevicesFound];
    }
    return KAA_ERR_NONE;
}

kaa_error_t on_photo_event_listeners(void *context, const kaa_endpoint_id listeners[], size_t listeners_count) {
    PhotoDeviceController *controller = (__bridge PhotoDeviceController*)context;
    for (int i = 0; i < listeners_count; i++) {
        [controller.kaa.deviceTypesRegistry setObject:[NSNumber numberWithInt:PhotoFrameType] forKey:[Utils endpointIdToData:listeners[i]]];
        [controller.kaa requestDeviceInfo:listeners[i] delegate:controller];
        [controller albumsRequest:listeners[i]];
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

- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId {
    [self.kaa requestGeoFencingInfo:endpointId delegate:self.geoFencingDelegate];
}

- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    [self.kaa changeGeoFencingMode:mode forEndpoint:endpointId delegate:self.geoFencingDelegate];
}

- (void)deletePhotos:(kaa_endpoint_id_p)endpointId {
    kaa_photo_event_class_family_delete_uploaded_photos_request_t *request = kaa_photo_event_class_family_delete_uploaded_photos_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_photo_event_class_family_delete_uploaded_photos_request(self.kaa_context->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error trying to delete uploaded photos, error code: %i", error_code);
    }
    request->destroy(request);
}

- (void)albumsRequest:(kaa_endpoint_id_p)endpointId {
    kaa_photo_event_class_family_photo_albums_request_t *request = kaa_photo_event_class_family_photo_albums_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_photo_event_class_family_photo_albums_request(self.kaa_context->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error trying to get albums list, error code: %i", error_code);
    }
    request->destroy(request);
}

- (void)uploadPhoto:(NSData*)photo withName:(NSString*)name toEndpoint:(kaa_endpoint_id_p)endpointId {
    kaa_photo_event_class_family_photo_upload_request_t *request = kaa_photo_event_class_family_photo_upload_request_create();
    request->name = kaa_string_copy_create([name cStringUsingEncoding:NSUTF8StringEncoding]);
    request->body = kaa_bytes_copy_create([photo bytes], [photo length]);
    kaa_error_t error_code = kaa_event_manager_send_kaa_photo_event_class_family_photo_upload_request(self.kaa_context->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error trying to upload photo named %@, error code: %i", name, error_code);
    }
    request->destroy(request);
}

- (void)startSlideshow:(NSString*)albumId enpoint:(kaa_endpoint_id_p)endpointId {
    kaa_photo_event_class_family_start_slide_show_request_t *request = kaa_photo_event_class_family_start_slide_show_request_create();
    request->album_id = kaa_string_copy_create([albumId cStringUsingEncoding:NSUTF8StringEncoding]);
    kaa_error_t error_code = kaa_event_manager_send_kaa_photo_event_class_family_start_slide_show_request(self.kaa_context->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error trying to start slideshow with album: %@, error code: %i", albumId, error_code);
    }
    request->destroy(request);
}

- (void)pauseSlideshow:(kaa_endpoint_id_p)endpointId {
    kaa_photo_event_class_family_pause_slide_show_request_t *request = kaa_photo_event_class_family_pause_slide_show_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_photo_event_class_family_pause_slide_show_request(self.kaa_context->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error trying to pause slideshow, error code: %i", error_code);
    }
    request->destroy(request);
}

- (void)subscribeForUpdatesRequest:(kaa_endpoint_id_p)endpointId {
    [self.kaa subscribeForUpdates:endpointId];
}

void on_photo_albums_response(void *context, kaa_photo_event_class_family_photo_albums_response_t *event, kaa_endpoint_id_p source) {
    PhotoDeviceController *controller = (__bridge PhotoDeviceController *)context;
    if (controller.photoFrameDelegate && [controller.photoFrameDelegate respondsToSelector:@selector(onAlbumsReceived:fromEndpoint:)]) {
        NSMutableArray *albums = [[NSMutableArray alloc] init];
        kaa_list_t *albumsList = event->albums;
        kaa_photo_event_class_family_photo_album_info_t *album = NULL;
        while (albumsList) {
            album = (kaa_photo_event_class_family_photo_album_info_t *)kaa_list_get_data(albumsList);
            [albums addObject:[[PhotoAlbumInfo alloc] initWithStruct:album]];
            albumsList = kaa_list_next(albumsList);
        }
        [controller.photoFrameDelegate onAlbumsReceived:albums fromEndpoint:source];
    }
    event->destroy(event);
}

void on_photo_frame_status_update(void *context, kaa_photo_event_class_family_photo_frame_status_update_t *event, kaa_endpoint_id_p source) {
    PhotoDeviceController *controller = (__bridge PhotoDeviceController *)context;
    if (controller.photoFrameDelegate && [controller.photoFrameDelegate respondsToSelector:@selector(onAlbumStatusUpdate:fromEndpoint:)]) {
        [controller.photoFrameDelegate onAlbumStatusUpdate:[[PhotoFrameStatus alloc] initWithStruct:event] fromEndpoint:source];
    }
    event->destroy(event);
}

- (void)shutDownIfError:(kaa_error_t)error {
    if (error) {
        [NSException raise:@"Error setting up photo ecf listeners" format:@" Error code: %i", error];
    }
}

@end
