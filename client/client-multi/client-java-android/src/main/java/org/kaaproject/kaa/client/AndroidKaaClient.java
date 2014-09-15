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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.connectivity.AndroidConnectivityChecker;
import org.kaaproject.kaa.client.persistence.AndroidInternalPersistentStorage;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.transport.AndroidHttpClient;

public class AndroidKaaClient extends AbstractKaaClient {

    AndroidKaaClient() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        super();
    }

    @Override
    public AbstractHttpClient createHttpClient(String url,
            PrivateKey privateKey, PublicKey publicKey,
            PublicKey remotePublicKey) {
        return new AndroidHttpClient(url, privateKey, publicKey, remotePublicKey);
    }

    @Override
    protected PersistentStorage createPersistentStorage() {
        return new AndroidInternalPersistentStorage(KaaAndroid.getContext());
    }

    @Override
    protected ConnectivityChecker createConnectivityChecker() {
        return new AndroidConnectivityChecker(KaaAndroid.getContext());
    }
}
