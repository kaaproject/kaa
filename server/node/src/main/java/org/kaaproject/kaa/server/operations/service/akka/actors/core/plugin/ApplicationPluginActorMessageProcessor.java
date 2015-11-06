package org.kaaproject.kaa.server.operations.service.akka.actors.core.plugin;

import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;

public class ApplicationPluginActorMessageProcessor {

    private final PluginService pluginService;
    private final PluginInstanceDto pluginInstanceDto;

    public ApplicationPluginActorMessageProcessor(AkkaContext context, String pluginInstanceId) {
        this.pluginService = context.getPluginService();
        this.pluginInstanceDto = pluginService.getInstanceById(pluginInstanceId);
    }

    public void stop() {
        // TODO Auto-generated method stub

    }

}
