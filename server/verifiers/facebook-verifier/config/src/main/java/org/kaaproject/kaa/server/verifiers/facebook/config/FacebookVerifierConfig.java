package org.kaaproject.kaa.server.verifiers.facebook.config;


import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.utils.CRC32Util;
import org.kaaproject.kaa.server.common.verifier.KaaUserVerifierConfig;
import org.kaaproject.kaa.server.common.verifier.UserVerifierConfig;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;

@KaaUserVerifierConfig
public class FacebookVerifierConfig implements UserVerifierConfig {

    private static final String ORG_KAAPROJECT_KAA_SERVER_VERIFIERS_TRUSTFUL = "org.kaaproject.kaa.server.verifiers.facebook";

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
        return FacebookAvroConfig.SCHEMA$;
    }
}
