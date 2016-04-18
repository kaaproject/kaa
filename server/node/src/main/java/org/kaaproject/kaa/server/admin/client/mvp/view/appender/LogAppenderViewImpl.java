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

package org.kaaproject.kaa.server.admin.client.mvp.view.appender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.plugin.BasePluginViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.IntegerListBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class LogAppenderViewImpl extends BasePluginViewImpl implements LogAppenderView, ChosenChangeHandler {

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();

    private IntegerListBox minSchemaVersion;
    private IntegerListBox maxSchemaVersion;
    private CheckBox confirmDelivery;
    private ChosenListBox metadatalistBox;
    
    private List<Integer> schemaVersions;

    public LogAppenderViewImpl(boolean create) {
        super(create);
    }
    
    @Override
    protected int initPluginDetails(int idx) {
        Label minSchemaVersionLabel = new Label(Utils.constants.minSchemaVersion());
        minSchemaVersionLabel.addStyleName(REQUIRED);
        minSchemaVersion = new IntegerListBox();
        minSchemaVersion.setWidth("30%");
        
        minSchemaVersion.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateMaxSchemaVersions();
                fireChanged();
            }
        });
        
        idx++;
        detailsTable.setWidget(idx, 0, minSchemaVersionLabel);
        detailsTable.setWidget(idx, 1, minSchemaVersion);

        Label maxSchemaVersionLabel = new Label(Utils.constants.maxVersion());
        maxSchemaVersionLabel.addStyleName(REQUIRED);
        maxSchemaVersion = new IntegerListBox();
        maxSchemaVersion.setWidth("30%");
        
        maxSchemaVersion.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                fireChanged();
            }
        });

        idx++;
        detailsTable.setWidget(idx, 0, maxSchemaVersionLabel);
        detailsTable.setWidget(idx, 1, maxSchemaVersion);
        
        confirmDelivery = new CheckBox();
        confirmDelivery.setWidth("100%");
        Label confirmDeliveryLabel = new Label(Utils.constants.confirmDelivery());
        idx++;
        detailsTable.setWidget(idx, 0, confirmDeliveryLabel);
        detailsTable.setWidget(idx, 1, confirmDelivery);
        confirmDelivery.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fireChanged();
            }
        });

        Label logMetadata = new Label(Utils.constants.logMetadata());
        generateMetadataListBox();
        idx++;
        detailsTable.setWidget(idx, 0, logMetadata);
        detailsTable.setWidget(idx, 1, metadatalistBox);
        return idx;
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
    public HasValue<Boolean> getConfirmDelivery() {
        return confirmDelivery;
    }

    @Override
    protected void resetImpl() {
        super.resetImpl();
        minSchemaVersion.reset();
        maxSchemaVersion.reset();
        confirmDelivery.setValue(true);
        if (metadatalistBox != null) {
            generateMetadataListBox();
        }
    }

    @Override
    protected boolean validate() {
        boolean result = super.validate();
        result &= minSchemaVersion.getValue() != null;
        result &= maxSchemaVersion.getValue() != null;
        return result;
    }

    @Override
    public ValueListBox<Integer> getMinSchemaVersion() {
        return minSchemaVersion;
    }

    @Override
    public ValueListBox<Integer> getMaxSchemaVersion() {
        return maxSchemaVersion;
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
        for(LogHeaderStructureDto headerDto: LogHeaderStructureDto.values()){
            metadatalistBox.addItem(headerDto.getValue());
        }
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
    
    private void updateMaxSchemaVersions() {
        if (schemaVersions != null) {
           Integer minVersionValue = minSchemaVersion.getValue();
           List<Integer> maxSchemaVersions = null;
           Integer maxVersionValue = maxSchemaVersion.getValue();
           
           if (minVersionValue != null) {
               maxSchemaVersions = new ArrayList<>();
               for (Integer version : schemaVersions) {
                   if (version >= minVersionValue) {
                       maxSchemaVersions.add(version);
                   }
               }
               if (maxVersionValue != null && maxVersionValue < minVersionValue) {
                   maxVersionValue = minVersionValue;
               }
           } else {
               maxSchemaVersions = new ArrayList<>(schemaVersions);
           }
           maxSchemaVersions.add(Integer.MAX_VALUE);
           if (maxVersionValue == null) {
               maxVersionValue = Integer.MAX_VALUE;
           }
           maxSchemaVersion.setValue(maxVersionValue);
           maxSchemaVersion.setAcceptableValues(maxSchemaVersions);
        }
    }
    
    @Override
    public void setSchemaVersions(List<Integer> schemaVersions) {
        this.schemaVersions = schemaVersions;
        if (minSchemaVersion.getValue() == null && !schemaVersions.isEmpty()) {
            minSchemaVersion.setValue(schemaVersions.get(0));
        }
        minSchemaVersion.setAcceptableValues(schemaVersions);
        updateMaxSchemaVersions();
    }

    @Override
    public void onChange(ChosenChangeEvent event) {
        fireChanged();
    }

}
