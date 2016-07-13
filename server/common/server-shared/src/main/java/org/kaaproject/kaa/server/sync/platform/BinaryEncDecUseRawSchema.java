package org.kaaproject.kaa.server.sync.platform;


import org.kaaproject.kaa.common.Constants;

import org.kaaproject.kaa.server.sync.ClientSync;

@KaaPlatformProtocol
public class BinaryEncDecUseRawSchema extends BinaryEncDec{

    @Override
    public int getId() {
        return Constants.KAA_PLATFORM_PROTOCOL_BINARY_ID_V2;
    }

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        ClientSync sync = super.decode(data);
        sync.setUseConfigurationRawSchema(true);
        return sync;
    }
}
