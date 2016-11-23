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

package org.kaaproject.kaa.client.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FilePersistentStorage implements PersistentStorage {

  @Override
  public InputStream openForRead(String path) throws IOException {
    return new FileInputStream(new File(path));
  }

  @Override
  public OutputStream openForWrite(String path) throws IOException {
    File file = new File(path);
    if (file.getParentFile() != null && !file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    return new FileOutputStream(file);
  }

  @Override
  public boolean exists(String path) {
    return new File(path).exists();
  }

  @Override
  public void delete(String path) throws IOException {
    new File(path).delete();
  }

  @Override
  public boolean renameTo(String oldPath, String newPath) throws IOException {
    return new File(oldPath).renameTo(new File(newPath));
  }

}
