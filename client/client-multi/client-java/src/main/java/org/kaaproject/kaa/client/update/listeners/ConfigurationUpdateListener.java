/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.client.update.listeners;

import java.io.IOException;

import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.client.update.UpdateListener;
import org.kaaproject.kaa.common.endpoint.gen.ConfSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

/**
 * Configuration listener ({@link UpdateListener}.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class ConfigurationUpdateListener implements UpdateListener {
    private final ConfigurationProcessor configurationProcessor;
    private final SchemaProcessor schemaProcessor;
    private final ConfigurationHashContainer hashContainer;
    private final KaaClientState clientState;

    public ConfigurationUpdateListener(
            ConfigurationProcessor configurationProcessor,
            SchemaProcessor schemaProcessor,
            ConfigurationHashContainer hashContainer, KaaClientState clientState) {
        this.configurationProcessor = configurationProcessor;
        this.schemaProcessor = schemaProcessor;
        this.hashContainer = hashContainer;
        this.clientState = clientState;
    }

    @Override
    public void onDeltaUpdate(SyncResponse response) throws IOException {
        if (response.getConfSyncResponse() != null) {
            ConfSyncResponse confResponse = response.getConfSyncResponse();
            if (confResponse.getConfSchemaBody() != null) {
                schemaProcessor.loadSchema(response.getConfSyncResponse().getConfSchemaBody());
            }
            switch (response.getResponseType()) {
                case CONF_RESYNC:
                    processDelta(response, confResponse, true);
                    break;
                case DELTA:
                    processDelta(response, confResponse, false);
                    break;
                default:
                    break;
            }
        }
    }

    protected void processDelta(SyncResponse response, ConfSyncResponse confResponse, boolean fullResync) throws IOException {
        if (confResponse.getConfDeltaBody() != null) {
            configurationProcessor.processConfigurationData(response.getConfSyncResponse().getConfDeltaBody(), fullResync);
            clientState.setConfigurationHash(hashContainer.getConfigurationHash());
        }
    }
}
