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

package org.kaaproject.kaa.server.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link Transport} that handles deserialization of
 * binary configuration.
 * 
 * @author Andrew Shvayka
 *
 * @param <T>
 *            specific configuration record type
 */
public abstract class AbstractKaaTransport<T extends SpecificRecordBase> implements Transport {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaTransport.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    protected static final int SIZE_OF_INT = 4;

    protected static final String BIND_INTERFACE_PROP_NAME = "transport_bind_interface";
    protected static final String PUBLIC_INTERFACE_PROP_NAME = "transport_public_interface";
    protected static final String LOCALHOST = "localhost";

    /**
     * A message handler
     */
    protected MessageHandler handler;

    protected SpecificTransportContext<T> context;

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.transport.Transport#init(byte[])
     */
    @Override
    public void init(GenericTransportContext context) throws TransportLifecycleException {
        this.handler = context.getHandler();
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(getConfigurationClass());
        try {
            T config = converter.fromByteArray(context.getConfiguration());
            LOG.info("Initializing transport {} with {}", getClassName(), config);
            this.context = new SpecificTransportContext<T>(context, config);
            init(this.context);
            LOG.info("Transport {} initialized with {}", getClassName(), this.context.getConfiguration());
        } catch (IOException e) {
            LOG.error(MessageFormat.format("Failed to initialize transport {0}", getClassName()), e);
            throw new TransportLifecycleException(e);
        }
    }

    @Override
    public TransportMetaData getConnectionInfo() {
        LOG.info("Serializing connection info");
        ByteBuffer buf = getSerializedConnectionInfo();
        LOG.trace("Serialized connection info is {}", Arrays.toString(buf.array()));
        return new TransportMetaData(getMinSupportedVersion(), getMaxSupportedVersion(), buf.array());
    }

    /**
     * Initializes the transport with specified context.
     *
     * @param context
     *            the initialization context
     * @throws TransportLifecycleException
     */
    protected abstract void init(SpecificTransportContext<T> context) throws TransportLifecycleException;

    /**
     * Gets the configuration class.
     *
     * @return the configuration class
     */
    public abstract Class<T> getConfigurationClass();

    protected abstract ByteBuffer getSerializedConnectionInfo();

    protected abstract int getMinSupportedVersion();

    protected abstract int getMaxSupportedVersion();

    private String getClassName() {
        return this.getClass().getName();
    }

    protected String replaceProperty(String source, String propertyName, String propertyValue) {
        return source.replace("${" + propertyName + "}", propertyValue);
    }

    protected byte[] toUTF8Bytes(String str) {
        return str.getBytes(UTF8);
    }
}
