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

import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.CtlSchemaPlace.SchemaType;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel.FormDataLoader;
import org.kaaproject.kaa.server.admin.client.util.ErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.SchemaErrorMessageCustomizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.BaseSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;

public abstract class AbstractBaseCtlSchemaActivity<S extends BaseSchemaDto,
    T extends BaseSchemaViewDto<S>,
    V extends BaseCtlSchemaView,
    P extends TreePlace>
    extends AbstractDetailsActivity<T, V, P>
    implements ErrorMessageCustomizer, FormDataLoader {

  private static final ErrorMessageCustomizer schemaErrorMessageCustomizer =
      new SchemaErrorMessageCustomizer();

  public AbstractBaseCtlSchemaActivity(P place, ClientFactory clientFactory) {
    super(place, clientFactory);
  }

  protected abstract T newSchema();

  protected P existingSchemaPlace(String applicationId, String schemaId) {
    return null;
  }

  protected P existingSchemaPlaceForEvent(String ecfId, String ecfVersionId, int ecfVersion,
                                          String schemaId) {
    return null;
  }

  protected abstract void createEmptyCtlSchemaForm(AsyncCallback<CtlSchemaFormDto> callback);

  @Override
  protected void bind(final EventBus eventBus) {
    if (create) {
      detailsView.getSchemaForm().setFormDataLoader(this);
    }
    super.bind(eventBus);
  }

  protected abstract SchemaType getPlaceSchemaType();

  protected void bindDetailsView(boolean fireChanged) {
    S schema = entity.getSchema();
    String version = schema.getVersion() + "";
    detailsView.getVersion().setValue(version);
    detailsView.getName().setValue(schema.getName());
    detailsView.getDescription().setValue(schema.getDescription());
    detailsView.getCreatedUsername().setValue(schema.getCreatedUsername());
    detailsView.getCreatedDateTime()
        .setValue(Utils.millisecondsToDateTimeString(schema.getCreatedTime()));
    if (entity.getCtlSchemaForm() != null) {
      detailsView.getSchemaForm().setValue(entity.getCtlSchemaForm().getSchema(), fireChanged);
    }
  }

  @Override
  protected void onSave() {
    S schema = entity.getSchema();
    schema.setName(detailsView.getName().getValue());
    schema.setDescription(detailsView.getDescription().getValue());
    if (create) {
      entity.setUseExistingCtlSchema(detailsView.useExistingCtlSchema());
      if (detailsView.useExistingCtlSchema()) {
        entity.setExistingMetaInfo(detailsView.getCtlSchemaReference().getValue());
      }
    }
  }

  @Override
  public String customizeErrorMessage(Throwable caught) {
    String errorMessage = schemaErrorMessageCustomizer.customizeErrorMessage(caught);
    if (errorMessage == null) {
      errorMessage = "Incorrect schema: Please validate your schema.";
    }
    return errorMessage;
  }

}
