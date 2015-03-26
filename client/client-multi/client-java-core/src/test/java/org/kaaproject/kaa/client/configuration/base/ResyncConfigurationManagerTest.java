package org.kaaproject.kaa.client.configuration.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.schema.base.Configuration;
import org.mockito.Mockito;

public class ResyncConfigurationManagerTest {

    private byte[] configurationData;

    @Before
    public void init() throws IOException {
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(Configuration.SCHEMA$);
        configurationData = converter.encode(new Configuration());
    }

    @Test
    public void testEmptyConfigurationStorage() {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ConfigurationStorage storage = Mockito.mock(ConfigurationStorage.class);

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties);
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

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties);
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

        ResyncConfigurationManager manager = new ResyncConfigurationManager(properties);
        manager.setConfigurationStorage(storage);

        Mockito.when(storage.loadConfiguration()).thenReturn(ByteBuffer.wrap(configurationData));

        // empty storage
        Assert.assertNotNull(manager.getConfigurationHashContainer());
        Assert.assertNotNull(manager.getConfigurationHashContainer().getConfigurationHash());
    }
}
