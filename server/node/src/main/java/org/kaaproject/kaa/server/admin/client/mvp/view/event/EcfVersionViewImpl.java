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

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfVersionView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EcfVersionViewImpl extends BaseListViewImpl<EventClassDto> implements EcfVersionView {

  @UiField
  public final ImageTextButton addEcButton;

  /**
   * Instantiates a new EcfVersionViewImpl.
   */
  public EcfVersionViewImpl(boolean editable) {
    super(true);
    this.addEcButton = new ImageTextButton(Utils.resources.plus(), addButtonEventClassString());
    addEcButton.setVisible(editable);
    addButton.setVisible(editable);
    supportPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    supportPanel.setWidth("300px");
    supportPanel.add(addEcButton);
  }

  @Override
  protected void initAddButton() {
    addButton = new ImageTextButton(null, addButtonString());
  }

  @Override
  protected AbstractGrid<EventClassDto, String> createGrid() {
    return new EcfVersionGrid();
  }

  @Override
  protected String titleString() {
    return Utils.constants.familyVersion();
  }

  @Override
  protected String addButtonString() {
    return Utils.constants.save();
  }

  private String addButtonEventClassString() {
    return Utils.constants.addEventClass();
  }

  @Override
  public Button addButtonEventClass() {
    return addEcButton;
  }

  @Override
  public Button addButton() {
    return addButton;
  }

}
