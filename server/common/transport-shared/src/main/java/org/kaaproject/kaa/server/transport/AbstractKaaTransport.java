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

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract implementation of {@link Transport} that handles deserialization of
 * binary configuration.
 *
 * @param <T> specific configuration record type
 * @author Andrew Shvayka
 */
public abstract class AbstractKaaTransport<T extends SpecificRecordBase> implements Transport {
  protected static final int SIZE_OF_INT = 4;
  protected static final String BIND_INTERFACE_PROP_NAME = "transport_bind_interface";
  protected static final String PUBLIC_INTERFACE_PROP_NAME = "transport_public_interface";
  protected static final String LOCALHOST = "localhost";
  private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaTransport.class);
  private static final Charset UTF8 = Charset.forName("UTF-8");

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
    } catch (IOException ex) {
      LOG.error(MessageFormat.format("Failed to initialize transport {0}", getClassName()), ex);
      throw new TransportLifecycleException(ex);
    }
  }

  /**
   * Initializes the transport with specified context.
   *
   * @param context the initialization context
   */
  protected abstract void init(SpecificTransportContext<T> context)
      throws TransportLifecycleException;


  @Override
  public TransportMetaData getConnectionInfo() {
    LOG.info("Serializing connection info");
    List<byte[]> buffs = getSerializedConnectionInfoList();
    for (byte[] buf : buffs) {
      LOG.trace("Serialized connection info is {}", Arrays.toString(buf));
    }
    return new TransportMetaData(getMinSupportedVersion(), getMaxSupportedVersion(), buffs);
  }


  /**
   * Gets the configuration class.
   *
   * @return the configuration class
   */
  public abstract Class<T> getConfigurationClass();

  protected abstract List<byte[]> getSerializedConnectionInfoList();

  protected abstract int getMinSupportedVersion();

  protected abstract int getMaxSupportedVersion();

  private String getClassName() {
    return this.getClass().getName();
  }

  protected String replaceProperty(String source, String propertyName, String propertyValue) {
    return source.replace("${" + propertyName + "}", propertyValue);
  }

  protected byte[] toUtf8Bytes(String str) {
    return str.getBytes(UTF8);
  }
}
