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
package org.kaaproject.kaa.client.configuration.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.avro.generic.GenericRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.schema.base.Configuration;
import org.mockito.Mockito;

public class ResyncConfigurationManagerTest {

    private static ExecutorContext executorContext;
    private static ExecutorService executor;
    private byte[] configurationData;

    @BeforeClass
    public static void beforeSuite() {
        executorContext = Mockito.mock(ExecutorContext.class);
        executor = Executors.newSingleThreadExecutor();
        Mockito.when(executorContext.getApiExecutor()).thenReturn(new AbstractExecutorService() {

            @Override
            public void execute(Runnable command) {
                command.run();
            }

            @Override
            public List<Runnable> shutdownNow() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void shutdown() {
                // TODO Auto-generated method stub
            }

            @Override
            public boolean isTerminated() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isShutdown() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                // TODO Auto-generated method stub
                return false;
            }
        });
        Mockito.when(executorContext.getCallbackExecutor()).thenReturn(executor);
    }

    @AfterClass
    public static void afterSuite() {
        executor.shutdown();
    }

    @Before
    public void init() throws IOException {
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(Configuration.SCHEMA$);
        configurationData = converter.encode(new Configuration());
    }

    @Test
    public void testEmptyConfigurationStorage() {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ConfigurationStorage storage = Mockito.mock(ConfigurationStorage.class);

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties, executorContext);
        manager.setConfigurationStorage(storage);

        Mockito.when(properties.getDefaultConfigData()).thenReturn(configurationData);

        // empty storage
        Assert.assertNotNull(manager.getConfiguration());

        Mockito.verify(properties).getDefaultConfigData();
    }

    @Test
    public void testConfigurationStorage() throws IOException {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ConfigurationStorage storage = Mockito.mock(ConfigurationStorage.class);

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties, executorContext);
        manager.setConfigurationStorage(storage);

        Mockito.when(properties.getDefaultConfigData()).thenReturn(configurationData);
        Mockito.when(storage.loadConfiguration()).thenReturn(ByteBuffer.wrap(configurationData));

        // empty storage
        Assert.assertNotNull(manager.getConfiguration());

        Mockito.verify(storage).loadConfiguration();
        Mockito.verify(properties, Mockito.never()).getDefaultConfigData();
    }

    @Test
    public void testConfigurationHashContainer() throws IOException {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ConfigurationStorage storage = Mockito.mock(ConfigurationStorage.class);

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties, executorContext);
        manager.setConfigurationStorage(storage);

        Mockito.when(storage.loadConfiguration()).thenReturn(ByteBuffer.wrap(configurationData));

        // empty storage
        Assert.assertNotNull(manager.getConfigurationHashContainer());
        Assert.assertNotNull(manager.getConfigurationHashContainer().getConfigurationHash());
    }

    @Test
    public void testConfigurationListeners() throws IOException {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ConfigurationStorage storage = Mockito.mock(ConfigurationStorage.class);

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties, executorContext);
        manager.setConfigurationStorage(storage);

        Mockito.when(storage.loadConfiguration()).thenReturn(ByteBuffer.wrap(configurationData));

        ConfigurationListener listener = Mockito.mock(ConfigurationListener.class);
        manager.addListener(listener);

        manager.getConfigurationProcessor().processConfigurationData(ByteBuffer.wrap(configurationData), true);
        Mockito.verify(listener, Mockito.timeout(1000)).onConfigurationUpdate(new Configuration());

        Mockito.reset(listener);
        manager.removeListener(listener);
        manager.getConfigurationProcessor().processConfigurationData(ByteBuffer.wrap(configurationData), true);
        Mockito.verify(listener, Mockito.timeout(1000).never()).onConfigurationUpdate(new Configuration());

    }
}
