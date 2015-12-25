package org.kaaproject.kaa.server.common.core.plugin.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains everything that is needed to generate a plugin API.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class PluginSetup {

    private static final Logger LOG = LoggerFactory.getLogger(PluginSetup.class);

    private AbstractSdkApiGenerator<?> pluginGenerator;
    private PluginSdkApiGenerationContext generationContext;

    public PluginSetup(AbstractSdkApiGenerator<?> pluginGenerator, PluginSdkApiGenerationContext generationContext) {
        this.pluginGenerator = pluginGenerator;
        this.generationContext = generationContext;
    }

    public PluginSDKApiBundle generatePluginAPI() {
        try {
            return this.pluginGenerator.generatePluginSdkApi(this.generationContext);
        } catch (SdkApiGenerationException cause) {
            LOG.error("Unable to generate plugin API", cause);
            throw new RuntimeException(cause);
        }
    }

    public AbstractSdkApiGenerator<?> getPluginGenerator() {
        return this.pluginGenerator;
    }

    public PluginSdkApiGenerationContext getGenerationContext() {
        return this.generationContext;
    }
}
