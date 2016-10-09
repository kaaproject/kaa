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

package org.kaaproject.kaa.client.configuration.base;

import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleConfigurationStorage implements ConfigurationStorage {
  private static final int _8KB = 1024 * 8; //NOSONAR
  private static final Logger LOG = LoggerFactory.getLogger(SimpleConfigurationStorage.class);

  private final KaaClientPlatformContext context;
  private final String path;

  public SimpleConfigurationStorage(KaaClientPlatformContext context, String path) {
    this.context = context;
    this.path = path;
  }

  @Override
  public void clearConfiguration() throws IOException {
    PersistentStorage storage = context.createPersistentStorage();
    if (storage.exists(path)) {
      storage.delete(path);
    } else {
      LOG.trace("There is no configuration to clear yet");
    }
  }

  @Override
  public void saveConfiguration(ByteBuffer buffer) throws IOException {
    PersistentStorage storage = context.createPersistentStorage();
    BufferedOutputStream os = new BufferedOutputStream(storage.openForWrite(path));
    byte[] data = new byte[buffer.remaining()];
    buffer.get(data);
    LOG.trace("Writing {} bytes to output stream", data.length);
    os.write(data);
    os.close();
  }

  @Override
  public ByteBuffer loadConfiguration() throws IOException {
    PersistentStorage storage = context.createPersistentStorage();
    if (!storage.exists(path)) {
      LOG.trace("There is no configuration in storage yet");
      return null;
    }
    BufferedInputStream is = new BufferedInputStream(storage.openForRead(path));
    List<byte[]> chunks = new ArrayList<byte[]>();
    byte[] tmp = new byte[_8KB];
    int size = 0;
    while (true) {
      int result = is.read(tmp);
      LOG.trace("Reading {} bytes from input stream", result);
      if (result > 0) {
        size += result;
        chunks.add(Arrays.copyOf(tmp, result));
      }
      if (result < tmp.length) {
        break;
      }
    }
    ByteBuffer data;
    if (size > 0) {
      data = ByteBuffer.wrap(new byte[size]);
      for (byte[] chunk : chunks) {
        data.put(chunk);
      }
      data.rewind();
    } else {
      data = null;
    }
    is.close();
    return data;
  }
}
