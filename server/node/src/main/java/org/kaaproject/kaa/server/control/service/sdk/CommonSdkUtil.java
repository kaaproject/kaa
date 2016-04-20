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
