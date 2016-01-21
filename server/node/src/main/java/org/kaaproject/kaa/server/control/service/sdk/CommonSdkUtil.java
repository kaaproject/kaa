package org.kaaproject.kaa.server.control.service.sdk;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;

import java.util.List;

public class CommonSdkUtil {

    private static final String SEPARATOR = ":";

    private CommonSdkUtil(){
    }

    public static TarEntryData tarEntryForSources(String source, String name) {
        TarArchiveEntry tarEntry = new TarArchiveEntry(name);
        tarEntry.setSize(source.getBytes().length);
        return new TarEntryData(tarEntry, source.getBytes());
    }

    public static String bootstrapNodesToString(List<BootstrapNodeInfo> bootstrapNodes) {
        String bootstrapServers = "";
        if (bootstrapNodes != null && !bootstrapNodes.isEmpty()) {
            for (BootstrapNodeInfo node : bootstrapNodes) {
                List<TransportMetaData> supportedChannels = node.getTransports();

                int accessPointId = ServerNameUtil.crc32(node.getConnectionInfo());

                for (TransportMetaData transport : supportedChannels) {
                    for (VersionConnectionInfoPair pair : transport.getConnectionInfo()) {
                        bootstrapServers += accessPointId;
                        bootstrapServers += SEPARATOR;
                        bootstrapServers += transport.getId();
                        bootstrapServers += SEPARATOR;
                        bootstrapServers += pair.getVersion();
                        bootstrapServers += SEPARATOR;
                        bootstrapServers += Base64.encodeBase64String(pair.getConenctionInfo().array());
                        bootstrapServers += ";";
                    }
                }
            }
        }
        return bootstrapServers;
    }
}
