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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ValueListBox;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;
import org.kaaproject.kaa.server.admin.client.mvp.place.EcfVersionPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EventClassPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EventClassView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.ArrayList;
import java.util.List;

public class EventClassActivity
    extends AbstractBaseCtlSchemaActivityEvent<EventClassDto, EventClassViewDto,
    EventClassView, EventClassPlace> {

  public EventClassActivity(EventClassPlace place, ClientFactory clientFactory) {
    super(place, clientFactory);
  }

  @Override
  protected EventClassViewDto newSchema() {
    return new EventClassViewDto();
  }

  @Override
  protected EventClassView getView(boolean create) {
    if (create) {
      return clientFactory.getCreateEventClassView();
    } else {
      return clientFactory.getEventClassView();
    }
  }

  @Override
  protected void getEntity(String eventClassId,
                           final AsyncCallback<EventClassViewDto> callback) {
    if (place.getEventClassDtoList() != null) {
      if (!place.getEventClassDtoList().isEmpty()
          && (Integer.valueOf(entityId) <= place.getEventClassDtoList().size())) {

        EventClassDto eventClassDto = place.getEventClassDtoList()
            .get(Integer.valueOf(eventClassId) - 1)
            .getSchema();
        detailsView.getEventClassTypes().setValue(eventClassDto.getType().name());
        KaaAdmin.getDataSource().getEventClassViewByCtlSchemaId(eventClassDto, callback);
      } else {
        getEventClassView(eventClassId, callback);
      }
    } else {
      getEventClassView(eventClassId, callback);
    }
  }

  private void getEventClassView(String eventClassId,
                                 final AsyncCallback<EventClassViewDto> callback) {
    KaaAdmin.getDataSource().getEventClassView(eventClassId,
        new AsyncCallback<EventClassViewDto>() {
          @Override
          public void onFailure(Throwable caught) {
            Utils.handleException(caught, EventClassActivity.this.detailsView);
          }

          @Override
          public void onSuccess(EventClassViewDto eventClassViewDto) {
            detailsView.getEventClassTypes()
                .setValue(eventClassViewDto.getSchema().getType().name());
            callback.onSuccess(eventClassViewDto);
          }
        });
  }

  @Override
  protected void editEntity(final EventClassViewDto eventClassViewDto,
                            final AsyncCallback<EventClassViewDto> callback) {
    KaaAdmin.getDataSource().saveEventClassView(eventClassViewDto,
        new AsyncCallback<EventClassViewDto>() {
          @Override
          public void onFailure(Throwable caught) {
            Utils.handleException(caught, EventClassActivity.this.detailsView);
          }

          @Override
          public void onSuccess(EventClassViewDto eventClassViewDto) {
            place.addEventClassViewDto(eventClassViewDto);
            callback.onSuccess(eventClassViewDto);
          }
        });
  }

  @Override
  protected void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback) {
    KaaAdmin.getDataSource().createNewCtlSchemaFormInstance(null,
        null,
        null,
        ConverterType.FORM_AVRO_CONVERTER,
        callback);
  }

  @Override
  public void loadFormData(String fileItemName,
                           AsyncCallback<RecordField> callback) {
    KaaAdmin.getDataSource().generateCommonSchemaForm(fileItemName, callback);
  }

  @Override
  protected EventClassPlace existingSchemaPlaceForEvent(String ecfId, String ecfVersionId,
                                                        int ecfVersion, String schemaId) {
    return new EventClassPlace(ecfId, ecfVersionId, ecfVersion,
        schemaId, place.getEventClassDtoList());
  }

  @Override
  protected SchemaType getPlaceSchemaType() {
    return SchemaType.EVENT_CLASS;
  }

  @Override
  protected void onSave() {
    super.onSave();
    entity.getSchema().setType(EventClassType.valueOf(detailsView.getEventClassTypes().getValue()));
  }

  @Override
  protected void doSave(final EventBus eventBus) {
    onSave();

    editEntity(entity,
        new BusyAsyncCallback<EventClassViewDto>() {
          public void onSuccessImpl(EventClassViewDto result) {
            if (!create) {
              goTo(existingSchemaPlaceForEvent(place.getEcfId(), place.getEcfVersionId(),
                  place.getEcfVersion(), result.getId()));
            } else {
              goTo(new EcfVersionPlace(place.getEcfId(), place.getEcfVersionId(),
                  place.getEcfVersion(), place.getEventClassDtoList()));
            }
          }

          public void onFailureImpl(Throwable caught) {
            Utils.handleException(caught, detailsView);
          }
        });
  }

  @Override
  protected void onEntityRetrieved() {
    super.onEntityRetrieved();
    ValueListBox<String> eventClassTypes = this.detailsView.getEventClassTypes();
    if (eventClassTypes != null) {
      List<String> eventClassTypeList = new ArrayList<>();
      for (EventClassType eventClassType : EventClassType.values()) {
        eventClassTypeList.add(eventClassType.name());
      }
      EventClassActivity.this.detailsView
          .getEventClassTypes()
          .setAcceptableValues(eventClassTypeList);
    }

    if (place.getCtlSchemaId() != null) {
      KaaAdmin.getDataSource().getLastCtlSchemaReferenceDto(place.getCtlSchemaId(),
          new AsyncCallback<CtlSchemaReferenceDto>() {

            @Override
            public void onFailure(Throwable caught) {
              Utils.handleException(caught, EventClassActivity.this.detailsView);
            }

            @Override
            public void onSuccess(CtlSchemaReferenceDto ctlSchemaReferenceDto) {
              detailsView.getCtlSchemaReference().setValue(ctlSchemaReferenceDto);
              detailsView.getName().setValue(place.getNameEc());
              place.setCtlSchemaId(null);
            }
          });
    }
  }
}
