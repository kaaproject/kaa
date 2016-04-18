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

package org.kaaproject.kaa.server.admin.client.mvp.view.profile;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.ProfileFilterView;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.AbstractRecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.struct.BaseRecordViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.VersionListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProfileFilterViewImpl extends BaseRecordViewImpl<ProfileFilterDto, String> 
                                                                        implements ProfileFilterView {

    private VersionListBox endpointProfileSchema;
    private SizedTextBox endpointProfileSchemaVersion;
    private VersionListBox serverProfileSchema;
    private SizedTextBox serverProfileSchemaVersion;
    
    public ProfileFilterViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected AbstractRecordPanel<ProfileFilterDto, String> createRecordPanel() {
        return new ProfileFilterPanel(this);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.profileFilter();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.profileFilter();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.profileFilterDetails();
    }
    
    
    @Override
    protected int initDetailsTableImpl() {
        int row = -1;
        Label endpointProfileSchemaLabel = new Label(Utils.constants.profileSchemaVersion());
        Label serverProfileSchemaLabel = new Label(Utils.constants.serverProfileSchemaVersion());
        
        int endpointProfileSchemaRow = ++row;
        int serverProfileSchemaRow = ++row;
        
        detailsTable.setWidget(endpointProfileSchemaRow, 0, endpointProfileSchemaLabel);
        detailsTable.setWidget(serverProfileSchemaRow, 0, serverProfileSchemaLabel);

        if (create) {
            endpointProfileSchemaLabel.addStyleName(Utils.avroUiStyle.requiredField());
            serverProfileSchemaLabel.addStyleName(Utils.avroUiStyle.requiredField());
            
            endpointProfileSchema = new VersionListBox();
            endpointProfileSchema.setWidth("80px");
            
            serverProfileSchema = new VersionListBox();
            serverProfileSchema.setWidth("80px");
            
            VerticalPanel panel = new VerticalPanel();
            panel.setWidth("100%");
            panel.add(endpointProfileSchema);
            panel.add(new HTML("&nbsp;"));
            detailsTable.setWidget(endpointProfileSchemaRow, 1, panel);
            
            panel = new VerticalPanel();
            panel.setWidth("100%");
            panel.add(serverProfileSchema);
            panel.add(new HTML("&nbsp;"));
            detailsTable.setWidget(serverProfileSchemaRow, 1, panel);
            
            endpointProfileSchema.addValueChangeHandler(this);
            serverProfileSchema.addValueChangeHandler(this);
        } else {
            endpointProfileSchemaVersion = new KaaAdminSizedTextBox(-1, false);
            endpointProfileSchemaVersion.setWidth("100%");
            detailsTable.setWidget(endpointProfileSchemaRow, 1, endpointProfileSchemaVersion);
            
            serverProfileSchemaVersion = new KaaAdminSizedTextBox(-1, false);
            serverProfileSchemaVersion.setWidth("100%");
            detailsTable.setWidget(serverProfileSchemaRow, 1, serverProfileSchemaVersion);
        }
        return row;
    }
    
    @Override
    protected void resetImpl() {
        super.resetImpl();
        if (create) {
            endpointProfileSchema.reset();
            serverProfileSchema.reset();
        } else {
            endpointProfileSchemaVersion.setValue("");
            serverProfileSchemaVersion.setValue("");
        }
    }

    @Override
    public VersionListBox getEndpointProfileSchema() {
        return endpointProfileSchema;
    }

    @Override
    public HasValue<String> getEndpointProfileSchemaVersion() {
        return endpointProfileSchemaVersion;
    }

    @Override
    public VersionListBox getServerProfileSchema() {
        return serverProfileSchema;
    }

    @Override
    public HasValue<String> getServerProfileSchemaVersion() {
        return serverProfileSchemaVersion;
    }

    @Override
    public HasClickHandlers getTestFilterButton() {
        return ((ProfileFilterPanel)recordPanel).getTestFilterButton();
    }

}
