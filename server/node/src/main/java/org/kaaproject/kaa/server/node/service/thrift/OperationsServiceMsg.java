/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.node.service.thrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;

public class OperationsServiceMsg {
    private final ThriftUnicastNotificationMessage unicastNotificationMsg;
    private final ThriftServerProfileUpdateMessage serverProfileUpdateMsg;
    private final ThriftEntityRouteMessage entityRouteMsg;
    private final ThriftEndpointDeregistrationMessage endpointDeregistrationMsg;
    private final UserConfigurationUpdate userConfigurationUpdateMsg;

    private OperationsServiceMsg(ThriftUnicastNotificationMessage unicastNotificationMsg,
            ThriftServerProfileUpdateMessage serverProfileUpdateMsg, ThriftEntityRouteMessage entityRouteMsg,
            UserConfigurationUpdate userConfigurationUpdateMsg, ThriftEndpointDeregistrationMessage endpointDeregistrationMsg) {
        super();
        this.unicastNotificationMsg = unicastNotificationMsg;
        this.serverProfileUpdateMsg = serverProfileUpdateMsg;
        this.entityRouteMsg = entityRouteMsg;
        this.endpointDeregistrationMsg = endpointDeregistrationMsg;
        this.userConfigurationUpdateMsg = userConfigurationUpdateMsg;
    }

    public static OperationsServiceMsg fromServerProfileUpdateMessage(ThriftServerProfileUpdateMessage serverProfileUpdateMsg) {
        return new OperationsServiceMsg(null, serverProfileUpdateMsg, null, null, null);
    }

    public static OperationsServiceMsg fromNotification(ThriftUnicastNotificationMessage unicastNotificationMsg) {
        return new OperationsServiceMsg(unicastNotificationMsg, null, null, null, null);
    }

    public static OperationsServiceMsg fromRoute(ThriftEntityRouteMessage entityRouteMsg) {
        return new OperationsServiceMsg(null, null, entityRouteMsg, null, null);
    }

    public static OperationsServiceMsg fromUpdate(UserConfigurationUpdate userConfigurationUpdateMsg) {
        return new OperationsServiceMsg(null, null, null, userConfigurationUpdateMsg, null);
    }

    public static OperationsServiceMsg fromDeregistration(ThriftEndpointDeregistrationMessage endpointDeregistrationMsg) {
        return new OperationsServiceMsg(null, null, null, null, endpointDeregistrationMsg);
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
    
    public ThriftEndpointDeregistrationMessage getEndpointDeregistrationMsg() {
        return endpointDeregistrationMsg;
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
            if (msg.getEndpointDeregistrationMsg() != null) {
                client.onEndpointDeregistration(msg.getEndpointDeregistrationMsg());
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