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

package org.kaaproject.kaa.server.admin.client.mvp.view.sdk;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.SdkProfileView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.event.AefMapsGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class SdkProfileViewImpl extends BaseDetailsViewImpl implements SdkProfileView {

    private SizedTextBox sdkName;
    private SizedTextBox sdkAuthor;
    private SizedTextBox sdkDateCreated;
    private Anchor sdkConfigurationVersion;
    private Anchor sdkProfileVersion;
    private Anchor sdkNotificationVersion;
    private Anchor sdkLoggingVersion;
    private SizedTextBox sdkProfileToken;

    private AbstractGrid<ApplicationEventFamilyMapDto, String> sdkAefMapsGrid;

    public SdkProfileViewImpl() {
        super(false, false);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.sdkProfile();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.sdkProfile();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.sdkProfileDetails();
    }

    @Override
    protected void initDetailsTable() {
        getSaveButtonWidget().removeFromParent();
        getCancelButtonWidget().removeFromParent();
        requiredFieldsNoteLabel.setVisible(false);

        int row = 0;
        Label sdkTokenLabel = new Label(Utils.constants.sdkToken());
        sdkProfileToken = new KaaAdminSizedTextBox(-1, false);
        sdkProfileToken.setWidth("100%");
        detailsTable.setWidget(row, 0, sdkTokenLabel);
        detailsTable.setWidget(row++, 1, sdkProfileToken);

        Label sdkNameLabel = new Label(Utils.constants.sdkName());
        sdkName = new KaaAdminSizedTextBox(-1, false);
        sdkName.setWidth("100%");
        detailsTable.setWidget(row, 0, sdkNameLabel);
        detailsTable.setWidget(row++, 1, sdkName);

        Label sdkAuthorLabel = new Label(Utils.constants.author());
        sdkAuthor = new KaaAdminSizedTextBox(-1, false);
        sdkAuthor.setWidth("100%");
        detailsTable.setWidget(row, 0, sdkAuthorLabel);
        detailsTable.setWidget(row++, 1, sdkAuthor);

        Label sdkDateCreatedLabel = new Label(Utils.constants.dateCreated());
        sdkDateCreated = new KaaAdminSizedTextBox(-1, false);
        sdkDateCreated.setWidth("100%");
        detailsTable.setWidget(row, 0, sdkDateCreatedLabel);
        detailsTable.setWidget(row++, 1, sdkDateCreated);

        Label sdkConfigurationVersionLabel = new Label(Utils.constants.configurationSchema());
        sdkConfigurationVersion = new Anchor();
        sdkConfigurationVersion.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        sdkConfigurationVersion.setWidth("100%");
        detailsTable.getFlexCellFormatter().setHeight(row, 0, "40px");
        detailsTable.setWidget(row, 0, sdkConfigurationVersionLabel);
        detailsTable.setWidget(row++, 1, sdkConfigurationVersion);

        Label sdkProfileVersionLabel = new Label(Utils.constants.profileSchema());
        sdkProfileVersion = new Anchor();
        sdkProfileVersion.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        sdkProfileVersion.setWidth("100%");
        detailsTable.getFlexCellFormatter().setHeight(row, 0, "40px");
        detailsTable.setWidget(row, 0, sdkProfileVersionLabel);
        detailsTable.setWidget(row++, 1, sdkProfileVersion);

        Label sdkNotificationVersionLabel = new Label(Utils.constants.notificationSchema());
        sdkNotificationVersion = new Anchor();
        sdkNotificationVersion.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        sdkNotificationVersion.setWidth("100%");
        detailsTable.getFlexCellFormatter().setHeight(row, 0, "40px");
        detailsTable.setWidget(row, 0, sdkNotificationVersionLabel);
        detailsTable.setWidget(row++, 1, sdkNotificationVersion);

        Label sdkLoggingVersionLabel = new Label(Utils.constants.logSchema());
        sdkLoggingVersion = new Anchor();
        sdkLoggingVersion.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        sdkLoggingVersion.setWidth("100%");
        detailsTable.getFlexCellFormatter().setHeight(row, 0, "40px");
        detailsTable.setWidget(row, 0, sdkLoggingVersionLabel);
        detailsTable.setWidget(row++, 1, sdkLoggingVersion);

        sdkAefMapsGrid = new AefMapsGrid();
        sdkAefMapsGrid.setSize("700px", "200px");
        Label sdkAefMapsGridLabel = new Label(Utils.constants.aefMaps());
        detailsTable.setWidget(++row, 0, sdkAefMapsGridLabel);
        sdkAefMapsGridLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
        detailsTable.setWidget(++row, 0, sdkAefMapsGrid);
        sdkAefMapsGrid.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
    }

    @Override
    protected void resetImpl() {
        sdkName.setValue("");
        sdkAuthor.setValue("");
        sdkDateCreated.setValue("");
        sdkConfigurationVersion.setText("");
        sdkProfileVersion.setText("");
        sdkNotificationVersion.setText("");
        sdkLoggingVersion.setText("");
        sdkProfileToken.setValue("");
    }

    @Override
    protected boolean validate() {
        return false;
    }

    @Override
    public SizedTextBox getSdkName() {
        return sdkName;
    }

    @Override
    public SizedTextBox getSdkAuthor() {
        return sdkAuthor;
    }

    @Override
    public SizedTextBox getSdkDateCreated() {
        return sdkDateCreated;
    }

    @Override
    public Anchor getSdkConfigurationVersion() {
        return sdkConfigurationVersion;
    }

    @Override
    public Anchor getSdkProfileVersion() {
        return sdkProfileVersion;
    }

    @Override
    public Anchor getSdkNotificationVersion() {
        return sdkNotificationVersion;
    }

    @Override
    public Anchor getSdkLoggingVersion() {
        return sdkLoggingVersion;
    }

    @Override
    public SizedTextBox getSdkProfileToken() {
        return sdkProfileToken;
    }

    @Override
    public AbstractGrid<ApplicationEventFamilyMapDto, String> getSdkAefMapsGrid() {
        return sdkAefMapsGrid;
    }
}
