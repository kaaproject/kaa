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

package org.kaaproject.kaa.server.datamigration.model;

public class Ctl {
  private final Long id;
  private final CtlMetaInfo metaInfo;
  private final String defaultRecord;
  private boolean existInDb;

  /**
   * Instantiates a new Ctl.
   *
   */
  public Ctl(Long id, CtlMetaInfo metaInfo, String defaultRecord) {
    this.id = id;
    this.metaInfo = metaInfo;
    this.defaultRecord = defaultRecord;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Ctl ctl = (Ctl) obj;

    if (!metaInfo.equals(ctl.metaInfo)) {
      return false;
    }
    return defaultRecord.equals(ctl.defaultRecord);
  }

  @Override
  public int hashCode() {
    int result = metaInfo.hashCode();
    result = 31 * result + defaultRecord.hashCode();
    return result;
  }

  public Long getId() {
    return id;
  }

  public CtlMetaInfo getMetaInfo() {
    return metaInfo;
  }

  public String getDefaultRecord() {
    return defaultRecord;
  }

  public boolean isExistInDb() {
    return existInDb;
  }

  public void setExistInDb(boolean existInDb) {
    this.existInDb = existInDb;
  }
}
