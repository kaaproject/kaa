//
//  PhotoDeviceController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/28/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

@protocol PhotoFrameDelegate;

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "KaaController.h"
#import "PhotoAlbumInfo.h"
#import "PhotoFrameStatus.h"
#import <kaa/gen/kaa_photo_event_class_family.h>
#import "Utils.h"

#define PHOTO_ALBUM_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest"
#define PHOTO_UPLOAD_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest"
#define START_SLIDESHOW_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest"
#define PAUSE_SLIDESHOW_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest"
#define DELETE_PHOTOS_REQUEST_FQN "org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest"

@interface PhotoDeviceController : NSObject <DeviceEventDelegate>

@property(weak,nonatomic) id<PhotoFrameDelegate> photoFrameDelegate;
@property(weak,nonatomic) id<DeviceEventDelegate> deviceDelegate;
@property(weak,nonatomic) id<GeoFencingEventDelegate> geoFencingDelegate;

- (instancetype)initWithKaa:(KaaController *)kaa;
- (void)findEventListeners;
- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId;
- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)deletePhotos:(kaa_endpoint_id_p)endpointId;
- (void)albumsRequest:(kaa_endpoint_id_p)endpointId;
- (void)uploadPhoto:(NSData*)photo withName:(NSString*)name toEndpoint:(kaa_endpoint_id_p)endpointId;
- (void)startSlideshow:(NSString*)albumId enpoint:(kaa_endpoint_id_p)endpointId;
- (void)pauseSlideshow:(kaa_endpoint_id_p)endpointId;
- (void)subscribeForUpdatesRequest:(kaa_endpoint_id_p)endpointId;
- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId;


@end

@protocol PhotoFrameDelegate <NSObject>

@optional
- (void)onAlbumsReceived:(NSArray*)albums fromEndpoint:(kaa_endpoint_id_p)endpointId;
@required
- (void)onAlbumStatusUpdate:(PhotoFrameStatus*)status fromEndpoint:(kaa_endpoint_id_p)endpointId;

@end