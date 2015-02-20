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
package org.kaaproject.kaa.client;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.util.Base64;

/**
 * Represents platform specific context for Kaa client initialization
 * 
 * @author Andrew Shvayka
 *
 */
public interface KaaClientPlatformContext {

    KaaClientProperties getProperties();

    AbstractHttpClient createHttpClient(String url, PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey);

    PersistentStorage createPersistentStorage();

    Base64 getBase64();

    ConnectivityChecker createConnectivityChecker();

}
