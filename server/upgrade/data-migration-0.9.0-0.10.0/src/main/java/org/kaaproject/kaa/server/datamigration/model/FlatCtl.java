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

import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;

import java.io.IOException;

//Contains data form Ctl and CtlMetaInfo
public class FlatCtl {
  private Long ctlId;
  private String body;
  private Long metaInfoId;
  private String fqn;
  private Long appId;
  private Long tenantId;

  public Long getCtlId() {
    return ctlId;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Long getMetaInfoId() {
    return metaInfoId;
  }

  public void setMetaInfoId(Long metaInfoId) {
    this.metaInfoId = metaInfoId;
  }

  public String getFqn() {
    return fqn;
  }

  public void setFqn(String fqn) {
    this.fqn = fqn;
  }

  public Long getAppId() {
    return appId;
  }

  public void setAppId(Long appId) {
    this.appId = appId;
  }

  public Long getTenantId() {
    return tenantId;
  }

  public void setTenantId(Long tenantId) {
    this.tenantId = tenantId;
  }

  public Ctl toCtl() throws IOException, ConfigurationGenerationException {
    return new Ctl(ctlId, new CtlMetaInfo(
        metaInfoId, fqn, appId, tenantId), generateDefaultRecord());
  }


  private String generateDefaultRecord() throws ConfigurationGenerationException, IOException {
    org.apache.avro.Schema schemaBody = new org.apache.avro.Schema.Parser().parse(body);
    String fqn = schemaBody.getFullName();
    RawSchema rawSchema = new RawSchema(schemaBody.toString());
    DefaultRecordGenerationAlgorithm<RawData> algorithm =
        new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
    return algorithm.getRootData().getRawData();
  }
}
