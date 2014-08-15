package org.kaaproject.kaa.server.operations.service.logs.flume.client;

import java.util.Collections;
import java.util.List;

import jline.internal.Log;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriorityFlumeClientManager extends FlumeClientManager {

    private static final Logger LOG = LoggerFactory.getLogger(PriorityFlumeClientManager.class);

    private static final int DEFAULT_POSITION = 0;
    private List<HostInfoDto> hosts = null;
    private Integer currentPosition = DEFAULT_POSITION;

    @Override
    public RpcClient initManager(FlumeAppenderParametersDto parameters) {
        hosts = parameters.getHosts();
        if (!hosts.isEmpty()) {
            Collections.sort(hosts);
        } else {
            Log.warn("Can't initialize flume Rpc client. No required hosts paraneters.");
        }
        return getNextClient(true);
    }

    private RpcClient getNextClient(boolean isInit) {
        return getNextClient(isInit, 0);
    }

    private RpcClient getNextClient(boolean isInit, int retryCount) {
        Log.debug("Get next flume rpc client");
        RpcClient client = null;
        HostInfoDto host = null;
        if (isInit) {
            host = hosts.get(DEFAULT_POSITION);
        } else {
            if (++currentPosition >= hosts.size()) {
                host = hosts.get(DEFAULT_POSITION);
                currentPosition = DEFAULT_POSITION;
            } else {
                host = hosts.get(currentPosition);
            }
        }
        try {
            Log.warn("Initialize new flume client.");
            client = RpcClientFactory.getDefaultInstance(host.getHostname(), host.getPort());
        } catch (FlumeException e) {
            Log.warn("Can't initialize flume client.", e);
            if (retryCount <= MAX_RETRY_COUNT) {
                client = getNextClient(false, ++retryCount);
            } else {
                LOG.warn("Wasn't initialized any clients. Got exception {}", e);
                throw e;
            }
        }
        return client;
    }

    @Override
    public void sendEventToFlume(Event event) throws EventDeliveryException {
        sendEventToFlume(event, 1);
    }

    private void sendEventToFlume(Event event, int retryCount) throws EventDeliveryException {
        try {
            LOG.debug("Sending flume event to flume agent {}", event);
            currentClient.append(event);
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume event. Got exception {}", e);
            currentClient.close();
            currentClient = getNextClient(false);
            if (retryCount <= MAX_RETRY_COUNT) {
                LOG.debug("Retry send flume event. Count {}", retryCount);
                sendEventToFlume(event, ++retryCount);
            } else {
                LOG.warn("Flume event wasn't sent. Got exception {}", e);
                throw e;
            }
        }
    }

}
