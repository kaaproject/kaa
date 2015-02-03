package org.kaaproject.kaa.server.verifiers.facebook.config;


import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;

@KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
public class FacebookVerifierConfig implements PluginConfig {

    private static final String TRUSTFUL_VERIFIER_NAME = "Facebook verifier";

    @Override
    public String getPluginTypeName() {
        return TRUSTFUL_VERIFIER_NAME;
    }

    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.server.verifiers.facebook.FacebookUserVerifier";
    }

    @Override
    public Schema getPluginConfigSchema() {
        return FacebookAvroConfig.SCHEMA$;
    }
}
