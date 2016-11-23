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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

import org.kaaproject.kaa.server.admin.client.mvp.view.EventClassView;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EventClassViewImpl extends BaseCtlSchemaViewImpl implements EventClassView {

  private ValueListBox<String> eventClassTypeName;

  public EventClassViewImpl(boolean create) {
    super(create);
  }

  @Override
  protected void initDetailsTable() {
    super.initDetailsTable();

    this.eventClassTypeName = new ValueListBox<String>();
    this.eventClassTypeName.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        EventClassViewImpl.this.fireChanged();
      }
    });
    this.eventClassTypeName.setWidth("100%");

    Label label = new Label("Class type");
    label.addStyleName(this.avroUiStyle.requiredField());
    detailsTable.setWidget(4, 0, label);

    this.detailsTable.setWidget(4, 1, this.eventClassTypeName);

  }

  @Override
  protected String getCreateTitle() {
    return Utils.constants.addEventClass();
  }

  @Override
  protected String getViewTitle() {
    return Utils.constants.eventClass();
  }

  @Override
  protected String getSubTitle() {
    return Utils.constants.eventClassDetails();
  }

  @Override
  public ValueListBox<String> getEventClassTypes() {
    return eventClassTypeName;
  }

  @Override
  protected boolean validate() {
    boolean result = super.validate();
    result &= eventClassTypeName.getValue().length() > 0;
    return result;
  }

  @Override
  protected void updateSaveButton(boolean enabled, boolean invalid) {
    if (create == true) {
      super.updateSaveButton(enabled, invalid);
    } else {
      getSaveButtonWidget().setEnabled(false);
    }
  }
}
