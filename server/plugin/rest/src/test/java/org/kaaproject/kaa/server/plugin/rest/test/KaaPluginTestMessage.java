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

package org.kaaproject.kaa.server.plugin.rest.test;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class KaaPluginTestMessage implements KaaPluginMessage {

    private static final long serialVersionUID = 100L;

    private static final Logger LOG = LoggerFactory.getLogger(KaaPluginTestMessage.class);

    private UUID id;
    private KaaMessage message;
    private PluginContractItemDef itemDef;
    private PluginContractItemInfo itemInfo;

    public KaaPluginTestMessage(PluginContractItemDef itemDef, PluginContractItemInfo itemInfo, String body) {

        this.id = UUID.randomUUID();

        try {
            this.message = new EndpointMessage(EndpointObjectHash.fromString("remote_endpoint"), body.getBytes());
        } catch (Exception cause) {
            LOG.error("Failed to create a message!", cause);
            throw new IllegalArgumentException(cause);
        }

        this.itemDef = itemDef;
        this.itemInfo = itemInfo;
    }

    @Override
    public UUID getUid() {
        return this.id;
    }

    @Override
    public KaaMessage getMsg() {
        return this.message;
    }

    @Override
    public void setMsg(KaaMessage message) {
        this.message = message;
    }

    @Override
    public PluginContractItemDef getItemDef() {
        return this.itemDef;
    }

    @Override
    public PluginContractItemInfo getItemInfo() {
        return this.itemInfo;
    }
}
