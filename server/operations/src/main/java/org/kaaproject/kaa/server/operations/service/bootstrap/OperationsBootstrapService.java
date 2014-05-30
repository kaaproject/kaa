/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.bootstrap;

import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;

/**
 * The interface for Operations bootstrap process.
 * Main methods are {@link #start() start} and {@link #stop() stop}
 *
 * @author ashvayka
 */
public interface OperationsBootstrapService {

    /**
     * Start.
     */
    void start();

    /**
     * Stop.
     */
    void stop();
    
    /**
     * OperationsService getter.
     * @return OperationsService
     */
    OperationsService getOperationsService();
    
    /**
     * KeyStoreService getter.
     * @return KeyStoreService
     */
    KeyStoreService getKeyStoreService();
    
    /**
     * CacheService getter.
     * @return CacheService
     */
    CacheService getCacheService();

}
