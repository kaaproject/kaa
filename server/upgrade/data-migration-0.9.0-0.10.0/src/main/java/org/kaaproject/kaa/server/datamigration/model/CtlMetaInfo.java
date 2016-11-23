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

public class CtlMetaInfo {
  private final Long id;
  private final String fqn;
  private final Long appId;
  private final Long tenantId;

  /**
   * Create new instance of ctl meta info.
   *
   * @param id       the uniq identifier
   * @param fqn      the full qualified name of type
   * @param appId    the application id
   * @param tenantId the tenant id
   */
  public CtlMetaInfo(Long id, String fqn, Long appId, Long tenantId) {
    this.id = id;
    this.fqn = fqn;
    this.appId = appId;
    this.tenantId = tenantId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    CtlMetaInfo that = (CtlMetaInfo) obj;

    if (!fqn.equals(that.fqn)) {
      return false;
    }

    if (appId != null ? !appId.equals(that.appId) : that.appId != null) {
      return false;
    }
    return tenantId != null ? tenantId.equals(that.tenantId) : that.tenantId == null;
  }

  @Override
  public int hashCode() {
    int result = fqn.hashCode();
    result = 31 * result + (appId != null ? appId.hashCode() : 0);
    result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
    return result;
  }

  public Long getAppId() {
    return appId;
  }

  public Long getTenantId() {
    return tenantId;
  }

  public String getFqn() {
    return fqn;
  }

  public Long getId() {
    return id;
  }
}
