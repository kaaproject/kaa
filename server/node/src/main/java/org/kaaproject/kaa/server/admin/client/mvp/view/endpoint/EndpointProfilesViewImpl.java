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

package org.kaaproject.kaa.server.admin.client.mvp.view.endpoint;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.EndpointGroupsInfoListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EndpointProfilesViewImpl
    extends BaseListViewImpl<EndpointProfileDto>
    implements EndpointProfilesView {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private EndpointGroupsInfoListBox listBox;
  private TextBox endpointKeyHash;
  private Button resetButton;
  private Button findButton;

  private RadioButton endpointGroupButton;
  private RadioButton endpointKeyHashButton;

  /**
   * Instantiates a new EndpointProfilesViewImpl.
   */
  public EndpointProfilesViewImpl() {
    super(false);

    supportPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    supportPanel.setWidth("1000px");

    resetButton = new Button(Utils.constants.reset());
    supportPanel.add(resetButton);

    endpointGroupButton = new RadioButton("filter", Utils.constants.endpointGroup());
    listBox = new EndpointGroupsInfoListBox();
    listBox.getElement().getStyle().setPropertyPx("minWidth", 100);
    HorizontalPanel groupPanel = new HorizontalPanel();
    groupPanel.setSpacing(15);
    groupPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    groupPanel.add(endpointGroupButton);
    groupPanel.add(listBox);
    supportPanel.add(groupPanel);

    HorizontalPanel keyHashPanel = new HorizontalPanel();
    keyHashPanel.setSpacing(15);
    keyHashPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    endpointKeyHashButton = new RadioButton("filter", Utils.constants.endpointKeyHash());
    endpointKeyHash = new TextBox();
    endpointKeyHash.setWidth("100%");
    findButton = new Button(Utils.constants.find());
    findButton.addStyleName(Utils.avroUiStyle.buttonSmall());
    keyHashPanel.add(endpointKeyHashButton);
    keyHashPanel.add(endpointKeyHash);
    keyHashPanel.add(findButton);
    supportPanel.add(keyHashPanel);

    endpointGroupButton.setValue(true);
  }

  @Override
  public EndpointGroupsInfoListBox getEndpointGroupsInfo() {
    return listBox;
  }

  @Override
  public Button getFindEndpointButton() {
    return findButton;
  }

  @Override
  public TextBox getEndpointKeyHashTextBox() {
    return endpointKeyHash;
  }

  @Override
  protected AbstractGrid<EndpointProfileDto, String> createGrid() {
    return new EndpointProfileGrid(DEFAULT_PAGE_SIZE);
  }

  @Override
  protected String titleString() {
    return Utils.constants.endpointProfiles();
  }

  @Override
  protected String addButtonString() {
    return "";
  }

  @Override
  public Button getResetButton() {
    return resetButton;
  }

  @Override
  public HasValue<Boolean> getEndpointGroupButton() {
    return endpointGroupButton;
  }

  @Override
  public HasValue<Boolean> getEndpointKeyHashButton() {
    return endpointKeyHashButton;
  }

}
