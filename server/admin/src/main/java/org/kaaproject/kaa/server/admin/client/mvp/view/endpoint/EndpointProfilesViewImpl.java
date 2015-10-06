/*
 * Copyright 2014 CyberVision, Inc.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import org.kaaproject.avro.ui.gwt.client.AvroUiResources;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.HasRowActionEventHandlers;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfilesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.EndpointGroupsInfoListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EndpointProfilesViewImpl extends ResizeComposite implements EndpointProfilesView {

    interface EndpointProfilesUiBinder extends UiBinder<Widget, EndpointProfilesViewImpl> { }
    private static EndpointProfilesUiBinder uiBinder = GWT.create(EndpointProfilesUiBinder.class);

    @UiField public DockLayoutPanel dockPanel;
    @UiField public HorizontalPanel backButtonPanel;
    @UiField public Button backButton;
    @UiField public Label titleLabel;
    @UiField public HorizontalPanel filterPanel;
    @UiField (provided=true) public final ImageTextButton addButton;
    @UiField (provided=true) public final AlertPanel errorPanel;
    @UiField (provided=true) public final KaaAdminResources.KaaAdminStyle kaaAdminStyle;
    @UiField (provided=true) public final AvroUiResources.AvroUiStyle avroUiStyle;

    private EndpointGroupsInfoListBox listBox;
    private TextBox endpointKeyHash;
    protected static final int DEFAULT_TEXTBOX_SIZE = 255;
    private Button findEndpointButton;

    protected AbstractGrid<EndpointProfileDto, String> grid;

    protected Presenter presenter;

    protected boolean editable;

    public EndpointProfilesViewImpl() {
        editable = false;
        addButton = new ImageTextButton(Utils.resources.plus(), addButtonString());
        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        kaaAdminStyle = Utils.kaaAdminStyle;
        avroUiStyle = Utils.avroUiStyle;

        initWidget(uiBinder.createAndBindUi(this));

        FlexTable flexTable = new FlexTable();
        Label endpointGroupLabel = new Label("Endpoint group");
        listBox = new EndpointGroupsInfoListBox();
        HorizontalPanel groupPanel = new HorizontalPanel();
        groupPanel.setSpacing(15);
        groupPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        groupPanel.add(endpointGroupLabel);
        groupPanel.add(listBox);

        HorizontalPanel keyHashPanel = new HorizontalPanel();
        keyHashPanel.setSpacing(15);
        keyHashPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Label endpointKeyHashLabel = new Label("Endpoint KeyHash");
        endpointKeyHash = new TextBox();
        endpointKeyHash.setWidth("100%");
        findEndpointButton = new Button("Find");
        findEndpointButton.addStyleName(Utils.avroUiStyle.buttonSmall());
        keyHashPanel.add(endpointKeyHashLabel);
        keyHashPanel.add(endpointKeyHash);
        keyHashPanel.add(findEndpointButton);

        flexTable.setWidget(0, 0, groupPanel);
        flexTable.setWidget(0, 1, keyHashPanel);

        filterPanel.add(flexTable);

        grid = createGrid();

        dockPanel.add(grid);

        titleLabel.setText(titleString());
        addButton.setVisible(editable);

        clearError();
    }

    @Override
    public ValueListBox<EndpointGroupDto> getEndpointGroupsInfo() {
        return listBox;
    }

    @Override
    public Button getFindEndpointButton() {
        return findEndpointButton;
    }

    @Override
    public TextBox getEndpointKeyHashTextBox() {
        return endpointKeyHash;
    }

    private AbstractGrid<EndpointProfileDto, String> createGrid() {
        return new EndpointProfileGrid();
    }

    private String titleString() {
        return Utils.constants.endpointProfiles();
    }

    private String addButtonString() {
        return "";
    }

    @Override
    public HasClickHandlers getAddButton() {
        return addButton;
    }

    @Override
    public HasClickHandlers getBackButton() {
        return backButton;
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        backButtonPanel.setVisible(enabled);
    }

    @Override
    public MultiSelectionModel<EndpointProfileDto> getSelectionModel() {
        return grid.getSelectionModel();
    }

    @Override
    public AbstractGrid<EndpointProfileDto,?> getListWidget() {
        return grid;
    }

    @Override
    public HasRowActionEventHandlers<String> getRowActionsSource() {
        return grid;
    }

    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
