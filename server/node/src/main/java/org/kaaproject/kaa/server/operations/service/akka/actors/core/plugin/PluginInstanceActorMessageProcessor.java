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
package org.kaaproject.kaa.server.operations.service.akka.actors.core.plugin;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginInitializationException;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginInstantiationException;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginInstanceActorMessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PluginInstanceActorMessageProcessor.class);

    private final PluginService pluginService;
    private final PluginInstanceDto pluginInstanceDto;
    private final KaaPlugin plugin;

    public PluginInstanceActorMessageProcessor(AkkaContext context, String pluginInstanceId) throws PluginLifecycleException,
            PluginInitializationException {
        this.pluginService = context.getPluginService();
        this.pluginInstanceDto = pluginService.getInstanceById(pluginInstanceId);
        this.plugin = newPluginInstance(pluginInstanceDto);
        initPluginInstance(plugin, pluginInstanceDto);
    }

    private static void initPluginInstance(KaaPlugin plugin, PluginInstanceDto pluginInstanceDto) throws PluginLifecycleException {
        LOG.info("[{}] Initializing new plugin instance using definition {}", pluginInstanceDto.getId(), pluginInstanceDto);
        plugin.init(new BasePluginInitContext(pluginInstanceDto));
        LOG.info("[{}] Initialized new plugin instance", pluginInstanceDto.getId());
    }

    private KaaPlugin newPluginInstance(PluginInstanceDto dto) throws PluginInstantiationException {
        PluginDto pluginDef = dto.getPluginDefinition();
        LOG.info("[{}] Creating new plugin instance using definition {}", dto.getId(), pluginDef);
        try {
            @SuppressWarnings("unchecked")
            Class<KaaPlugin> appenderClass = (Class<KaaPlugin>) Class.forName(pluginDef.getClassName());
            KaaPlugin plugin = appenderClass.newInstance();
            LOG.info("[{}] Created new plugin instance", dto.getId());
            return plugin;
        } catch (ClassNotFoundException e) {
            LOG.error("Can't find plugin class: {}", pluginDef.getClassName(), e);
            throw new PluginInstantiationException(pluginDef.getClassName());
        } catch (ReflectiveOperationException e) {
            LOG.error("Can't craete instance of plugin class: {}", dto.getName(), e);
            throw new PluginInstantiationException(dto.getName());
        }
    }

    public void stop() throws PluginLifecycleException {
        LOG.info("[{}] Stopping new plugin instance", pluginInstanceDto.getId());
        plugin.stop();
        LOG.info("[{}] Stopped new plugin instance", pluginInstanceDto.getId());
    }

}
