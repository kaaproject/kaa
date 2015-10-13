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
import com.google.gwt.user.client.ui.Label;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import java.util.List;

public class EndpointProfileViewImpl extends BaseDetailsViewImpl implements EndpointProfileView {


    private SizedTextBox endpointKeyHash;
    private SizedTextBox id;
    private SizedTextBox applicationId;
    private List<EndpointGroupStateDto> nfGroupState;
    private SizedTextBox profileVersion;
    private SizedTextBox configurationHash;
    private SizedTextBox configurationVersion;
    private SizedTextBox notificationVersion;
    private SizedTextBox systemNfVersion;
    private SizedTextBox userNfVersion;
    private SizedTextBox logSchemaVersion;
//    private List<EventClassFamilyVersionStateDto> ecfVersionStates;
    private SizedTextBox serverHash;

//    private AbstractGrid<ConfigurationSchemaDto, String> configurationsGrid;
    private TopicGrid topicsGrid;

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

        Label idLabel = new Label("Endpoint id");
        id = new KaaAdminSizedTextBox(-1, false);
        id.setWidth("100%");
        detailsTable.setWidget(1, 0, idLabel);
        detailsTable.setWidget(1, 1, id);

        Label applicationIdLabel = new Label("Application id");
        applicationId = new KaaAdminSizedTextBox(-1, false);
        applicationId.setWidth("100%");
        detailsTable.setWidget(2, 0, applicationIdLabel);
        detailsTable.setWidget(2, 1, applicationId);

        Label profileVersionLabel = new Label("Profile version");
        profileVersion = new KaaAdminSizedTextBox(-1, false);
        profileVersion.setWidth("100%");
        detailsTable.setWidget(3, 0, profileVersionLabel);
        detailsTable.setWidget(3, 1, profileVersion);

        Label configurationHashLabel = new Label("Configuration hash");
        configurationHash = new KaaAdminSizedTextBox(-1, false);
        configurationHash.setWidth("100%");
        detailsTable.setWidget(4, 0, configurationHashLabel);
        detailsTable.setWidget(4, 1, configurationHash);

        Label configurationVersionLabel = new Label("Configuration version");
        configurationVersion = new KaaAdminSizedTextBox(-1, false);
        configurationVersion.setWidth("100%");
        detailsTable.setWidget(5, 0, configurationVersionLabel);
        detailsTable.setWidget(5, 1, configurationVersion);

        Label notificationVersionLabel = new Label("Notification version");
        notificationVersion = new KaaAdminSizedTextBox(-1, false);
        notificationVersion.setWidth("100%");
        detailsTable.setWidget(6, 0, notificationVersionLabel);
        detailsTable.setWidget(6, 1, notificationVersion);

        Label systemNfVersionLabel = new Label("SystemNf version");
        systemNfVersion = new KaaAdminSizedTextBox(-1, false);
        systemNfVersion.setWidth("100%");
        detailsTable.setWidget(7, 0, systemNfVersionLabel);
        detailsTable.setWidget(7, 1, systemNfVersion);

        Label userNfVersionLabel = new Label("UserNf Version");
        userNfVersion = new KaaAdminSizedTextBox(-1, false);
        userNfVersion.setWidth("100%");
        detailsTable.setWidget(8, 0, userNfVersionLabel);
        detailsTable.setWidget(8, 1, userNfVersion);

        Label logSchemaVersionLabel = new Label("LogSchema version");
        logSchemaVersion = new KaaAdminSizedTextBox(-1, false);
        logSchemaVersion.setWidth("100%");
        detailsTable.setWidget(9, 0, logSchemaVersionLabel);
        detailsTable.setWidget(9, 1, logSchemaVersion);

        Label serverHashLabel = new Label("Server hash");
        serverHash = new KaaAdminSizedTextBox(-1, false);
        serverHash.setWidth("100%");
        detailsTable.setWidget(10, 0, serverHashLabel);
        detailsTable.setWidget(10, 1, serverHash);

//        configurationsGrid = new ConfigurationGrid();
//        configurationsGrid.setSize("700px", "200px");
//        Label configurationsLabel = new Label(Utils.constants.configurations());
//        configurationsLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
//        detailsTable.setWidget(11, 0, configurationsGrid);
//        detailsTable.getFlexCellFormatter().setColSpan(11, 0, 3);

        topicsGrid = new TopicGrid(true) {
            /*
                override this method to avoid modification side effects
             */
            @Override
            protected float constructActions(DataGrid<TopicDto> table, float prefWidth) {
                return 0f;
            }
        };
        topicsGrid.setSize("700px", "200px");
        Label topicsLabel = new Label(Utils.constants.notificationTopics());
        topicsLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
        Label topicLabel = new Label(Utils.constants.notificationTopics());
        topicLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
        detailsTable.setWidget(12, 0, topicLabel);
        detailsTable.setWidget(13, 0, topicsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(13, 0, 3);
    }

    protected AbstractGrid<EndpointGroupDto, String> createGrid() {
        return new EndpointGroupGrid();
    }

    @Override
    protected void resetImpl() {
        endpointKeyHash.setValue("");
        id.setValue("");
        applicationId.setValue("");
        profileVersion.setValue("");
        configurationHash.setValue("");
        configurationVersion.setValue("");
        notificationVersion.setValue("");
        systemNfVersion.setValue("");
        userNfVersion.setValue("");
        logSchemaVersion.setValue("");
        serverHash.setValue("");
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
        return id;
    }

    @Override
    public SizedTextBox getAppId() {
        return applicationId;
    }

    @Override
    public SizedTextBox getProfileVersion() {
        return profileVersion;
    }

    @Override
    public SizedTextBox getConfigurationHash() {
        return configurationHash;
    }

    @Override
    public SizedTextBox getConfigurationVersion() {
        return configurationVersion;
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
    public SizedTextBox getServerHash() {
        return serverHash;
    }

//    @Override
//    public AbstractGrid<ConfigurationSchemaDto, ?> getConfigurationsGrid() {
//        return configurationsGrid;
//    }

    @Override
    public TopicGrid getTopicsGrid() {
        return topicsGrid;
    }
}
