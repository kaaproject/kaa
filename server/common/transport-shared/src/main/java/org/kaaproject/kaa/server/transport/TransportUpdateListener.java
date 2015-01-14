package org.kaaproject.kaa.server.transport;

import java.util.List;
import java.util.Map;

public interface TransportUpdateListener {

    void onTransportsStarted(List<org.kaaproject.kaa.server.common.zk.gen.TransportMetaData> mdList);

}
