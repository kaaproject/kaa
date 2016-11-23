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

package org.kaaproject.kaa.server.control.service.sdk.event;

import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;

import java.util.List;

public class EventFamilyMetadata {

  private String ecfName;
  private String ecfNamespace;
  private String ecfClassName;
  private int version;
  private List<EventClassDto> records;
  private List<String> rawCtlsSchemas;
  private List<ApplicationEventMapDto> eventMaps;

  public List<String> getRawCtlsSchemas() {
    return rawCtlsSchemas;
  }

  public void setRawCtlsSchemas(List<String> rawCtlsSchemas) {
    this.rawCtlsSchemas = rawCtlsSchemas;
  }

  public String getEcfName() {
    return ecfName;
  }

  public void setEcfName(String ecfName) {
    this.ecfName = ecfName;
  }

  public String getEcfNamespace() {
    return ecfNamespace;
  }

  public void setEcfNamespace(String ecfNamespace) {
    this.ecfNamespace = ecfNamespace;
  }

  public String getEcfClassName() {
    return ecfClassName;
  }

  public void setEcfClassName(String ecfClassName) {
    this.ecfClassName = ecfClassName;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public List<ApplicationEventMapDto> getEventMaps() {
    return eventMaps;
  }

  public void setEventMaps(List<ApplicationEventMapDto> eventMaps) {
    this.eventMaps = eventMaps;
  }

  public List<EventClassDto> getRecords() {
    return records;
  }

  public void setRecords(List<EventClassDto> records) {
    this.records = records;
  }
}
