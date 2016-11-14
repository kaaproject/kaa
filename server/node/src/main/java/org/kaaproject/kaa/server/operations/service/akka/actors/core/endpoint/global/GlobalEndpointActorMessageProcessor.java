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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import akka.actor.ActorContext;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftActorClassifier;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointConfigurationRefreshMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ActorClassifier;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteTable;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class GlobalEndpointActorMessageProcessor
    extends AbstractEndpointActorMessageProcessor<GlobalEndpointActorState> {


  private static final Logger LOG = LoggerFactory
      .getLogger(GlobalEndpointActorMessageProcessor.class);

  private final String nodeId;
  private final RouteTable<EndpointClusterAddress> routes;
  private final ClusterService clusterService;
  private final OperationsService operationsService;

  /**
   * All-args constructor.
   */
  public GlobalEndpointActorMessageProcessor(AkkaContext context, String appToken,
                                             EndpointObjectHash key, String actorKey) {
    super(new GlobalEndpointActorState(Base64Util.encode(key.getData()), actorKey),
        context.getOperationsService(),
        appToken, key, actorKey, Base64Util.encode(key.getData()),
        context.getGlobalEndpointTimeout()
    );
    clusterService = context.getClusterService();
    operationsService = context.getOperationsService();
    nodeId = context.getClusterService().getNodeId();
    routes = new RouteTable<>(nodeId);
  }

  /**
   * Process an endpoint route message.
   *
   * @param message endpoint route message
   */
  public void processRouteMessage(EndpointRouteMessage message) {
    LOG.debug("[{}] Processing {} operation for address {}",
        endpointKey, message.getOperation(), message.getAddress());
    switch (message.getOperation()) {
      case ADD:
      case UPDATE:
        routes.add(message.getAddress());
        break;
      case DELETE:
        routes.remove(message.getAddress());
        break;
      default:
        break;
    }
  }

  /**
   * Process a cluster update.
   *
   * @param context actor context
   */
  public void processClusterUpdate(ActorContext context) {
    if (!clusterService.isMainEntityNode(key)) {
      LOG.debug("[{}] No longer a global endpoint node for {}", endpointKey);
      routes.clear();
      context.stop(context.self());
    }
  }

  @Override
  protected void processThriftMsg(ActorContext context, ThriftEndpointActorMsg<?> msg) {
    Object thriftMsg = msg.getMsg();
    if (thriftMsg instanceof ThriftServerProfileUpdateMessage) {
      processServerProfileUpdateMsg(context, (ThriftServerProfileUpdateMessage) thriftMsg);
    } else if (thriftMsg instanceof ThriftUnicastNotificationMessage) {
      processUnicastNotificationMsg(context, (ThriftUnicastNotificationMessage) thriftMsg);
    } else if (thriftMsg instanceof ThriftEndpointConfigurationRefreshMessage) {
      processEndpointConfigurationRefreshMsg(context, (ThriftEndpointConfigurationRefreshMessage) thriftMsg);
    }
  }

  private void processServerProfileUpdateMsg(ActorContext context,
                                             ThriftServerProfileUpdateMessage thriftMsg) {
    operationsService.syncServerProfile(appToken, endpointKey, key);
    ThriftServerProfileUpdateMessage localMsg = new ThriftServerProfileUpdateMessage(thriftMsg);
    localMsg.setActorClassifier(ThriftActorClassifier.LOCAL);
    dispatchMsg(context, localMsg, clusterService::sendServerProfileUpdateMessage);
  }

  private void processUnicastNotificationMsg(ActorContext context,
                                             ThriftUnicastNotificationMessage thriftMsg) {
    ThriftUnicastNotificationMessage localMsg = new ThriftUnicastNotificationMessage(thriftMsg);
    localMsg.setActorClassifier(ThriftActorClassifier.LOCAL);
    dispatchMsg(context, localMsg, clusterService::sendUnicastNotificationMessage);
  }

  private void processEndpointConfigurationRefreshMsg(ActorContext context, ThriftEndpointConfigurationRefreshMessage thriftMsg) {
    ThriftEndpointConfigurationRefreshMessage localMsg = new ThriftEndpointConfigurationRefreshMessage(thriftMsg);
    localMsg.setActorClassifier(ThriftActorClassifier.LOCAL);
    dispatchMsg(context, localMsg, clusterService::sendEndpointConfigurationRefreshMessage);
  }

  private <T> void dispatchMsg(ActorContext context, T localMsg, BiConsumer<String, T> biConsumer) {
    for (EndpointClusterAddress address : routes.getLocalRoutes()) {
      LOG.info("Forwarding {} to local endpoint actor {}", localMsg, address);
      ThriftEndpointActorMsg<T> msg = new ThriftEndpointActorMsg<>(
          address.toEndpointAddress(), ActorClassifier.LOCAL, localMsg);
      context.parent().tell(msg, context.self());
    }
    for (EndpointClusterAddress address : routes.getRemoteRoutes()) {
      LOG.info("Forwarding {} to remote endpoint actor {}", localMsg, address);
      biConsumer.accept(address.getNodeId(), localMsg);
    }
  }
}
