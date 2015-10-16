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

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EndpointProfileViewImpl extends BaseDetailsViewImpl implements EndpointProfileView {


    private SizedTextBox endpointKeyHash;
    private SizedTextBox notificationVersion;
    private SizedTextBox systemNfVersion;
    private SizedTextBox userNfVersion;
    private SizedTextBox logSchemaVersion;

    private SizedTextBox userID;
    private SizedTextBox userName;
    private SizedTextBox userExternalID;

    private SizedTextBox schemaName;
    private SizedTextBox description;

    private FlexTable userInfoTable;

    private TopicGrid topicsGrid;
    private AbstractGrid<EndpointGroupDto, String> groupsGrid;

    private RecordPanel schemaForm;

    public EndpointProfileViewImpl() {
        super(true);
    }

    @Override
    protected String getCreateTitle() {
        return "Endpoint Profile";
    }

    @Override
    protected String getViewTitle() {
        return "Endpoint Profile";
    }

    @Override
    protected String getSubTitle() {
        return "Endpoint Profile";
    }

    @Override
    protected void initDetailsTable() {
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        requiredFieldsNoteLabel.setVisible(false);

        Label keyHashLabel = new Label("Endpoint key hash");
        endpointKeyHash = new KaaAdminSizedTextBox(-1, false);
        endpointKeyHash.setWidth("100%");
        detailsTable.setWidget(0, 0, keyHashLabel);
        detailsTable.setWidget(0, 1, endpointKeyHash);

        userInfoTable = new FlexTable();

        Label userIDLabel = new Label("User id");
        userID = new KaaAdminSizedTextBox(-1, false);
        userID.setWidth("100%");
        userInfoTable.setWidget(0, 0, userIDLabel);
        userInfoTable.setWidget(0, 1, userID);

        Label userNameLabel = new Label("User name");
        userName = new KaaAdminSizedTextBox(-1, false);
        userName.setWidth("100%");
        userInfoTable.setWidget(1, 0, userNameLabel);
        userInfoTable.setWidget(1, 1, userName);

        Label userExternalIDLabel = new Label("User external ID");
        userExternalID = new KaaAdminSizedTextBox(-1, false);
        userExternalID.setWidth("100%");
        userInfoTable.setWidget(2, 0, userExternalIDLabel);
        userInfoTable.setWidget(2, 1, userExternalID);
        detailsTable.setWidget(1, 0, userInfoTable);

        Label notificationVersionLabel = new Label("Notification version");
        notificationVersion = new KaaAdminSizedTextBox(-1, false);
        notificationVersion.setWidth("100%");
        detailsTable.setWidget(4, 0, notificationVersionLabel);
        detailsTable.setWidget(4, 1, notificationVersion);

        Label systemNfVersionLabel = new Label("System notification version");
        systemNfVersion = new KaaAdminSizedTextBox(-1, false);
        systemNfVersion.setWidth("100%");
        detailsTable.setWidget(5, 0, systemNfVersionLabel);
        detailsTable.setWidget(5, 1, systemNfVersion);

        Label userNfVersionLabel = new Label("User notification Version");
        userNfVersion = new KaaAdminSizedTextBox(-1, false);
        userNfVersion.setWidth("100%");
        detailsTable.setWidget(6, 0, userNfVersionLabel);
        detailsTable.setWidget(6, 1, userNfVersion);

        Label logSchemaVersionLabel = new Label("LogSchema version");
        logSchemaVersion = new KaaAdminSizedTextBox(-1, false);
        logSchemaVersion.setWidth("100%");
        detailsTable.setWidget(7, 0, logSchemaVersionLabel);
        detailsTable.setWidget(7, 1, logSchemaVersion);

        CaptionPanel formPanel = new CaptionPanel("Profile Record");
        FlexTable recordTable = new FlexTable();

        Label schemaNameLabel = new Label("Schema name");
        schemaName = new KaaAdminSizedTextBox(-1, false);
        schemaName.setWidth("100%");
        recordTable.setWidget(1, 0, schemaNameLabel);
        recordTable.setWidget(1, 1, schemaName);

        Label descriptionLabel = new Label("Schema description");
        description = new KaaAdminSizedTextBox(-1, false);
        description.setWidth("100%");
        recordTable.setWidget(2, 0, descriptionLabel);
        recordTable.setWidget(2, 1, description);

        schemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(700).createConfig(),
                Utils.constants.schema(), this, false, true);
        recordTable.setWidget(3, 0, schemaForm);
        recordTable.getFlexCellFormatter().setColSpan(3, 0, 3);

        formPanel.add(recordTable);
        detailsTable.setWidget(8, 0, formPanel);
        detailsTable.getFlexCellFormatter().setColSpan(8, 0, 3);

        groupsGrid = new EndpointGroupGrid(true) {

            @Override
            protected float constructActions(DataGrid<EndpointGroupDto> table, float prefWidth) {
                return 0F;
            }
        };
        groupsGrid.setSize("700px", "200px");
        Label groupsLabel = new Label("Endpoint groups");
        detailsTable.setWidget(12, 0, groupsLabel);
        detailsTable.setWidget(13, 0, groupsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(13, 0, 3);

        topicsGrid = new TopicGrid(true) {
            /*    overriding this method to avoid modification side effects    */
            @Override
            protected float constructActions(DataGrid<TopicDto> table, float prefWidth) {
                return 0F;
            }
        };
        topicsGrid.setSize("700px", "200px");
        Label topicLabel = new Label(Utils.constants.notificationTopics());
        topicLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
        detailsTable.setWidget(14, 0, topicLabel);
        detailsTable.setWidget(15, 0, topicsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(15, 0, 3);
    }

    protected AbstractGrid<EndpointGroupDto, String> createGrid() {
        return new EndpointGroupGrid();
    }

    @Override
    protected void resetImpl() {
//        endpointKeyHash.setValue("");
//        notificationVersion.setValue("");
//        systemNfVersion.setValue("");
//        userNfVersion.setValue("");
//        logSchemaVersion.setValue("");
//
//        userID.setValue("");
//        userName.setValue("");
//        userExternalID.setValue("");
    }



    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    public SizedTextBox getKeyHash() {
        return endpointKeyHash;
    }

    @Override
    public SizedTextBox getId() {
        return null;
    }

    @Override
    public SizedTextBox getNotificationVersion() {
        return notificationVersion;
    }

    @Override
    public SizedTextBox getSystemNfVersion() {
        return systemNfVersion;
    }

    @Override
    public SizedTextBox getUserNfVersion() {
        return userNfVersion;
    }

    @Override
    public SizedTextBox getLogSchemaVer() {
        return logSchemaVersion;
    }

    @Override
    public TopicGrid getTopicsGrid() {
        return topicsGrid;
    }

    @Override
    public AbstractGrid<EndpointGroupDto, String> getGroupsGrid() {
        return groupsGrid;
    }

    @Override
    public SizedTextBox getUserID() {
        return userID;
    }

    @Override
    public SizedTextBox getUserName() {
        return userName;
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
    public SizedTextBox getUserExternalID() {
        return userExternalID;
    }

    @Override
    public RecordPanel getSchemaForm() {
        return schemaForm;
    }

    @Override
    public FlexTable getUserInfoTable() {
        return userInfoTable;
    }
}
