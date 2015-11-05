package org.kaaproject.kaa.server.common.core.plugin.instance;

import java.util.UUID;

public interface KaaPluginMessage extends KaaMessageWrapper{

    UUID getUid();

}
