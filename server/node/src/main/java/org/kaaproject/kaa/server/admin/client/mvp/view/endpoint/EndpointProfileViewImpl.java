/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class EndpointProfileViewImpl extends BaseDetailsViewImpl implements EndpointProfileView {

    private SizedTextBox endpointKeyHash;
    private SizedTextBox userID;
    private SizedTextBox userExternalID;
    private List<Widget> userInfoList;

    private SizedTextBox schemaName;
    private SizedTextBox description;
    private RecordPanel schemaForm;

    private Anchor sdkAnchor;
    private AbstractGrid<EndpointGroupDto, String> groupsGrid;
    private TopicGrid topicsGrid;

    public EndpointProfileViewImpl() {
        super(true);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.endpointProfile();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.endpointProfile();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.endpointProfileDetails();
    }

    @Override
    protected void initDetailsTable() {
        saveButton.removeFromParent();
        cancelButton.removeFromParent();
        requiredFieldsNoteLabel.setVisible(false);

        int row = 0;
        Label keyHashLabel = new Label(Utils.constants.endpointKeyHash());
        endpointKeyHash = new KaaAdminSizedTextBox(-1, false);
        endpointKeyHash.setWidth("100%");
        detailsTable.setWidget(row, 0, keyHashLabel);
        detailsTable.setWidget(row, 1, endpointKeyHash);

        userInfoList = new ArrayList<>();
        Label userIDLabel = new Label(Utils.constants.userId());
        userID = new KaaAdminSizedTextBox(-1, false);
        userID.setWidth("100%");
        detailsTable.setWidget(++row, 0, userIDLabel);
        detailsTable.setWidget(row, 1, userID);
        userInfoList.add(userIDLabel);
        userInfoList.add(userID);

        Label userExternalIDLabel = new Label(Utils.constants.userExternalId());
        userExternalID = new KaaAdminSizedTextBox(-1, false);
        userExternalID.setWidth("100%");
        detailsTable.setWidget(++row, 0, userExternalIDLabel);
        detailsTable.setWidget(row, 1, userExternalID);
        userInfoList.add(userExternalIDLabel);
        userInfoList.add(userExternalID);

        Label sdkLabel = new Label(Utils.constants.sdkProfile());
        sdkAnchor = new Anchor();
        sdkAnchor.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        sdkAnchor.setWidth("100%");
        detailsTable.getFlexCellFormatter().setHeight(row, 0, "40px");
        detailsTable.setWidget(row, 0, sdkLabel);
        detailsTable.setWidget(row++, 1, sdkAnchor);

        CaptionPanel formPanel = new CaptionPanel(Utils.constants.profileSchema());
        FlexTable recordTable = new FlexTable();

        int recordTableRow = 0;
        Label schemaNameLabel = new Label(Utils.constants.schemaName());
        schemaName = new KaaAdminSizedTextBox(-1, false);
        schemaName.setWidth("100%");
        recordTable.setWidget(recordTableRow, 0, schemaNameLabel);
        recordTable.setWidget(recordTableRow++, 1, schemaName);

        Label descriptionLabel = new Label(Utils.constants.schemaDescription());
        description = new KaaAdminSizedTextBox(-1, false);
        description.setWidth("100%");
        recordTable.setWidget(recordTableRow, 0, descriptionLabel);
        recordTable.setWidget(recordTableRow++, 1, description);

        schemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(700).createConfig(),
                Utils.constants.schema(), this, false, true);
        recordTable.setWidget(recordTableRow, 0, schemaForm);
        recordTable.getFlexCellFormatter().setColSpan(recordTableRow, 0, 3);

        formPanel.add(recordTable);
        detailsTable.setWidget(++row, 0, formPanel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
        formPanel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        groupsGrid = new EndpointGroupGrid(true);
        groupsGrid.setSize("700px", "200px");
        Label groupsLabel = new Label(Utils.constants.endpointGroups());
        detailsTable.setWidget(++row, 0, groupsLabel);
        groupsLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
        detailsTable.setWidget(++row, 0, groupsGrid);
        groupsGrid.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);

        topicsGrid = new TopicGrid(false, true);
        topicsGrid.setSize("700px", "200px");
        Label topicLabel = new Label(Utils.constants.subscribedOnNfTopics());
        topicLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
        detailsTable.setWidget(++row, 0, topicLabel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
        topicLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
        detailsTable.setWidget(++row, 0, topicsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
        topicsGrid.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
    }

    protected AbstractGrid<EndpointGroupDto, String> createGrid() {
        return new EndpointGroupGrid();
    }

    @Override
    protected void resetImpl() {
        endpointKeyHash.setValue("");
        userID.setValue("");
        userExternalID.setValue("");
        sdkAnchor.setText("");
        schemaName.setValue("");
        description.setValue("");

        schemaForm.getRecordWidget().clear();
    }

    @Override
    protected boolean validate() {
        return false;
    }

    @Override
    public SizedTextBox getKeyHash() {
        return endpointKeyHash;
    }

    @Override
    public SizedTextBox getUserID() {
        return userID;
    }

    @Override
    public SizedTextBox getUserExternalID() {
        return userExternalID;
    }

    @Override
    public List<Widget> getUserInfoList() {
        return userInfoList;
    }

    @Override
    public SizedTextBox getSchemaName() {
        return schemaName;
    }

    @Override
    public SizedTextBox getDescription() {
        return description;
    }

    @Override
    public RecordPanel getSchemaForm() {
        return schemaForm;
    }

    @Override
    public Anchor getSdkAnchor() {
        return sdkAnchor;
    }

    @Override
    public AbstractGrid<EndpointGroupDto, String> getGroupsGrid() {
        return groupsGrid;
    }

    @Override
    public TopicGrid getTopicsGrid() {
        return topicsGrid;
    }
}
