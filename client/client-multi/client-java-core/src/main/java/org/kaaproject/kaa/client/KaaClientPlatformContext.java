package org.kaaproject.kaa.client;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.util.Base64;

public interface KaaClientPlatformContext {

    KaaClientProperties getProperties();

    AbstractHttpClient createHttpClient(String url, PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey);

    PersistentStorage createPersistentStorage();

    Base64 getBase64();

    ConnectivityChecker createConnectivityChecker();

}
