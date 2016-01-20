package org.kaaproject.kaa.server.node.service.thrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;

public class OperationsServiceMsg {
    private final ThriftUnicastNotificationMessage unicastNotificationMsg;
    private final ThriftServerProfileUpdateMessage serverProfileUpdateMsg;
    private final ThriftEntityRouteMessage entityRouteMsg;
    private final UserConfigurationUpdate userConfigurationUpdateMsg;

    private OperationsServiceMsg(ThriftUnicastNotificationMessage unicastNotificationMsg,
            ThriftServerProfileUpdateMessage serverProfileUpdateMsg, ThriftEntityRouteMessage entityRouteMsg,
            UserConfigurationUpdate userConfigurationUpdateMsg) {
        super();
        this.unicastNotificationMsg = unicastNotificationMsg;
        this.serverProfileUpdateMsg = serverProfileUpdateMsg;
        this.entityRouteMsg = entityRouteMsg;
        this.userConfigurationUpdateMsg = userConfigurationUpdateMsg;
    }

    public static OperationsServiceMsg fromServerProfileUpdateMessage(ThriftServerProfileUpdateMessage serverProfileUpdateMsg) {
        return new OperationsServiceMsg(null, serverProfileUpdateMsg, null, null);
    }

    public static OperationsServiceMsg fromNotification(ThriftUnicastNotificationMessage unicastNotificationMsg) {
        return new OperationsServiceMsg(unicastNotificationMsg, null, null, null);
    }

    public static OperationsServiceMsg fromRoute(ThriftEntityRouteMessage entityRouteMsg) {
        return new OperationsServiceMsg(null, null, entityRouteMsg, null);
    }

    public static OperationsServiceMsg fromUpdate(UserConfigurationUpdate userConfigurationUpdateMsg) {
        return new OperationsServiceMsg(null, null, null, userConfigurationUpdateMsg);
    }

    public ThriftUnicastNotificationMessage getUnicastNotificationMsg() {
        return unicastNotificationMsg;
    }

    public ThriftServerProfileUpdateMessage getServerProfileUpdateMsg() {
        return serverProfileUpdateMsg;
    }

    public UserConfigurationUpdate getUserConfigurationUpdateMsg() {
        return userConfigurationUpdateMsg;
    }

    public ThriftEntityRouteMessage getEntityRouteMsg() {
        return entityRouteMsg;
    }

    public static void dispatch(Iface client, List<OperationsServiceMsg> messages) throws TException {
        List<UserConfigurationUpdate> updates = new ArrayList<UserConfigurationUpdate>();
        List<ThriftEntityRouteMessage> routes = new ArrayList<ThriftEntityRouteMessage>();
        for (OperationsServiceMsg msg : messages) {
            if (msg.getUnicastNotificationMsg() != null) {
                client.onUnicastNotification(msg.getUnicastNotificationMsg());
            }
            if (msg.getServerProfileUpdateMsg() != null) {
                client.onServerProfileUpdate(msg.getServerProfileUpdateMsg());
            }
            if (msg.getUserConfigurationUpdateMsg() != null) {
                updates.add(msg.getUserConfigurationUpdateMsg());
            }
            if (msg.getEntityRouteMsg() != null) {
                routes.add(msg.getEntityRouteMsg());
            }
        }
        if (updates.size() > 0) {
            client.sendUserConfigurationUpdates(updates);
        }
        if (routes.size() > 0) {
            client.onEntityRouteMessages(routes);
        }
    }

}