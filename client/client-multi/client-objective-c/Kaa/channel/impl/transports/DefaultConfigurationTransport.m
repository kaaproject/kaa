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

#import "DefaultConfigurationTransport.h"
#import "ConfigurationCommon.h"
#import "SchemaProcessor.h"
#import "KaaLogging.h"

#define TAG @"DefaultConfigurationTransport >>>"

@interface DefaultConfigurationTransport ()

@property (nonatomic) BOOL resyncOnly;
@property (nonatomic, strong) id<ConfigurationHashContainer> hashContainer;
@property (nonatomic, strong) id<ConfigurationProcessor> configProcessor;
@property (nonatomic, strong) id<SchemaProcessor> schemaProc;

@end

@implementation DefaultConfigurationTransport

- (void)setConfigurationHashContainer:(id<ConfigurationHashContainer>)container {
    self.hashContainer = container;
}

- (void)setConfigurationProcessor:(id<ConfigurationProcessor>)processor {
    self.configProcessor = processor;
}

- (void)setSchemaProcessor:(id<SchemaProcessor>)schemaProcessor {
    self.schemaProc = schemaProcessor;
}

- (ConfigurationSyncRequest *)createConfigurationRequest {
    if (self.clientState && self.hashContainer) {
        EndpointObjectHash *hash = [self.hashContainer getConfigurationHash];
        ConfigurationSyncRequest *request = [[ConfigurationSyncRequest alloc] init];
        if (hash.data) {
            request.configurationHash = hash.data;
        }
        request.resyncOnly = [KAAUnion unionWithBranch:KAA_UNION_BOOLEAN_OR_NULL_BRANCH_0 data:@(self.resyncOnly)];
        return request;
    } else {
        DDLogError(@"%@ Can't create config request due to invalid params: %@, %@", TAG, self.clientState, self.hashContainer);
    }
    return nil;
}

- (void)onConfigurationResponse:(ConfigurationSyncResponse *)response {
    if (!self.clientState || !self.configProcessor) {
        return;
    }
    
    if (response.confSchemaBody && response.confSchemaBody.branch == KAA_UNION_BYTES_OR_NULL_BRANCH_0) {
        [self.schemaProc loadSchema:response.confSchemaBody.data];
    }
    if (response.confDeltaBody && response.confDeltaBody.branch == KAA_UNION_BYTES_OR_NULL_BRANCH_0) {
        BOOL fullResync = response.responseStatus == SYNC_RESPONSE_STATUS_RESYNC;
        [self.configProcessor processConfigurationData:response.confDeltaBody.data fullResync:fullResync];
    }
    [self syncAck:response.responseStatus];
    DDLogInfo(@"%@ Processed configuration response", TAG);
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_CONFIGURATION;
}

- (void)setResyncOnly:(BOOL)resyncOnly {
    _resyncOnly = resyncOnly;
}

@end
