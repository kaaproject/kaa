package org.kaaproject.kaa.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Kaa {

    public static KaaClient newClient(KaaClientPlatformContext context) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException{
        return new BaseKaaClient(context);
    };

}
