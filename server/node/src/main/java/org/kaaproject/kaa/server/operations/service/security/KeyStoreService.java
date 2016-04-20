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

package org.kaaproject.kaa.server.operations.service.security;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * The interface KeyStoreService is used to model key store.
 * There is only two useful methods that allow to get Public and Private Key.
 * 
 * @author ashvayka
 */
public interface KeyStoreService {

    /**
     * Gets the private key.
     *
     * @return the private key
     */
    PrivateKey getPrivateKey();

    /**
     * Gets the public key.
     *
     * @return the public key
     */
    PublicKey getPublicKey();

}
