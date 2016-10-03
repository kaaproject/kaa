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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.Prefix;

import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.List;

public class EventClassPlace extends AbstractSchemaPlaceEvent {

  private String ctlSchemaId;
  private String nameEc;

  public EventClassPlace(String ecfId, String ecfVersionId,
                         int ecfVersion,
                         String schemaId,
                         List<EventClassViewDto> eventClassDtoList) {
    super(ecfId, ecfVersionId, ecfVersion, schemaId);
    this.eventClassDtoList = eventClassDtoList;
  }

  public EventClassPlace(String ecfId, String ecfVersionId, int ecfVersion, String schemaId) {
    super(ecfId, ecfVersionId, ecfVersion, schemaId);
  }

  public String getCtlSchemaId() {
    return ctlSchemaId;
  }

  public void setCtlSchemaId(String ctlSchemaId) {
    this.ctlSchemaId = ctlSchemaId;
  }

  public String getNameEc() {
    return nameEc;
  }

  public void setNameEc(String nameEc) {
    this.nameEc = nameEc;
  }

  @Override
  public String getName() {
    return Utils.constants.versions();
  }

  @Override
  public TreePlace createDefaultPreviousPlace() {
    return new EcfVersionPlace(ecfId, ecfVersionId, ecfVersion, eventClassDtoList);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof EventClassPlace)) {
      return false;
    }

    if (!super.equals(object)) {
      return false;
    }

    EventClassPlace that = (EventClassPlace) object;

    return getEcfVersionId() != null
        ? getEcfVersionId().equals(that.getEcfVersionId())
        : that.getEcfVersionId() == null;
  }

  @Override
  public int hashCode() {
    return getEcfVersionId() != null ? getEcfVersionId().hashCode() : 0;
  }

  @Prefix(value = "eventClass")
  public static class Tokenizer extends AbstractSchemaPlaceEvent.Tokenizer<EventClassPlace> {

    @Override
    protected EventClassPlace getPlaceImpl(
            String ecfId, String ecfVersionId, int ecfVersion, String schemaId) {
      return new EventClassPlace(ecfId, ecfVersionId, ecfVersion, schemaId);
    }

  }
}
