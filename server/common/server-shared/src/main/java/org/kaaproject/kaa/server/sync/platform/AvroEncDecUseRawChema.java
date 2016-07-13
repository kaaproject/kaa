package org.kaaproject.kaa.server.sync.platform;


import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.platform.AvroEncDec;

@KaaPlatformProtocol
public class AvroEncDecUseRawChema extends AvroEncDec {

    @Override
    public int getId() {
        return Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID_V2;
    }

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        ClientSync sync = super.decode(data);
        sync.setUseConfigurationRawSchema(true);
        return sync;
    }
}
