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

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.StructureRecordKey;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.BaseStructGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;

public class EndpointGroupViewImpl extends BaseDetailsViewImpl implements EndpointGroupView {

    private Label nameLabel;
    private SizedTextBox name;
    private Label weightLabel;
    private IntegerBox weight;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private SizedTextBox endpointCount;

    private CheckBox includeDeprecatedProfileFilters;
    private BaseStructGrid<ProfileFilterDto> profileFiltersGrid;
    private CheckBox includeDeprecatedConfigurations;
    private BaseStructGrid<ConfigurationDto> configurationsGrid;
    private TopicGrid topicsGrid;

    private Button addProfileFilterButton;
    private Button addConfigurationButton;
    private Button addTopicButton;

    public EndpointGroupViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewEndpointGroup();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.endpointGroup();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.endpointGroupDetails();
    }

    @Override
    protected void initDetailsTable() {

        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new SizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(0, 0, authorLabel);
        detailsTable.setWidget(0, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new SizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(1, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(1, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        Label endpointCountLabel = new Label(Utils.constants.numberOfEndpoints());
        endpointCount = new SizedTextBox(-1, false);
        endpointCount.setWidth("100%");
        detailsTable.setWidget(2, 0, endpointCountLabel);
        detailsTable.setWidget(2, 1, endpointCount);

        endpointCountLabel.setVisible(!create);
        endpointCount.setVisible(!create);

        name = new SizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        nameLabel = new Label(Utils.constants.name());
        detailsTable.setWidget(3, 0, nameLabel);
        detailsTable.setWidget(3, 1, name);
        name.addInputHandler(this);

        weight = new IntegerBox();
        weight.setWidth("100%");
        weightLabel = new Label(Utils.constants.weight());
        detailsTable.setWidget(4, 0, weightLabel);
        detailsTable.setWidget(4, 1, weight);
        weight.addChangeHandler(this);

        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        description.getTextArea().getElement().getStyle().setPropertyPx("maxWidth", 487);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(5, 0, descriptionLabel);
        detailsTable.setWidget(5, 1, description);
        detailsTable.getFlexCellFormatter().setColSpan(5, 1, 2);
        description.addInputHandler(this);
        detailsTable.getCellFormatter().setVerticalAlignment(5, 0, HasVerticalAlignment.ALIGN_TOP);

        profileFiltersGrid = new BaseStructGrid<ProfileFilterDto>();
        profileFiltersGrid.setSize("700px", "200px");
        Label profileFiltersLabel = new Label(Utils.constants.profileFilters());
        profileFiltersLabel.addStyleName("b-app-content-title-label");
        includeDeprecatedProfileFilters = new CheckBox(Utils.constants.includeDeprecated());
        setCheckBoxStyle(includeDeprecatedProfileFilters);

        addProfileFilterButton = new Button(Utils.constants.addProfileFilter());
        addProfileFilterButton.addStyleName("b-app-button-small");

        detailsTable.setWidget(6, 0, profileFiltersLabel);
        profileFiltersLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(6, 1, includeDeprecatedProfileFilters);


        detailsTable.setWidget(7, 0, profileFiltersGrid);
        detailsTable.getFlexCellFormatter().setColSpan(7, 0, 3);

        detailsTable.setWidget(8, 2, addProfileFilterButton);
        addProfileFilterButton.getElement().getParentElement().getStyle().setPropertyPx("paddingTop", 15);
        detailsTable.getCellFormatter().setHorizontalAlignment(8, 2, HasHorizontalAlignment.ALIGN_RIGHT);

        profileFiltersLabel.setVisible(!create);
        includeDeprecatedProfileFilters.setVisible(!create);
        profileFiltersGrid.setVisible(!create);

        configurationsGrid = new BaseStructGrid<ConfigurationDto>();
        configurationsGrid.setSize("700px", "200px");
        Label configurationsLabel = new Label(Utils.constants.configurations());
        configurationsLabel.addStyleName("b-app-content-title-label");
        includeDeprecatedConfigurations = new CheckBox(Utils.constants.includeDeprecated());
        setCheckBoxStyle(includeDeprecatedConfigurations);

        addConfigurationButton = new Button(Utils.constants.addConfiguration());
        addConfigurationButton.addStyleName("b-app-button-small");

        detailsTable.setWidget(9, 0, configurationsLabel);
        configurationsLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(9, 1, includeDeprecatedConfigurations);

        detailsTable.setWidget(10, 0, configurationsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(10, 0, 3);

        detailsTable.setWidget(11, 2, addConfigurationButton);
        addConfigurationButton.getElement().getParentElement().getStyle().setPropertyPx("paddingTop", 15);
        detailsTable.getCellFormatter().setHorizontalAlignment(11, 2, HasHorizontalAlignment.ALIGN_RIGHT);

        configurationsLabel.setVisible(!create);
        includeDeprecatedConfigurations.setVisible(!create);
        configurationsGrid.setVisible(!create);

        topicsGrid = new TopicGrid(true);
        topicsGrid.setSize("700px", "200px");
        Label topicsLabel = new Label(Utils.constants.notificationTopics());
        topicsLabel.addStyleName("b-app-content-title-label");

        addTopicButton = new Button(Utils.constants.addNotificationTopic());
        addTopicButton.addStyleName("b-app-button-small");

        detailsTable.setWidget(12, 0, topicsLabel);
        topicsLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(13, 0, topicsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(13, 0, 3);

        detailsTable.setWidget(14, 2, addTopicButton);
        addTopicButton.getElement().getParentElement().getStyle().setPropertyPx("paddingTop", 15);
        detailsTable.getCellFormatter().setHorizontalAlignment(14, 2, HasHorizontalAlignment.ALIGN_RIGHT);

        topicsLabel.setVisible(!create);
        topicsGrid.setVisible(!create);

        name.setFocus(true);
    }

    private void setCheckBoxStyle(CheckBox box) {
        Element input = box.getElement().getFirstChildElement();
        input.getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        Element label = input.getNextSiblingElement();
        label.getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        label.getStyle().setPaddingLeft(5, Unit.PX);
        label.getStyle().setFontSize(13, Unit.PX);
    }

    @Override
    protected void resetImpl() {
        name.setValue("");
        name.setEnabled(true);
        nameLabel.addStyleName("required");
        weight.setValue(null);
        weight.setEnabled(true);
        weightLabel.addStyleName("required");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        endpointCount.setValue("");
        addProfileFilterButton.setVisible(!create);
        addConfigurationButton.setVisible(!create);
        addTopicButton.setVisible(!create);
        profileFiltersGrid.setEnableActions(true);
        configurationsGrid.setEnableActions(true);
        includeDeprecatedProfileFilters.setVisible(!create);
        includeDeprecatedProfileFilters.setValue(false);
        includeDeprecatedConfigurations.setVisible(!create);
        includeDeprecatedConfigurations.setValue(false);
    }

    @Override
    protected boolean validate() {
        boolean result = name.getValue().length()>0;
        result &= weight.getValue() != null;
        return result;
    }

    @Override
    public void setReadOnly() {
        name.setEnabled(false);
        nameLabel.removeStyleName("required");
        weight.setEnabled(false);
        weightLabel.removeStyleName("required");
        addProfileFilterButton.setVisible(false);
        addConfigurationButton.setVisible(false);
        includeDeprecatedProfileFilters.setVisible(false);
        includeDeprecatedConfigurations.setVisible(false);
        profileFiltersGrid.setEnableActions(false);
        configurationsGrid.setEnableActions(false);
    }



    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public HasValue<Integer> getWeight() {
        return weight;
    }

    @Override
    public HasValue<String> getDescription() {
        return description;
    }

    @Override
    public HasValue<String> getCreatedUsername() {
        return createdUsername;
    }

    @Override
    public HasValue<String> getCreatedDateTime() {
        return createdDateTime;
    }

    @Override
    public HasValue<String> getEndpointCount() {
        return endpointCount;
    }

    @Override
    public AbstractGrid<StructureRecordDto<ProfileFilterDto>, StructureRecordKey> getProfileFiltersGrid() {
        return profileFiltersGrid;
    }

    @Override
    public AbstractGrid<StructureRecordDto<ConfigurationDto>, StructureRecordKey> getConfigurationsGrid() {
        return configurationsGrid;
    }

    @Override
    public AbstractGrid<TopicDto, String> getTopicsGrid() {
        return topicsGrid;
    }


    @Override
    public HasClickHandlers getAddProfileFilterButton() {
        return addProfileFilterButton;
    }

    @Override
    public HasClickHandlers getAddConfigurationButton() {
        return addConfigurationButton;
    }

    @Override
    public HasClickHandlers getAddTopicButton() {
        return addTopicButton;
    }

    @Override
    public HasValue<Boolean> getIncludeDeprecatedProfileFilters() {
        return includeDeprecatedProfileFilters;
    }

    @Override
    public HasValue<Boolean> getIncludeDeprecatedConfigurations() {
        return includeDeprecatedConfigurations;
    }

}
