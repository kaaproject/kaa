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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.AefMapView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.EcfListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class AefMapViewImpl extends BaseDetailsViewImpl implements AefMapView {

    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private EcfListBox ecf;
    private SizedTextBox ecfName;
    private SizedTextBox ecfVersion;
    private EventMapGrid eventMapGrid;

    public AefMapViewImpl(boolean create) {
        super(create, create);
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
    public HasValue<EcfInfoDto> getEcf() {
        return ecf;
    }
    
    @Override
    public HasValue<String> getEcfName() {
        return ecfName;
    }

    @Override
    public HasValue<String> getEcfVersion() {
        return ecfVersion;
    }

    @Override
    public AbstractGrid<ApplicationEventMapDto, String> getEventMapGrid() {
        return eventMapGrid;
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addNewAefMap();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.aefMap();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.aefMapDetails();
    }

    @Override
    protected void initDetailsTable() {
        
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(0, 0, authorLabel);
        detailsTable.setWidget(0, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(1, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(1, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);
        
        Label ecfLabel = new Label(Utils.constants.ecf());
        ecfLabel.addStyleName(Utils.avroUiStyle.requiredField());
        ecf = new EcfListBox();
        ecf.setWidth("100%");
        detailsTable.setWidget(2, 0, ecfLabel);
        detailsTable.setWidget(2, 1, ecf);
        
        ecfLabel.setVisible(create);
        ecf.setVisible(create);
        
        ecf.addValueChangeHandler(new ValueChangeHandler<EcfInfoDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<EcfInfoDto> event) {
                fireChanged();
            }
        });
        
        Label ecfNameLabel = new Label(Utils.constants.ecfName());
        ecfName = new KaaAdminSizedTextBox(-1, false);
        ecfName.setWidth("100%");
        detailsTable.setWidget(3, 0, ecfNameLabel);
        detailsTable.setWidget(3, 1, ecfName);
        
        ecfNameLabel.setVisible(!create);
        ecfName.setVisible(!create);

        Label ecfVersionLabel = new Label(Utils.constants.ecfVersion());
        ecfVersion = new KaaAdminSizedTextBox(-1, false);
        ecfVersion.setWidth("100%");
        detailsTable.setWidget(4, 0, ecfVersionLabel);
        detailsTable.setWidget(4, 1, ecfVersion);
        
        ecfVersionLabel.setVisible(!create);
        ecfVersion.setVisible(!create);

        eventMapGrid = new EventMapGrid(create);
        
        eventMapGrid.setSize("700px", "400px");
        Label eventMapLabel = new Label(Utils.constants.eventMap());
        eventMapLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitleLabel());

        detailsTable.setWidget(5, 0, eventMapLabel);
        eventMapLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(6, 0, eventMapGrid);
        detailsTable.getFlexCellFormatter().setColSpan(6, 0, 3);
    }

    @Override
    protected void resetImpl() {
        createdUsername.setValue("");
        createdDateTime.setValue("");
        ecf.reset();
        ecfName.setValue("");
        ecfVersion.setValue("");
    }

    @Override
    protected boolean validate() {
        return ecf.getValue() != null;
    }

    @Override
    public void updateEcfs(List<EcfInfoDto> ecfs) {
        ecf.reset();
        ecf.setAcceptableValues(ecfs);
    }

}
