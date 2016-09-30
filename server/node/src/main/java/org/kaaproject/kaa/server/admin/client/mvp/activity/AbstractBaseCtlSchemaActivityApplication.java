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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractSchemaPlaceApplication;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.BaseSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;

import java.util.List;

public abstract class AbstractBaseCtlSchemaActivityApplication<S extends BaseSchemaDto,
    T extends BaseSchemaViewDto<S>,
    V extends BaseCtlSchemaView,
    P extends AbstractSchemaPlaceApplication>
    extends AbstractBaseCtlSchemaActivity<S, T, V, P>
    implements ErrorMessageCustomizer, FormDataLoader {

  protected String applicationId;

  public AbstractBaseCtlSchemaActivityApplication(P place,
                                                  ClientFactory clientFactory) {
    super(place, clientFactory);
    this.applicationId = place.getApplicationId();
  }

  @Override
  protected T newEntity() {
    T schema = newSchema();
    schema.setApplicationId(applicationId);
    return schema;
  }

  @Override
  protected String getEntityId(P place) {
    return place.getSchemaId();
  }

  @Override
  protected void onEntityRetrieved() {
    if (create) {
      registrations.add(detailsView.getNewCtlButton().addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          CtlSchemaPlace newCtlPlace = new CtlSchemaPlace("", null, CTLSchemaScopeDto.APPLICATION,
              place.getApplicationId(), true, true);
          newCtlPlace.setSchemaType(getPlaceSchemaType());
          newCtlPlace.setPreviousPlace(place);
          canceled = true;
          goTo(newCtlPlace);
        }
      }));
      KaaAdmin.getDataSource().getAvailableApplicationCtlSchemaReferences(applicationId,
          new BusyAsyncCallback<List<CtlSchemaReferenceDto>>() {
            @Override
            public void onFailureImpl(Throwable caught) {
              Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccessImpl(List<CtlSchemaReferenceDto> result) {
              detailsView.getCtlSchemaReference().setAcceptableValues(result);
              bindDetailsView(true);
            }
          });
      detailsView.getSchemaForm().setFormDataLoader(this);
    } else {
      bindDetailsView(false);
    }
  }

  @Override
  protected void doSave(final EventBus eventBus) {
    super.onSave();

    editEntity(entity,
        new BusyAsyncCallback<T>() {
          public void onSuccessImpl(T result) {
            if (!create) {
              goTo(existingSchemaPlace(applicationId, result.getId()));
            } else if (place.getPreviousPlace() != null) {
              goTo(place.getPreviousPlace());
            }
          }

          public void onFailureImpl(Throwable caught) {
            Utils.handleException(caught, detailsView);
          }
        });
  }

}
