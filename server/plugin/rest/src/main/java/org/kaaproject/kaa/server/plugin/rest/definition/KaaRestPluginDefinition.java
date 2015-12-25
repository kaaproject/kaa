/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.plugin.rest.definition;

import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class KaaRestPluginDefinition implements PluginDef {

    private static final long serialVersionUID = 3242496999565136016L;

    private static final String REST_PLUGIN_TYPE = "REST";
    public static final String DEFAULT_NAME = "Rest";
    public static final Integer VERSION = 1;

    public KaaRestPluginDefinition() {
    }

    @Override
    public String getName() {
        return DEFAULT_NAME;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public String getType() {
        return REST_PLUGIN_TYPE;
    }

    @Override
    public PluginScope getScope() {
        return PluginScope.LOCAL_APPLICATION;
    }

    @Override
    public String getConfigurationSchema() {
        return readFileAsString("rest_plugin.avsc");
    }

    @Override
    public Set<PluginContractDef> getPluginContracts() {
        Set<PluginContractDef> contracts = new HashSet<>();
        contracts.add(MessagingPluginContract.buildMessagingContract(PluginContractDirection.OUT,
                readFileAsString("rest_plugin_read_item.avsc"), readFileAsString("rest_plugin_write_item.avsc")));

        return contracts;
    }

    private String readFileAsString(String fileName) {
        String fileBody = null;
        URL url = getClass().getResource(fileName);
        if (url != null) {
            try {
                Path path = Paths.get(url.toURI());
                byte[] bytes = Files.readAllBytes(path);
                if (bytes != null) {
                    fileBody = new String(bytes);
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return fileBody;
    }
}
