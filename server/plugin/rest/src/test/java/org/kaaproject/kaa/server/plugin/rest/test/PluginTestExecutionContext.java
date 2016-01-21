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

package org.kaaproject.kaa.server.plugin.rest.test;

import java.io.IOException;
import java.util.UUID;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bohdan Khablenko
 */
public class PluginTestExecutionContext<T extends SpecificRecordBase> implements PluginExecutionContext {

    private static final Logger LOG = LoggerFactory.getLogger(PluginTestExecutionContext.class);

    public PluginTestExecutionContext(Class<T> type) {
        try {
            T instance = type.newInstance();
            this.converter = new AvroJsonConverter<T>(instance.getSchema(), type);
        } catch (Exception cause) {
            LOG.error("Failed to create execution context!", cause);
            throw new IllegalArgumentException(cause);
        }
    }

    private EndpointMessage message;

    public EndpointMessage getEndpointMessage() {
        return this.message;
    }

    private final AvroJsonConverter<T> converter;

    public T getMessageContent() {
        try {
            byte[] bytes = this.message.getMessageData();
            return (bytes != null) ? this.converter.decodeJson(bytes) : null;
        } catch (IOException cause) {
            LOG.error("Failed to decode the message!", cause);
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void tellEndpoint(EndpointObjectHash endpointKey, KaaMessage message) {
        LOG.error("Not yet implemented!");
        throw new UnsupportedOperationException();
    }

    @Override
    public void tellPlugin(UUID id, KaaMessage message) {
        this.message = (EndpointMessage) message;
    }
}
