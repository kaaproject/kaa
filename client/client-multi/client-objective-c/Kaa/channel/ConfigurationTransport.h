/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#ifndef Kaa_ConfigurationTransport_h
#define Kaa_ConfigurationTransport_h

#import "KaaTransport.h"
#import "EndpointGen.h"
#import "ConfigurationCommon.h"
#import "SchemaProcessor.h"

/**
 * KaaTransport for the Configuration service.
 * Updates the Configuration manager state.
 */
@protocol ConfigurationTransport <KaaTransport>

/**
 * Creates the configuration request.
 *
 * @return The configuration request object.
 * @see ConfigurationSyncRequest
 */
- (ConfigurationSyncRequest *)createConfigurationRequest;

/**
 * Updates the state of the Configuration manager according to the given response.
 *
 * @param response The configuration response.
 * @see ConfigurationSyncResponse
 */
- (void)onConfigurationResponse:(ConfigurationSyncResponse *)response;

- (void)setConfigurationHashContainer:(id<ConfigurationHashContainer>)container;

- (void)setConfigurationProcessor:(id<ConfigurationProcessor>)processor;

- (void)setSchemaProcessor:(id<SchemaProcessor>)processor;

- (void)setResyncOnly:(BOOL)resyncOnly;

@end

#endif
