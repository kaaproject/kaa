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

package org.kaaproject.kaa.server.admin.client.mvp.view.enduser;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.GetUserConfigView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaInfoListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;


public class GetUserConfigViewImpl extends BaseDetailsViewImpl implements GetUserConfigView {

  private SizedTextBox externalUserId;
  private SchemaInfoListBox configurationSchemaInfo;
  private ImageTextButton downloadUserCongigurationButton;

  public GetUserConfigViewImpl() {
    super(true);
    getSaveButtonWidget().setVisible(false);
  }

  @Override
  public HasValue<String> getExternalUserId() {
    return externalUserId;
  }

  @Override
  public ValueListBox<SchemaInfoDto> getConfigurationSchemaInfo() {
    return configurationSchemaInfo;
  }

  @Override
  protected String getCreateTitle() {
    return Utils.constants.getConfiguration();
  }

  @Override
  protected String getViewTitle() {
    return Utils.constants.getConfiguration();
  }

  @Override
  protected String getSubTitle() {
    return Utils.constants.configurationDetails();
  }

  @Override
  public HasClickHandlers getDownloadUserCongigurationButton() {
    return downloadUserCongigurationButton;
  }

  @Override
  protected void initDetailsTable() {
    Label label = new Label(Utils.constants.userId());

    label.addStyleName(Utils.avroUiStyle.requiredField());
    externalUserId = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
    configurationSchemaInfo = new SchemaInfoListBox();
    downloadUserCongigurationButton = new ImageTextButton(Utils.resources.download(), "Download");
    externalUserId.setWidth(FULL_WIDTH);
    externalUserId.addInputHandler(this);
    detailsTable.setWidget(0, 0, label);
    detailsTable.setWidget(0, 1, externalUserId);

    label = new Label(Utils.constants.configurationSchema());
    label.addStyleName(Utils.avroUiStyle.requiredField());
    detailsTable.setWidget(1, 0, label);
    detailsTable.setWidget(1, 1, configurationSchemaInfo);
    detailsTable.setWidget(2, 0, downloadUserCongigurationButton);

    configurationSchemaInfo.addValueChangeHandler(new ValueChangeHandler<SchemaInfoDto>() {
      @Override
      public void onValueChange(ValueChangeEvent<SchemaInfoDto> event) {
        updateConfigurationData(event.getValue());
      }
    });
  }

  @Override
  protected void resetImpl() {
    externalUserId.setValue("");
    configurationSchemaInfo.reset();
  }

  @Override
  protected boolean validate() {
    boolean result = Utils.isNotBlank(externalUserId.getValue());
    result &= configurationSchemaInfo.getValue() != null;
    return result;
  }

  private void updateConfigurationData(SchemaInfoDto value) {
    fireChanged();
    configurationSchemaInfo.setValue(value);
  }

  @Override
  protected void fireChanged() {
    boolean valid = true;
    valid &= validate();
    updateDownloadButton(!valid);
  }

  protected void updateDownloadButton(boolean invalid) {
    if (invalid) {
      downloadUserCongigurationButton.setEnabled(false);
    } else {
      downloadUserCongigurationButton.setEnabled(true);
    }
  }
}
