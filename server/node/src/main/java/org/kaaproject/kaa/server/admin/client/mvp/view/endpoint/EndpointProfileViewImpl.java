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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EndpointProfileViewImpl extends BaseDetailsViewImpl implements EndpointProfileView {

    private SizedTextBox endpointKeyHash;
    private SizedTextBox userID;
    private SizedTextBox userExternalID;
    private List<Widget> userInfoList;

    private Anchor sdkAnchor;
    private AbstractGrid<EndpointGroupDto, String> groupsGrid;
    private TopicGrid topicsGrid;

    private Anchor endpointProfSchemaName;
    private RecordPanel endpointProfForm;
    private Button downloadEndpointProfileJsonButton;

    private Anchor serverProfSchemaName;
    private RecordPanel serverProfForm;
    private Button downloadServerProfileJsonButton;
    private Button editServerProfileButton;

    public EndpointProfileViewImpl() {
        super(false, false);
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

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "550px");
        detailsTable.getColumnFormatter().setWidth(2, "0px");

        getSaveButtonWidget().removeFromParent();
        getCancelButtonWidget().removeFromParent();
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

        SpanElement span = Document.get().createSpanElement();
        span.appendChild(Document.get().createTextNode(Utils.constants.endpointProfile()));
        span.addClassName("gwt-Label");

        CaptionPanel formPanel = new CaptionPanel(span.getString(), true);
        FlexTable recordTable = new FlexTable();
        recordTable.setWidth("100%");

        Label endpointProfSchemaLabel = new Label(Utils.constants.schemaName());
        endpointProfSchemaName = new Anchor();
        endpointProfSchemaName.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        HorizontalPanel schemaNamePanel = new HorizontalPanel();
        schemaNamePanel.setHeight("40px");
        schemaNamePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        schemaNamePanel.add(endpointProfSchemaLabel);
        schemaNamePanel.add(endpointProfSchemaName);
        schemaNamePanel.setCellWidth(endpointProfSchemaName, "200px");
        endpointProfSchemaName.getElement().getParentElement().getStyle().setPaddingLeft(10, Unit.PX);
        
        HorizontalPanel schemaButtonsPanel = new HorizontalPanel();     
        schemaButtonsPanel.setSpacing(6);
        downloadEndpointProfileJsonButton = new ImageTextButton(Utils.resources.download(), Utils.constants.downloadJson());
        schemaButtonsPanel.add(downloadEndpointProfileJsonButton);
        schemaNamePanel.add(schemaButtonsPanel);
        schemaButtonsPanel.getElement().getParentElement().getStyle().setPaddingLeft(10, Unit.PX);
        
        recordTable.setWidget(0, 0, schemaNamePanel);

        endpointProfForm = new RecordPanel(new AvroWidgetsConfig.Builder().recordPanelWidth(700).createConfig(), Utils.constants.profile(),
                this, true, true);
        endpointProfForm.getRecordWidget().setForceNavigation(true);
        endpointProfForm.setPreferredHeightPx(200);
        recordTable.setWidget(1, 0, endpointProfForm);
        recordTable.getFlexCellFormatter().setColSpan(1, 0, 2);

        formPanel.add(recordTable);

        detailsTable.setWidget(++row, 0, formPanel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);

        formPanel.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);

        span = Document.get().createSpanElement();
        span.appendChild(Document.get().createTextNode(Utils.constants.serverProfile()));
        span.addClassName("gwt-Label");

        CaptionPanel serverFormPanel = new CaptionPanel(span.getString(), true);
        FlexTable serverRecordTable = new FlexTable();
        serverRecordTable.setWidth("100%");

        Label serverProfSchemaLabel = new Label(Utils.constants.schemaName());
        serverProfSchemaName = new Anchor();
        serverProfSchemaName.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        schemaNamePanel = new HorizontalPanel();
        schemaNamePanel.setHeight("40px");
        schemaNamePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        schemaNamePanel.add(serverProfSchemaLabel);
        schemaNamePanel.add(serverProfSchemaName);
        schemaNamePanel.setCellWidth(serverProfSchemaName, "200px");
        serverProfSchemaName.getElement().getParentElement().getStyle().setPaddingLeft(10, Unit.PX);

        schemaButtonsPanel = new HorizontalPanel();     
        schemaButtonsPanel.setSpacing(6);
        downloadServerProfileJsonButton = new ImageTextButton(Utils.resources.download(), Utils.constants.downloadJson());
        schemaButtonsPanel.add(downloadServerProfileJsonButton);
        editServerProfileButton = new Button(Utils.constants.edit());
        schemaButtonsPanel.add(editServerProfileButton);
        schemaNamePanel.add(schemaButtonsPanel);
        schemaButtonsPanel.getElement().getParentElement().getStyle().setPaddingLeft(10, Unit.PX);

        serverRecordTable.setWidget(0, 0, schemaNamePanel);
        serverProfForm = new RecordPanel(new AvroWidgetsConfig.Builder().recordPanelWidth(700).createConfig(), Utils.constants.profile(),
                this, true, true);
        serverProfForm.getRecordWidget().setForceNavigation(true);
        serverProfForm.setPreferredHeightPx(200);
        serverRecordTable.setWidget(1, 0, serverProfForm);
        serverRecordTable.getFlexCellFormatter().setColSpan(1, 0, 2);

        serverFormPanel.add(serverRecordTable);

        detailsTable.setWidget(++row, 0, serverFormPanel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        serverFormPanel.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);

        groupsGrid = new EndpointGroupGrid(true);
        groupsGrid.setSize("100%", "200px");
        Label groupsLabel = new Label(Utils.constants.endpointGroups());
        detailsTable.setWidget(++row, 0, groupsLabel);
        groupsLabel.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);
        detailsTable.setWidget(++row, 0, groupsGrid);
        groupsGrid.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);

        topicsGrid = new TopicGrid(false, true);
        topicsGrid.setSize("100%", "200px");
        Label topicLabel = new Label(Utils.constants.subscribedOnNfTopics());
        topicLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());
        detailsTable.setWidget(++row, 0, topicLabel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        topicLabel.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);
        detailsTable.setWidget(++row, 0, topicsGrid);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        topicsGrid.getElement().getParentElement().getStyle().setPaddingBottom(10, Unit.PX);
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
        endpointProfSchemaName.setText("");
        serverProfSchemaName.setText("");
        endpointProfForm.reset();
        serverProfForm.reset();
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

    public Anchor getEndpointProfSchemaName() {
        return endpointProfSchemaName;
    }

    public RecordPanel getEndpointProfForm() {
        return endpointProfForm;
    }

    @Override
    public Anchor getServerProfSchemaName() {
        return serverProfSchemaName;
    }

    @Override
    public RecordPanel getServerProfForm() {
        return serverProfForm;
    }

    @Override
    public HasClickHandlers getEditServerProfileButton() {
        return editServerProfileButton;
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

    @Override
    public HasClickHandlers getDownloadEndpointProfileJsonButton() {
        return downloadEndpointProfileJsonButton;
    }

    @Override
    public HasClickHandlers getDownloadServerProfileJsonButton() {
        return downloadServerProfileJsonButton;
    }

}
