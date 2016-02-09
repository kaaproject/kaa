/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.view.tenant;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.TenantView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class TenantViewImpl extends BaseDetailsViewImpl implements TenantView {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private SizedTextBox tenantName;
    private SizedTextBox userName;
    private SizedTextBox email;

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

        Label titleLabel = new Label(Utils.constants.tenantName());
        titleLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(0, 0, titleLabel);
        detailsTable.setWidget(0, 1, tenantName);

        userName = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE, create);
        userName.setWidth("100%");
        userName.addInputHandler(this);

        Label userLabel = new Label(Utils.constants.tenantAdminUsername());
        if (create) {
            userLabel.addStyleName(REQUIRED);
        }
        detailsTable.setWidget(1, 0, userLabel);
        detailsTable.setWidget(1, 1, userName);

        email = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        email.setWidth("100%");
        email.addInputHandler(this);

        Label emailLabel = new Label(Utils.constants.tenantAdminEmail());
        emailLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(2, 0, emailLabel);
        detailsTable.setWidget(2, 1, email);

        tenantName.setFocus(true);
    }

    @Override
    protected void resetImpl() {
        tenantName.setValue("");
        userName.setValue("");
        email.setValue("");
    }

    @Override
    protected boolean validate() {
        boolean result = tenantName.getValue().length()>0;
        result &= userName.getValue().length()>0;
        result &= Utils.validateEmail(email.getValue());
        return result;
    }

    @Override
    public HasValue<String> getTenantName() {
        return tenantName;
    }

    @Override
    public HasValue<String> getUserName() {
        return userName;
    }

    @Override
    public HasValue<String> getEmail() {
        return email;
    }

    @Override
    public HasValue<KaaAuthorityDto> getAuthority() {
        return new HasValue<KaaAuthorityDto>() {

            @Override
            public HandlerRegistration addValueChangeHandler(
                    ValueChangeHandler<KaaAuthorityDto> handler) {
                return null;
            }

            @Override
            public void fireEvent(GwtEvent<?> event) {
            }

            @Override
            public KaaAuthorityDto getValue() {
                return KaaAuthorityDto.TENANT_ADMIN;
            }

            @Override
            public void setValue(KaaAuthorityDto value) {
            }

            @Override
            public void setValue(KaaAuthorityDto value, boolean fireEvents) {
            }

        };
    }

}
