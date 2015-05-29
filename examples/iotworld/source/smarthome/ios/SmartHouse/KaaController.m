//
//  SmartHouseHandler.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/7/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "KaaController.h"

@interface KaaController ()

@property(nonatomic) BOOL isShutdown;
@property(nonatomic) BOOL isInitialized;
@property(weak,nonatomic) id<DeviceEventDelegate> deviceDelegate;
@property(weak,nonatomic) id<GeoFencingEventDelegate> geoFencingDelegate;

- (void)shutDownIfError:(kaa_error_t) error;
- (void)eventLoop;

@end

@implementation KaaController

- (void)initController {
    self.deviceTypesRegistry = [NSMutableDictionary dictionary];
    kaa_error_t error_code = kaa_init(&_kaa_context_);
    if (error_code) {
        [NSException raise:@"Error during kaa context " format:@"creation %i", error_code];
    }
    error_code = kaa_tcp_channel_create(&_operations_channel
                                        , _kaa_context_->logger
                                        , OPERATIONS_SERVICES
                                        , OPERATIONS_SERVICES_COUNT);
    [self shutDownIfError:error_code];
    
    error_code = kaa_tcp_channel_create(&_bootstrap_channel
                                        , _kaa_context_->logger
                                        , BOOTSTRAP_SERVICE
                                        , BOOTSTRAP_SERVICE_COUNT);
    [self shutDownIfError:error_code];
    
    error_code = kaa_channel_manager_add_transport_channel(_kaa_context_->channel_manager
                                                           , &_bootstrap_channel
                                                           , NULL);
    [self shutDownIfError:error_code];
    
    error_code = kaa_channel_manager_add_transport_channel(_kaa_context_->channel_manager
                                                           , &_operations_channel
                                                           , NULL);
    [self shutDownIfError:error_code];
    
    error_code = kaa_event_manager_set_kaa_device_event_class_family_device_info_response_listener(_kaa_context_->event_manager, &on_device_info_response, (__bridge void *)(self));
    [self shutDownIfError:error_code];
    error_code = kaa_event_manager_set_kaa_geo_fencing_event_class_family_geo_fencing_status_response_listener(_kaa_context_->event_manager, &on_geo_fencing_status_response,  (__bridge void *)(self));
    [self shutDownIfError:error_code];
    
    [self eventLoop];
    
    NSLog(@"Kaa initialized!");
    self.isInitialized = YES;
}

- (BOOL)isAttached {
    return kaa_user_manager_is_attached_to_user(self.kaa_context_->user_manager);
}

- (void)destroyController {
    if (self.isInitialized) {
        self.isInitialized = false;
        self.isShutdown = true;
        kaa_tcp_channel_disconnect(&_operations_channel);
        kaa_deinit(_kaa_context_);
    }
}

- (void)shutDownIfError:(kaa_error_t)error {
    if (error) {
        [NSException raise:@"Error initializing kaa" format:@" Error code: %i", error];
    }
}

- (void)renameEndpoint:(kaa_endpoint_id_p)endpointId to:(NSString*)name {
    kaa_device_event_class_family_device_change_name_request_t *request = kaa_device_event_class_family_device_change_name_request_create();
    request->name = kaa_string_copy_create([name cStringUsingEncoding:NSUTF8StringEncoding]);
    kaa_error_t error_code = kaa_event_manager_send_kaa_device_event_class_family_device_change_name_request(self.kaa_context_->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error sending change name request, error code: %i", error_code);
    }
    request->destroy(request);
}

- (void)attachToUserWithUsername:(NSString *)username andPassword:(NSString *)password {
    NSLog(@"Attaching to user %@ with password %@", username, password);
    if (!_isInitialized) {
        [NSException raise:@"Couldn't attach to user" format:@"Kaa isn't initialized!"];
    }
    
    kaa_attachment_status_listeners_t listeners = { (__bridge void *)(self), &kaa_on_attached, &kaa_on_detached, &kaa_on_attach_success, &kaa_on_attach_failed};
    
    kaa_error_t error_code = kaa_user_manager_set_attachment_listeners(_kaa_context_->user_manager, &listeners);
    [self shutDownIfError:error_code];
    
    error_code = kaa_user_manager_default_attach_to_user(_kaa_context_->user_manager,
                                                 [username cStringUsingEncoding:NSASCIIStringEncoding],
                                                 [password cStringUsingEncoding:NSASCIIStringEncoding]);
    [self shutDownIfError:error_code];
    
}

