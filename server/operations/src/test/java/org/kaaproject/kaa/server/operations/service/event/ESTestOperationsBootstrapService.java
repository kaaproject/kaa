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

package org.kaaproject.kaa.server.operations.service.event;

import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestOperationsBootstrapService implements OperationsBootstrapService {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getOperationsService()
     */
    @Override
    public OperationsService getOperationsService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getKeyStoreService()
     */
    @Override
    public KeyStoreService getKeyStoreService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getCacheService()
     */
    @Override
    public CacheService getCacheService() {
        // TODO Auto-generated method stub
        return null;
    }

}
