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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class EcfSchemaViewImpl extends BaseDetailsViewImpl implements EcfSchemaView {

    private SizedTextBox version;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private SizedTextArea schema;
    private EventClassesGrid eventClassesGrid;

    public EcfSchemaViewImpl() {
        super(false, false);
    }

    @Override
    public HasValue<String> getVersion() {
        return version;
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
    public HasValue<String> getSchema() {
        return schema;
    }

    @Override
    public AbstractGrid<EventClassDto, String> getEventClassesGrid() {
        return eventClassesGrid;
    }

    @Override
    protected String getCreateTitle() {
        return "";
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.ecfSchema();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.ecfSchemaDetails();
    }

    @Override
    protected void initDetailsTable() {
        
        Label versionLabel = new Label(Utils.constants.version());
        version = new SizedTextBox(-1, false);
        version.setWidth("100%");
        detailsTable.setWidget(0, 0, versionLabel);
        detailsTable.setWidget(0, 1, version);
        
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new SizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(1, 0, authorLabel);
        detailsTable.setWidget(1, 1, createdUsername);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new SizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(2, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(2, 1, createdDateTime);
        
        Label schemaLabel = new Label(Utils.constants.schema());
        detailsTable.setWidget(3, 0, schemaLabel);
        
        schema = new SizedTextArea(524288);
        schema.setWidth("500px");
        schema.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 150);
        schema.getTextArea().setReadOnly(true);
        detailsTable.setWidget(3, 1, schema);
        detailsTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

        eventClassesGrid = new EventClassesGrid(Unit.PX);
        eventClassesGrid.setSize("700px", "300px");
        Label eventClassesLabel = new Label(Utils.constants.eventClasses());
        eventClassesLabel.addStyleName("b-app-content-title-label");

        detailsTable.setWidget(4, 0, eventClassesLabel);
        eventClassesLabel.getElement().getParentElement().getStyle().setPropertyPx("paddingBottom", 10);

        detailsTable.setWidget(5, 0, eventClassesGrid);
        detailsTable.getFlexCellFormatter().setColSpan(5, 0, 3);
    }

    @Override
    protected void resetImpl() {
        version.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        schema.setValue("");
    }

    @Override
    protected boolean validate() {
        return true;
    }

}
