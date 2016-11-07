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
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.event.shared.EventBus;

import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.ArrayList;
import java.util.List;

public class SchemasPlaceEvent extends TreePlace {

  protected static List<EventClassViewDto> eventClassDtoList;
  protected String ecfId;
  protected String ecfVersionId;
  protected int ecfVersion;
  private SchemasPlaceDataProvider dataProvider;

  /**
   * Instantiates a new SchemasPlaceEvent.
   */
  public SchemasPlaceEvent(String ecfId, String ecfVersionId, int ecfVersion) {
    this.ecfId = ecfId;
    this.ecfVersionId = ecfVersionId;
    this.ecfVersion = ecfVersion;
  }

  /**
   * Instantiates a new SchemasPlaceEvent.
   */
  public SchemasPlaceEvent(String ecfId, String ecfVersionId,
                           int ecfVersion, List<EventClassViewDto> eventClassDtoList) {
    this.ecfId = ecfId;
    this.ecfVersionId = ecfVersionId;
    this.ecfVersion = ecfVersion;
    this.eventClassDtoList = eventClassDtoList;
  }

  public static List<EventClassViewDto> getEventClassDtoList() {
    return eventClassDtoList;
  }

  public void setEventClassDtoList(List<EventClassViewDto> eventClassDtoList) {
    this.eventClassDtoList = eventClassDtoList;
  }

  /**
   * Add event class view DTO.
   *
   * @param eventClassViewDto the eventClassViewDto
   */
  public void addEventClassViewDto(EventClassViewDto eventClassViewDto) {
    if (eventClassDtoList == null) {
      this.eventClassDtoList = new ArrayList<>();
    }
    eventClassDtoList.add(eventClassViewDto);
  }

  public String getEcfId() {
    return ecfId;
  }

  public String getEcfVersionId() {
    return ecfVersionId;
  }

  public int getEcfVersion() {
    return ecfVersion;
  }

  @Override
  public String getName() {
    return Utils.constants.versions();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
    if (dataProvider == null) {
      dataProvider = new SchemasPlaceDataProvider();
    }
    return dataProvider;
  }

  @Override
  public TreePlace createDefaultPreviousPlace() {
    return new TenantPlace(PlaceParams.getParam(USER_ID));
  }

  public abstract static class Tokenizer<P extends SchemasPlaceEvent>
      implements PlaceTokenizer<P>, PlaceConstants {

    @Override
    public P getPlace(String token) {
      PlaceParams.paramsFromToken(token);
      return getPlaceImpl(
          PlaceParams.getParam(ECF_ID),
          PlaceParams.getParam(ECF_VERSION_ID),
          PlaceParams.getIntParam(ECF_VERSION));
    }

    protected abstract P getPlaceImpl(String ecfId, String ecfVersionId, int ecfVersion);

    @Override
    public String getToken(P place) {
      PlaceParams.clear();
      PlaceParams.putParam(ECF_ID, place.getEcfId());
      PlaceParams.putParam(ECF_VERSION_ID, place.getEcfVersionId());
      PlaceParams.putIntParam(ECF_VERSION, place.getEcfVersion());
      return PlaceParams.generateToken();
    }
  }

  class SchemasPlaceDataProvider extends TreePlaceDataProvider {

    @Override
    protected void loadData(LoadCallback callback,
                            HasData<TreePlace> display) {
      List<TreePlace> result = new ArrayList<TreePlace>();
      result.add(new EcfVersionPlace(ecfId, ecfVersionId, ecfVersion, eventClassDtoList));
      callback.onSuccess(result, display);
    }

  }
}
