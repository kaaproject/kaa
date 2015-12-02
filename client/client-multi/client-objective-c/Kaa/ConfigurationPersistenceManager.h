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

#ifndef Kaa_ConfigurationPersistenceManager_h
#define Kaa_ConfigurationPersistenceManager_h

#import <Foundation/Foundation.h>
#import "ConfigurationStorage.h"

/**
 * Manager for saving and loading of configuration data
 *
 * Provide ConfigurationStorage implementation instance to store merged
 * configuration when configuration deltas are received from Operation server.
 * Once ConfigurationPersistenceManager#setConfigurationStorage(ConfigurationStorage)
 * is called ConfigurationStorage#loadConfiguration() will be invoked to
 * load persisted configuration.
 *
 * @see ConfigurationStorage
 */
@protocol ConfigurationPersistenceManager

/**
 * Provide storage object which is able to persist encoded configuration data.
 */
- (void)setConfigurationStorage:(id<ConfigurationStorage>)storage;

@end

#endif
