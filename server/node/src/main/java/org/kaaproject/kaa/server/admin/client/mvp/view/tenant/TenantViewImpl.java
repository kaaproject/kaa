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

package org.kaaproject.kaa.server.admin.client.mvp.view.tenant;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.user.UsersGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class TenantViewImpl extends BaseDetailsViewImpl implements TenantView {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private SizedTextBox tenantName;

    private UsersGrid tenantAdminsGrid;

    private Button tenantAdminAddButton;

    private Label lableUser;


    public TenantViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewTenant();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.tenant();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.tenantDetails();
    }

    @Override
    protected void initDetailsTable() {

        tenantName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        tenantName.setWidth("100%");
        tenantName.addInputHandler(this);
        lableUser = new Label("Users");

        Label titleLabel = new Label(Utils.constants.tenantName());
        titleLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(0, 0, titleLabel);
        detailsTable.setWidget(0, 1, tenantName);


            tenantAdminsGrid = new UsersGrid(false, true);
            tenantAdminsGrid.setWidth("100%");
            tenantAdminsGrid.setSize("1000px", "400px");

        detailsTable.getFlexCellFormatter().setColSpan(2, 0, 3);

        tenantAdminAddButton  = new Button(Utils.constants.addNewUser());
        tenantAdminAddButton.addStyleName(Utils.kaaAdminStyle.bAppButtonSmall());
        tenantName.setFocus(true);
        if(!create) {
            detailsTable.setWidget(2,0,tenantAdminsGrid);
            detailsTable.setWidget(3, 3, tenantAdminAddButton);
            detailsTable.setWidget(1, 0, lableUser);
        }
    }

    @Override
    protected void resetImpl() {
        tenantName.setValue("");
    }

    @Override
    protected boolean validate() {
        boolean result = tenantName.getValue().length()>2 && tenantName.getValue().length()<255;
        return result;
    }

    @Override
    public HasValue<String> getTenantName() {
        return tenantName;
    }

    @Override
    public AbstractGrid<UserDto, String> getTenantAdminsGrid() {
        return  tenantAdminsGrid;
    }

    @Override
    public HasClickHandlers getAddTenantAdminButton() {
        return tenantAdminAddButton;
    }



}
