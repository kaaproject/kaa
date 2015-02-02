package org.kaaproject.kaa.server.verifiers.trustful.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.utils.CRC32Util;
import org.kaaproject.kaa.server.common.verifier.KaaUserVerifierConfig;
import org.kaaproject.kaa.server.common.verifier.UserVerifierConfig;
import org.kaaproject.kaa.server.verifiers.trustful.config.gen.TrustfulAvroConfig;

@KaaUserVerifierConfig
public class TrustfulVerifierConfig implements UserVerifierConfig{

    private static final String ORG_KAAPROJECT_KAA_SERVER_VERIFIERS_TRUSTFUL = "org.kaaproject.kaa.server.verifiers.trustful";

    @Override
    public int getId() {
        return CRC32Util.crc32(getName());
    }

    @Override
    public String getName() {
        return ORG_KAAPROJECT_KAA_SERVER_VERIFIERS_TRUSTFUL;
    }

    @Override
    public String getUserVerifierClass() {
        return "org.kaaproject.kaa.server.verifiers.trustful.TrustfulUserVerifier";
    }

    @Override
    public Schema getConfigSchema() {
        return TrustfulAvroConfig.SCHEMA$;
    }

}
