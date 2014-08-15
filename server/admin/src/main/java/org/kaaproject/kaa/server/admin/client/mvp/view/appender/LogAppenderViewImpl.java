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

package org.kaaproject.kaa.server.admin.client.mvp.view.appender;

import static org.kaaproject.kaa.server.admin.client.util.Utils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FlumeBalancingTypeListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.LogTypeListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class LogAppenderViewImpl extends BaseDetailsViewImpl implements LogAppenderView, ValueChangeHandler<LogAppenderTypeDto> {

    private static final int FULL_TABLE_SIZE = 8;
    private static final int DEFAULT_PRIORITIZED_TABLE_ROW_COUNT = 2;
    private static final int DEFAULT_ROUND_ROBIN_TABLE_ROW_COUNT = 3;

    private SizedTextBox name;
    private CheckBox status;
    private SchemaListBox schema;
    private LogTypeListBox type;
    private FlumeBalancingTypeListBox flumeBalancingType;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private Button activate;
    private Button addHost;
    private Button removeHost;
    private FlexTable hostTable;
    private boolean hasPriority = true;
    private int defaultRowCountLimit = 2;

    private Label typeBalancingLabel;

    private static final String FULL_WIDTH = "100%";

    public LogAppenderViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new SizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(0, 0, authorLabel);
        detailsTable.setWidget(0, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new SizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(1, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(1, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new SizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName("required");
        detailsTable.setWidget(2, 0, nameLabel);
        detailsTable.setWidget(2, 1, name);
        name.addInputHandler(this);

        Label statusLabel = new Label(Utils.constants.activate());
        status = new CheckBox();
        status.setWidth("100%");
        status.setEnabled(false);

        statusLabel.setVisible(!create);
        status.setVisible(!create);

        detailsTable.setWidget(3, 0, statusLabel);
        detailsTable.setWidget(3, 1, status);

        Label schemaLabel = new Label(Utils.constants.schemaVersion());
        schema = new SchemaListBox();
        schema.setEnabled(create);

        detailsTable.setWidget(4, 0, schemaLabel);
        detailsTable.setWidget(4, 1, schema);

        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(5, 0, descriptionLabel);
        detailsTable.setWidget(5, 1, description);
        detailsTable.getCellFormatter().setVerticalAlignment(5, 0, HasVerticalAlignment.ALIGN_TOP);
        description.addInputHandler(this);

        Label typeLabel = new Label(Utils.constants.logAppenderType());
        type = new LogTypeListBox();
        type.setValue(LogAppenderTypeDto.FILE);
        type.setAcceptableValues(Arrays.asList(LogAppenderTypeDto.values()));
        type.setEnabled(create);
        type.addValueChangeHandler(this);

        detailsTable.setWidget(6, 0, typeLabel);
        detailsTable.setWidget(6, 1, type);

        addHost = new Button(Utils.constants.addHost());
        addHost.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int rowCount = hostTable.getRowCount();
                addEmptyRow(rowCount, hasPriority);
                fireChanged();
            }
        });

        removeHost = new Button(Utils.constants.remHost());
        removeHost.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int rowCount = hostTable.getRowCount();
                if (rowCount > defaultRowCountLimit) {
                    hostTable.removeRow(rowCount - 1);
                    fireChanged();
                }
            }
        });
        addHost.addStyleName("button-margin-left");

        getFooter().setStyleName("b-app-content-details-table");
        name.setFocus(true);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addLogAppender();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.logAppender();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.logAppenderDetails();
    }

    @Override
    protected void resetImpl() {
        name.setValue("");
        status.setValue(false);
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        if (type != null) {
            type.setValue(LogAppenderTypeDto.FILE);
        }
        getFooter().clear();
        if (detailsTable.getRowCount() == FULL_TABLE_SIZE) {
            // Remove flumeBalancingType from detail table
            detailsTable.removeRow(FULL_TABLE_SIZE - 1);
        }
    }

    @Override
    protected boolean validate() {
        boolean result = isNotBlank(name.getValue());
        if (hostTable != null && type != null && LogAppenderTypeDto.FLUME.equals(type.getValue())) {
            for (int i = 1; i < hostTable.getRowCount(); i++) {
                result &= isNotBlank(((SizedTextBox) hostTable.getWidget(i, 0)).getValue())
                        && isNotBlank(((SizedTextBox) hostTable.getWidget(i, 1)).getValue());
                if (flumeBalancingType != null && FlumeBalancingTypeDto.PRIORITIZED.equals(flumeBalancingType.getValue())) {
                    result &= isNotBlank(((SizedTextBox) hostTable.getWidget(i, 2)).getValue());
                }
            }
        }
        return result;
    }

    @Override
    public SchemaListBox getSchemaVersions() {
        return schema;
    }

    @Override
    public SizedTextBox getName() {
        return name;
    }

    @Override
    public CheckBox getStatus() {
        return status;
    }

    @Override
    public LogTypeListBox getType() {
        return type;
    }

    @Override
    public SizedTextArea getDescription() {
        return description;
    }

    @Override
    public SizedTextBox getCreatedUsername() {
        return createdUsername;
    }

    @Override
    public SizedTextBox getCreatedDateTime() {
        return createdDateTime;
    }

    @Override
    public Button getActivate() {
        return activate;
    }

    @Override
    public void onValueChange(ValueChangeEvent<LogAppenderTypeDto> event) {
        switch (event.getValue()) {
            case FILE:
            case MONGO:
                hideFlumeCongurationFields();
                fireChanged();
                break;
            case FLUME:
                showFlumeCongurationFields();
                fireChanged();
                break;
        }
    }

    @Override
    public FlumeBalancingTypeListBox getFlumeBalancingType() {
        return flumeBalancingType;
    }

    @Override
    public FlexTable getHostTable() {
        return hostTable;
    }

    @Override
    public void showFlumeCongurationFields(FlumeAppenderParametersDto flumeAppenderParametersDto) {
        hasPriority = flumeAppenderParametersDto.getBalancingType() == FlumeBalancingTypeDto.PRIORITIZED;
        hostTable = generateTable(hasPriority);
        List<HostInfoDto> hosts = flumeAppenderParametersDto.getHosts();
        for (int i = 0; i < hosts.size(); i++) {
            HostInfoDto host = hosts.get(i);
            hostTable.setWidget(i + 1, 0, buildTextBox(DEFAULT_TEXTBOX_SIZE, host.getHostname(), true));
            hostTable.setWidget(i + 1, 1, buildTextBox(5, String.valueOf(host.getPort()), true));
            if (hasPriority) {
                hostTable.setWidget(i + 1, 2, buildTextBox(3, String.valueOf(host.getPriority()), true));
            }
        }
        showFlumeBalancingCongurationFields(false, flumeAppenderParametersDto.getBalancingType());
        getFooter().clear();
        getFooter().add(hostTable);
    }

    private FlexTable generateTable(boolean hasPriority) {
        hostTable = new FlexTable();

        hostTable.getColumnFormatter().setWidth(0, "300px");
        hostTable.getColumnFormatter().setWidth(1, "150px");
        hostTable.setWidget(0, 0, new Label(Utils.constants.host()));
        hostTable.setWidget(0, 1, new Label(Utils.constants.port()));

        if (hasPriority) {
            hostTable.getColumnFormatter().setWidth(2, "150px");
            hostTable.setWidget(0, 2, new Label(Utils.constants.priority()));
        }
        return hostTable;
    }

    private void addEmptyRow(int rowIndex, boolean hasPriority) {
        hostTable.setWidget(rowIndex, 0, buildTextBox(DEFAULT_TEXTBOX_SIZE, null, true));
        hostTable.setWidget(rowIndex, 1, buildTextBox(5, null, true));
        if (hasPriority) {
            hostTable.setWidget(rowIndex, 2, buildTextBox(3, null, true));
        }
    }

    private void showFlumeCongurationFields() {
        showFlumeBalancingCongurationFields(true, FlumeBalancingTypeDto.PRIORITIZED);
        buildParametersHostFields(true, DEFAULT_PRIORITIZED_TABLE_ROW_COUNT);
    }

    private void showFlumeBalancingCongurationFields(boolean isCreate, FlumeBalancingTypeDto value) {
        typeBalancingLabel = new Label(Utils.constants.logAppenderHostsBalancing());
        flumeBalancingType = new FlumeBalancingTypeListBox();
        flumeBalancingType.setValue(value);
        if (isCreate) {
            flumeBalancingType.setAcceptableValues(Arrays.asList(FlumeBalancingTypeDto.values()));
            flumeBalancingType.addValueChangeHandler(new ValueChangeHandler<FlumeBalancingTypeDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<FlumeBalancingTypeDto> event) {
                    if (event.getValue().equals(FlumeBalancingTypeDto.PRIORITIZED)) {
                        buildParametersHostFields(true, DEFAULT_PRIORITIZED_TABLE_ROW_COUNT);
                        fireChanged();
                    } else {
                        buildParametersHostFields(false, DEFAULT_ROUND_ROBIN_TABLE_ROW_COUNT);
                        fireChanged();
                    }
                }
            });
        }
        flumeBalancingType.setEnabled(isCreate);
        detailsTable.setWidget(7, 0, typeBalancingLabel);
        detailsTable.setWidget(7, 1, flumeBalancingType);
    }

    private void hideFlumeCongurationFields() {
        if (typeBalancingLabel != null) {
            typeBalancingLabel.setVisible(false);
        }
        if (flumeBalancingType != null) {
            flumeBalancingType.setVisible(false);
        }
        getFooter().clear();
    }

    private void buildParametersHostFields(final boolean hasPriority, int defaultRowCount) {
        this.hasPriority = hasPriority;
        this.defaultRowCountLimit = defaultRowCount;
        getFooter().clear();
        hostTable = generateTable(hasPriority);
        for (int i = 1; i <= defaultRowCount - 1; i++) {
            addEmptyRow(i, hasPriority);
        }

        getFooter().add(hostTable);
        getFooter().add(addHost);
        getFooter().add(removeHost);
    }

    private SizedTextBox buildTextBox(int textSize, String value, boolean editable) {
        return buildTextBox(textSize, value, editable, FULL_WIDTH);
    }

    private SizedTextBox buildTextBox(int textSize, String value, boolean editable, String width) {
        SizedTextBox textBox = null;
        if (!editable) {
            textBox = new SizedTextBox(-1, false);
        } else {
            textBox = new SizedTextBox(textSize);
        }
        textBox.setWidth(width);
        textBox.addInputHandler(this);
        if (value != null) {
            textBox.setValue(value);
        }
        return textBox;
    }
}
