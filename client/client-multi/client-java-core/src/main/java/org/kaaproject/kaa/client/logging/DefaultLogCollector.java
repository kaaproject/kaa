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
package org.kaaproject.kaa.client.logging;

import java.io.IOException;

import javax.annotation.Generated;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.schema.base.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of @see LogCollector
 * 
 * @author Andrew Shvayka
 */
@Generated("DefaultLogCollector.java.template")
public class DefaultLogCollector extends AbstractLogCollector {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogCollector.class);

    public DefaultLogCollector(LogTransport transport, ExecutorContext executorContext, KaaChannelManager manager) {
        super(transport, executorContext, manager);
    }

    @Override
    public synchronized void addLogRecord(final Log record) {
        executorContext.getApiExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    storage.addLogRecord(new LogRecord(record));
                } catch (IOException e) {
                    LOG.warn("Can't serialize log record {}", record);
                }

                if (!isDeliveryTimeout()) {
                    uploadIfNeeded();
                }
            }
        });
    }

}
