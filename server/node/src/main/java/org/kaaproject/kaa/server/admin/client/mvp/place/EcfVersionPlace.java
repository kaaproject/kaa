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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.List;

public class EcfVersionPlace extends SchemasPlaceEvent {

  public EcfVersionPlace(String ecfId, String ecfVersionId, int ecfVersion) {
    super(ecfId, ecfVersionId, ecfVersion);
  }

  public EcfVersionPlace(String ecfId, String ecfVersionId,
                         int ecfVersion, List<EventClassViewDto> eventClassViewDtoList) {
    super(ecfId, ecfVersionId, ecfVersion, eventClassViewDtoList);
  }

  public int getEcfVersion() {
    return ecfVersion;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EcfVersionPlace other = (EcfVersionPlace) obj;
    if (ecfId == null) {
      if (other.ecfId != null) {
        return false;
      }
    } else if (!ecfId.equals(other.ecfId)) {
      return false;
    }
    if (ecfVersion != other.ecfVersion) {
      return false;
    }
    return true;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public TreePlace createDefaultPreviousPlace() {
    return new EcfPlace(ecfId);
  }

  @Prefix(value = "ecfVersion")
  public static class Tokenizer implements PlaceTokenizer<EcfVersionPlace>, PlaceConstants {

    @Override
    public EcfVersionPlace getPlace(String token) {
      PlaceParams.paramsFromToken(token);
      return new EcfVersionPlace(
          PlaceParams.getParam(ECF_ID),
          PlaceParams.getParam(ECF_VERSION_ID),
          PlaceParams.getIntParam(ECF_VERSION));
    }

    @Override
    public String getToken(EcfVersionPlace place) {
      PlaceParams.clear();
      PlaceParams.putParam(ECF_ID, place.getEcfId());
      PlaceParams.putParam(ECF_VERSION_ID, place.getEcfVersionId());
      PlaceParams.putIntParam(ECF_VERSION, place.getEcfVersion());
      return PlaceParams.generateToken();
    }
  }

}
