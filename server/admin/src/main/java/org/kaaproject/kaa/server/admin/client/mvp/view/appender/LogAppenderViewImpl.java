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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AppenderInfoListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FlumeBalancingTypeListBox;
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
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class LogAppenderViewImpl extends BaseDetailsViewImpl implements LogAppenderView, ValueChangeHandler<LogAppenderInfoDto>, ChosenChangeHandler {

    private static final int FULL_TABLE_SIZE = 9;
    private static final int DEFAULT_PRIORITIZED_TABLE_ROW_COUNT = 2;
    private static final int DEFAULT_ROUND_ROBIN_TABLE_ROW_COUNT = 3;
    private static final String REQUIRED = "required";

    private SizedTextBox name;
    private CheckBox status;
    private SchemaListBox schema;
    private AppenderInfoListBox appenderInfo;
    private FlumeBalancingTypeListBox flumeBalancingType;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private Button activate;
    private Button addHost;
    private Button removeHost;
    private FlexTable hostTable;
    private ChosenListBox metadatalistBox;
    private boolean hasPriority = true;
    private int defaultRowCountLimit = 2;

    private Label typeBalancingLabel;

    private Label publicKeyLabel;
    private SizedTextBox sshKey;

    private Label configurationLabel;
    private SizedTextArea configuration;
    
    private static final String FULL_WIDTH = "100%";

    public LogAppenderViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new SizedTextBox(-1, false);
        createdUsername.setWidth(FULL_WIDTH);
        detailsTable.setWidget(0, 0, authorLabel);
        detailsTable.setWidget(0, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new SizedTextBox(-1, false);
        createdDateTime.setWidth(FULL_WIDTH);
        detailsTable.setWidget(1, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(1, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new SizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth(FULL_WIDTH);
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(2, 0, nameLabel);
        detailsTable.setWidget(2, 1, name);
        name.addInputHandler(this);

        Label statusLabel = new Label(Utils.constants.activate());
        status = new CheckBox();
        status.setWidth(FULL_WIDTH);
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

        Label logMetadata = new Label(Utils.constants.logMetada());
        generateMetadataListBox();

        detailsTable.setWidget(5, 0, logMetadata);
        detailsTable.setWidget(5, 1, metadatalistBox);

        description = new SizedTextArea(1024);
        description.setWidth(FULL_WIDTH);
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(6, 0, descriptionLabel);
        detailsTable.setWidget(6, 1, description);
        detailsTable.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
        description.addInputHandler(this);

        Label typeLabel = new Label(Utils.constants.logAppenderType());
        appenderInfo = new AppenderInfoListBox();
        appenderInfo.setValue(new LogAppenderInfoDto(LogAppenderTypeDto.FILE), true);
        appenderInfo.setEnabled(create);
        appenderInfo.addValueChangeHandler(this);

        detailsTable.setWidget(7, 0, typeLabel);
        detailsTable.setWidget(7, 1, appenderInfo);

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

        showFileCongurationFields();
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
        if (metadatalistBox != null) {
            generateMetadataListBox();
        }
        if (appenderInfo != null) {
            appenderInfo.setValue(new LogAppenderInfoDto(LogAppenderTypeDto.FILE), true);
        }
        getFooter().clear();
        if(sshKey != null) {
            sshKey.setValue("");
        }
    }

    @Override
    protected boolean validate() {
        boolean result = isNotBlank(name.getValue());
        if (hostTable != null && appenderInfo != null && LogAppenderTypeDto.FLUME.equals(appenderInfo.getValue())) {
            for (int i = 1; i < hostTable.getRowCount(); i++) {
                result &= isNotBlank(((SizedTextBox) hostTable.getWidget(i, 0)).getValue())
                        && isNotBlank(((SizedTextBox) hostTable.getWidget(i, 1)).getValue());
                if (flumeBalancingType != null && FlumeBalancingTypeDto.PRIORITIZED.equals(flumeBalancingType.getValue())) {
                    result &= isNotBlank(((SizedTextBox) hostTable.getWidget(i, 2)).getValue());
                }
            }
        }
        if (sshKey != null && appenderInfo != null && LogAppenderTypeDto.FILE.equals(appenderInfo.getValue())) {
            result &= isNotBlank(sshKey.getValue());
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
    public AppenderInfoListBox getAppenderInfo() {
        return appenderInfo;
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

    public ChosenListBox getMetadatalistBox() {
        return metadatalistBox;
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
    public String getPublicKey() {
        String key = "";
        if(sshKey != null) {
            key = sshKey.getValue();
        }
        return key;
    }

    @Override
    public void setPublicKey(String publicKey) {
        if(sshKey != null) {
            sshKey.setValue(publicKey);
        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<LogAppenderInfoDto> event) {
        switch (event.getValue().getType()) {
            case FILE:
                hideFlumeCongurationFields();
                hideCustomConfigurationFields();
                showFileCongurationFields();
                fireChanged();
                break;
            case MONGO:
                hideFlumeCongurationFields();
                hideFileCongurationFields();
                hideCustomConfigurationFields();
                fireChanged();
                break;
            case FLUME:
                hideFileCongurationFields();
                hideCustomConfigurationFields();
                showFlumeCongurationFields();
                fireChanged();
                break;
            case CUSTOM:
                hideFlumeCongurationFields();
                hideFileCongurationFields();
                showCustomConfigurationFields();
                configuration.setValue(event.getValue().getDefaultConfig());
                fireChanged();
                break;
        }
    }

    @Override
    public void showFileCongurationFields() {
        publicKeyLabel = new Label(Utils.constants.publicKey());
        publicKeyLabel.addStyleName(REQUIRED);
        sshKey = new SizedTextBox(1000);
        sshKey.setWidth(FULL_WIDTH);
        sshKey.addInputHandler(this);
        sshKey.setEnabled(create);
        detailsTable.setWidget(8, 0, publicKeyLabel);
        detailsTable.setWidget(8, 1, sshKey);
    }

    @Override
    public void hideFileCongurationFields() {
        if (publicKeyLabel != null) {
            detailsTable.remove(publicKeyLabel);
        }
        if (sshKey != null) {
            detailsTable.remove(sshKey);
        }
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
    
    @Override
    public void showCustomConfigurationFields() {
        configurationLabel = new Label(Utils.constants.configuration());
        configurationLabel.addStyleName(REQUIRED);
        configuration = new SizedTextArea(524288);
        configuration.setWidth(FULL_WIDTH);
        configuration.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
        configuration.addInputHandler(this);
        detailsTable.setWidget(8, 0, configurationLabel);
        detailsTable.setWidget(8, 1, configuration);
    }

    @Override
    public String getConfiguration() {
        String configurationString = "";
        if(configuration != null) {
            configurationString = configuration.getValue();
        }
        return configurationString;
    }

    @Override
    public void setConfiguration(String configurationString) {
        if(configuration != null) {
            configuration.setValue(configurationString);
        }
    }

    @Override
    public void hideCustomConfigurationFields() {
        if (configurationLabel != null) {
            detailsTable.remove(configurationLabel);
        }
        if (configuration != null) {
            detailsTable.remove(configuration);
        }
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
        detailsTable.setWidget(8, 0, typeBalancingLabel);
        detailsTable.setWidget(8, 1, flumeBalancingType);
    }

    private void hideFlumeCongurationFields() {
        if (typeBalancingLabel != null) {
            detailsTable.remove(typeBalancingLabel);
        }
        if (flumeBalancingType != null) {
            detailsTable.remove(flumeBalancingType);
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

    private void generateMetadataListBox() {
        if (metadatalistBox != null) {
            metadatalistBox.clear();
        } else {
            metadatalistBox = new ChosenListBox(true);
            metadatalistBox.addChosenChangeHandler(this);
        }
        metadatalistBox.setPixelSize(300, 30);
        metadatalistBox.setPlaceholderText("Select metadata components");
        metadatalistBox.addItem(LogHeaderStructureDto.KEYHASH.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.TIMESTAMP.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.TOKEN.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.VERSION.getValue());
    }

    public void setMetadataListBox(List<LogHeaderStructureDto> header) {
        if (header != null) {
            for (LogHeaderStructureDto field : header) {
                metadatalistBox.setSelectedValue(field.getValue());
            }
        }
    }

    public List<LogHeaderStructureDto> getHeader() {
        List<LogHeaderStructureDto> header = Collections.emptyList();
        if (metadatalistBox != null) {
            String[] selected = metadatalistBox.getValues();
            if (selected != null && selected.length != 0) {
                header = new ArrayList<>();
                for (String field : selected) {
                    for (LogHeaderStructureDto value : LogHeaderStructureDto.values()) {
                        if (value.getValue().equalsIgnoreCase(field)) {
                            header.add(value);
                            continue;
                        }
                    }
                }
            }
        }
        return header;
    }

    @Override
    public void onChange(ChosenChangeEvent event) {
        fireChanged();
    }

}
