/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.server.plugin.rest;

import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.plugin.rest.definition.KaaRestPluginDefinition;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfigSchema;

import java.io.IOException;

import static org.kaaproject.kaa.server.plugin.rest.gen.HttpSchemaType.HTTP;

@Plugin(KaaRestPluginDefinition.class)
public class KaaRestPlugin implements KaaPlugin {

    @Override
    public void init(PluginInitContext context) throws PluginLifecycleException {

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

    @Override
    public void stop() throws PluginLifecycleException {
        // TODO Auto-generated method stub

    }
}
