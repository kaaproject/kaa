package org.kaaproject.kaa.server.verifiers.gplus.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;


@KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
public class GplusVerifierConfig implements PluginConfig {

    private static final String GPLUS_VERIFIER_NAME = "Google+ verifier";

    @Override
    public String getPluginTypeName() {
        return GPLUS_VERIFIER_NAME;
    }

    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.server.verifiers.gplus.GplusUserVerifier";
    }

    @Override
    public Schema getPluginConfigSchema() {
        return GplusAvroConfig.SCHEMA$;
    }

}
