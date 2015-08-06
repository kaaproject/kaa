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
//  SmartHouseHandler.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/7/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_configuration_manager.h>
#include <kaa/kaa_event.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_defaults.h>

#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>

#include <kaa/platform/ext_sha.h>
#include <kaa/platform/ext_transport_channel.h>
#include <kaa/platform-impl/kaa_tcp_channel.h>
#include <kaa/platform/ext_user_callback.h>
#include <kaa/gen/kaa_device_event_class_family.h>
#include <kaa/gen/kaa_geo_fencing_event_class_family.h>
#import "Utils.h"

@protocol GeoFencingEventDelegate;
@protocol DeviceEventDelegate;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE, KAA_SERVICE_USER, KAA_SERVICE_EVENT};
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

@interface KaaController : NSObject

@property(nonatomic) kaa_context_t *kaa_context_;
@property(nonatomic) kaa_transport_channel_interface_t bootstrap_channel;
@property(nonatomic) kaa_transport_channel_interface_t operations_channel;
@property(strong,nonatomic) NSMutableDictionary *deviceTypesRegistry;

- (BOOL)isInitialized;
- (void)initController;
- (void)destroyController;
- (void)attachToUserWithUsername:(NSString *)username andPassword:(NSString *)password;

- (void)findEventListeners:(const char *[])fullyQualifiedNames size:(size_t)count listeners:(kaa_event_listeners_callback_t*)callback;
- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId delegate:(id<DeviceEventDelegate>)delegate;
- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId delegate:(id<GeoFencingEventDelegate>)delegate;
- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId delegate:(id<GeoFencingEventDelegate>)delegate;
- (void)subscribeForUpdates:(kaa_endpoint_id_p)endpointId;
- (BOOL)isAttached;
- (void)renameEndpoint:(kaa_endpoint_id_p)endpointId to:(NSString*)name;

@end

@protocol GeoFencingEventDelegate <NSObject>

- (void)onStatusReceived:(kaa_geo_fencing_event_class_family_operation_mode_t)mode forEndpoint:(kaa_endpoint_id_p)endpointId;

@end

@protocol DeviceEventDelegate <NSObject>

- (void)noDevicesFound;
- (void)foundDevice:(NSString *)name forEndpoint:(kaa_endpoint_id_p)endpoinId;

@end