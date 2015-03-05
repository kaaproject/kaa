package org.kaaproject.kaa.server.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;

public interface NeighborTemplate<V> {

    void process(OperationsThriftService.Iface client, List<V> messages) throws TException;

    void onServerError(String serverId, Exception e);

}