- (void)findEventListeners:(const char *[])fullyQualifiedNames size:(size_t)count listeners:(kaa_event_listeners_callback_t*)callback {
    if ([self isAttached]) {
        kaa_error_t error_code = kaa_event_manager_find_event_listeners(_kaa_context_->event_manager, fullyQualifiedNames, count, callback);
        if (error_code) {
            KAA_LOG_ERROR(_kaa_context_->logger, error_code, "Failed to find event listeners");
            NSLog(@"Failed to find event listeners");
        }
    }
}

- (void)requestDeviceInfo:(kaa_endpoint_id_p)endpointId delegate:(id<DeviceEventDelegate>)delegate {
    self.deviceDelegate = delegate;
    kaa_device_event_class_family_device_info_request_t *request = kaa_device_event_class_family_device_info_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_device_event_class_family_device_info_request(_kaa_context_->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error sending device info request, error code: %i", error_code);
    }
    request->destroy(request);
}

- (void)requestGeoFencingInfo:(kaa_endpoint_id_p)endpointId delegate:(id<GeoFencingEventDelegate>)delegate {
    self.geoFencingDelegate = delegate;
    kaa_geo_fencing_event_class_family_geo_fencing_status_request_t *request = kaa_geo_fencing_event_class_family_geo_fencing_status_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_geo_fencing_event_class_family_geo_fencing_status_request(_kaa_context_->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error sending geo fencing request, code: %i", error_code);
    }
    request->destroy(request);
}

- (void)changeGeoFencingMode:(kaa_geo_fencing_event_class_family_operation_mode_t) mode forEndpoint:(kaa_endpoint_id_p)endpointId delegate:(id<GeoFencingEventDelegate>)delegate {
    self.geoFencingDelegate = delegate;
    kaa_geo_fencing_event_class_family_operation_mode_update_request_t *request = kaa_geo_fencing_event_class_family_operation_mode_update_request_create();
    request->mode = mode;
    kaa_error_t error_code = kaa_event_manager_send_kaa_geo_fencing_event_class_family_operation_mode_update_request(_kaa_context_->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error sending geo fencing change mode request, code: %i", error_code);
    }
    request->destroy(request);
}

- (void)subscribeForUpdates:(kaa_endpoint_id_p)endpointId {
    kaa_device_event_class_family_device_status_subscription_request_t *request =kaa_device_event_class_family_device_status_subscription_request_create();
    kaa_error_t error_code = kaa_event_manager_send_kaa_device_event_class_family_device_status_subscription_request(_kaa_context_->event_manager, request, endpointId);
    if (error_code) {
        NSLog(@"Error subscrubing for status updates, code: %i", error_code);
    }
    request->destroy(request);
}
void on_geo_fencing_status_response(void *context, kaa_geo_fencing_event_class_family_geo_fencing_status_response_t *event, kaa_endpoint_id_p source) {
    KaaController *controller = (__bridge KaaController*)context;
    if (controller.geoFencingDelegate && [controller.geoFencingDelegate respondsToSelector:@selector(onStatusReceived:forEndpoint:)]) {
        [controller.geoFencingDelegate onStatusReceived:event->mode forEndpoint:source];
    }
}

void on_device_info_response(void *context, kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p source) {
    KaaController *controller = (__bridge KaaController*)context;
    if (controller.deviceDelegate && [controller.deviceDelegate respondsToSelector:@selector(foundDevice:forEndpoint:)]) {
        NSString *name = [NSString stringWithCString:event->device_info->name->data encoding:NSUTF8StringEncoding];
        [controller.deviceDelegate foundDevice:name forEndpoint:source];
    }
    event->destroy(event);
}

#pragma mark - Callbacks

kaa_error_t kaa_on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    NSLog(@"Kaa Demo attached to user %s, access token %s\n", user_external_id, endpoint_access_token);
    return KAA_ERR_NONE;
}


