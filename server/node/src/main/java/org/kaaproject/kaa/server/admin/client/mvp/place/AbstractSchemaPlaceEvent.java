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
import com.google.web.bindery.event.shared.EventBus;

public abstract class AbstractSchemaPlaceEvent extends SchemasPlaceEvent {

  protected String schemaId;

  public AbstractSchemaPlaceEvent(String ecfId,
                                  String ecfVersionId,
                                  int ecfVersion,
                                  String schemaId) {
    super(ecfId, ecfVersionId, ecfVersion);
    this.schemaId = schemaId;
  }

  public String getSchemaId() {
    return schemaId;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
    return null;
  }

  public abstract static class Tokenizer<P extends AbstractSchemaPlaceEvent>
      implements PlaceTokenizer<P>, PlaceConstants {

    @Override
    public P getPlace(String token) {
      PlaceParams.paramsFromToken(token);
      return getPlaceImpl(
          PlaceParams.getParam(ECF_ID),
          PlaceParams.getParam(ECF_VERSION_ID),
          PlaceParams.getIntParam(ECF_VERSION),
          PlaceParams.getParam(SCHEMA_ID));
    }

    protected abstract P getPlaceImpl(String ecfId,
                                      String ecfVersionId,
                                      int ecfVersion,
                                      String schemaId);

    @Override
    public String getToken(P place) {
      PlaceParams.clear();
      PlaceParams.putParam(ECF_ID, place.getEcfId());
      PlaceParams.putParam(ECF_VERSION, String.valueOf(place.getEcfVersion()));
      PlaceParams.putParam(ECF_VERSION_ID, place.getEcfVersionId());
      PlaceParams.putParam(SCHEMA_ID, String.valueOf(place.getSchemaId()));
      return PlaceParams.generateToken();
    }
  }

}
