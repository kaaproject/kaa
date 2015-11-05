package org.kaaproject.kaa.server.plugin.rest;

import org.kaaproject.kaa.server.common.core.plugin.def.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.plugin.rest.definition.KaaRestPluginDefinition;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfigSchema;

import java.io.IOException;

import static org.kaaproject.kaa.server.plugin.rest.gen.HttpSchemaType.HTTP;

@Plugin(KaaRestPluginDefinition.class)
public class KaaRestPlugin implements KaaPlugin {

    @Override
    public void init(PluginInitContext context) {

    }

    @Override
    public void onPluginMessage(KaaPluginMessage msg, PluginExecutionContext ctx) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) throws IOException {
        PluginHttpServer httpServer = new PluginHttpServer();
        KaaRestPluginConfigSchema conf = new KaaRestPluginConfigSchema();
        conf.setHost("localhost");
        conf.setPort(9999);
        conf.setHttpSchema(HTTP);
        httpServer.initServer(conf);
        httpServer.start();
        System.in.read();
        httpServer.stop();
    }
}