kaa_error_t kaa_on_detached(void *context, const char *endpoint_access_token)
{
    NSLog(@"Kaa Demo detached from user access token %s\n", endpoint_access_token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_success(void *context)
{
    NSLog(@"Kaa Demo attach success");
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    [NSException raise:@"Kaa attach failed" format:@"Error code: %i, reason: %s", error_code, reason];
    return KAA_ERR_NONE;
}

- (void)eventLoop {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        kaa_error_t error_code = kaa_start(_kaa_context_);
        if (error_code) {
            NSLog(@"Failed to start Kaa workflow");
            [self shutDownIfError:error_code];
        }
        
        uint16_t select_timeout;
        error_code = kaa_tcp_channel_get_max_timeout(&_operations_channel, &select_timeout);
        if (error_code) {
            NSLog(@"Failed to get Operations channel keepalive timeout\n");
        }
        
        if (select_timeout > 3) {
            select_timeout = 3;
        }
        
        fd_set read_fds, write_fds, except_fds;
        int ops_fd = 0, bootstrap_fd = 0;
        struct timeval select_tv = { 0, 0 };
        int max_fd = 0;
        
        while (!_isShutdown) {
            FD_ZERO(&read_fds);
            FD_ZERO(&write_fds);
            FD_ZERO(&except_fds);
            
            max_fd = 0;
            
            kaa_tcp_channel_get_descriptor(&_operations_channel, &ops_fd);
            if (max_fd < ops_fd)
                max_fd = ops_fd;
            kaa_tcp_channel_get_descriptor(&_bootstrap_channel, &bootstrap_fd);
            if (max_fd < bootstrap_fd)
                max_fd = bootstrap_fd;
            
            if (kaa_tcp_channel_is_ready(&_operations_channel, FD_READ))
                FD_SET(ops_fd, &read_fds);
            if (kaa_tcp_channel_is_ready(&_operations_channel, FD_WRITE))
                FD_SET(ops_fd, &write_fds);
            
            if (kaa_tcp_channel_is_ready(&_bootstrap_channel, FD_READ))
                FD_SET(bootstrap_fd, &read_fds);
            if (kaa_tcp_channel_is_ready(&_bootstrap_channel, FD_WRITE))
                FD_SET(bootstrap_fd, &write_fds);
            
            select_tv.tv_sec = select_timeout;
            select_tv.tv_usec = 0;
            
            int poll_result = select(max_fd + 1, &read_fds, &write_fds, NULL, &select_tv);
            if (poll_result == 0) {
                kaa_tcp_channel_check_keepalive(&_operations_channel);
                kaa_tcp_channel_check_keepalive(&_bootstrap_channel);
            } else if (poll_result > 0) {
                if (bootstrap_fd >= 0) {
                    if (FD_ISSET(bootstrap_fd, &read_fds)) {
                        KAA_LOG_DEBUG(_kaa_context_->logger, KAA_ERR_NONE,"Processing IN event for the Bootstrap client socket %d", bootstrap_fd);
                        error_code = kaa_tcp_channel_process_event(&_bootstrap_channel, FD_READ);
                        if (error_code)
                            KAA_LOG_ERROR(_kaa_context_->logger, KAA_ERR_NONE,"Failed to process IN event for the Bootstrap client socket %d", bootstrap_fd);
                    }
                    if (FD_ISSET(bootstrap_fd, &write_fds)) {
                        KAA_LOG_DEBUG(_kaa_context_->logger, KAA_ERR_NONE,"Processing OUT event for the Bootstrap client socket %d", bootstrap_fd);
                        error_code = kaa_tcp_channel_process_event(&_bootstrap_channel, FD_WRITE);
                        if (error_code)
                            KAA_LOG_ERROR(_kaa_context_->logger, error_code,"Failed to process OUT event for the Bootstrap client socket %d", bootstrap_fd);
                    }
                }
                if (ops_fd >= 0) {
                    if (FD_ISSET(ops_fd, &read_fds)) {
                        KAA_LOG_DEBUG(_kaa_context_->logger, KAA_ERR_NONE,"Processing IN event for the Operations client socket %d", ops_fd);
                        error_code = kaa_tcp_channel_process_event(&_operations_channel, FD_READ);
                        if (error_code)
                            KAA_LOG_ERROR(_kaa_context_->logger, error_code,"Failed to process IN event for the Operations client socket %d", ops_fd);
                    }
                    if (FD_ISSET(ops_fd, &write_fds)) {
                        KAA_LOG_DEBUG(_kaa_context_->logger, KAA_ERR_NONE,"Processing OUT event for the Operations client socket %d", ops_fd);
                        error_code = kaa_tcp_channel_process_event(&_operations_channel, FD_WRITE);
                        if (error_code)
                            KAA_LOG_ERROR(_kaa_context_->logger, error_code,"Failed to process OUT event for the Operations client socket %d", ops_fd);
                    }
                }
            } else {
                KAA_LOG_ERROR(_kaa_context_->logger, KAA_ERR_BAD_STATE,"Failed to poll descriptors: %s", strerror(errno));
            }
        }
    });
    NSLog(@"Event loop finished!");
}

@end
