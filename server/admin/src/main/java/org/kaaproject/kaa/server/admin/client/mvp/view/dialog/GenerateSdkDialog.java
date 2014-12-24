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

package org.kaaproject.kaa.server.admin.client.mvp.view.dialog;

import static org.kaaproject.kaa.server.admin.client.util.Utils.getMaxSchemaVersions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AlertPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.MultiAefMapListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GenerateSdkDialog extends KaaDialog implements HasErrorMessage {

    private static final String REQUIRED = Utils.fieldWidgetStyle.requiredField();

    private AlertPanel errorPanel;

    private SchemaListBox configurationSchemaVersion;
    private SchemaListBox profileSchemaVersion;
    private SchemaListBox notificationSchemaVersion;
    private SchemaListBox logSchemaVersion;
    private ValueListBox<SdkPlatform> targetPlatform;

    private List<AefMapInfoDto> aefMaps;
    private AefMapInfoDtoComparator aefMapComparator;

    private MultiAefMapListBox availableAefMaps;
    private MultiAefMapListBox selectedAefMaps;

    private Button addAefMapButton;
    private Button removeAefMapButton;

    private String applicationId;

    private Button generateSdkButton;

    public static void showGenerateSdkDialog(final String applicationId, final AsyncCallback<GenerateSdkDialog> callback) {
        KaaAdmin.getDataSource().getSchemaVersionsByApplicationId(applicationId, new AsyncCallback<SchemaVersions>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final SchemaVersions schemaVersions) {
                KaaAdmin.getDataSource().getAefMaps(applicationId, new AsyncCallback<List<AefMapInfoDto>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<AefMapInfoDto> ecfs) {
                        GenerateSdkDialog dialog = new GenerateSdkDialog(applicationId, schemaVersions, ecfs);
                        dialog.center();
                        callback.onSuccess(dialog);
                        dialog.show();
                    }
                });
            }
        });
    }

    public GenerateSdkDialog(String applicationId, SchemaVersions schemaVersions, List<AefMapInfoDto> aefMaps) {
        super(false, true);

        this.applicationId = applicationId;
        this.aefMaps = aefMaps;

        setWidth("500px");

        setTitle(Utils.constants.generate_sdk());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table = new FlexTable();
        table.setCellSpacing(6);

        int row = 0;

        ValueChangeHandler<SchemaDto> schemaValueChangeHandler = new ValueChangeHandler<SchemaDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<SchemaDto> event) {
                fireChanged();
            }
        };

        Widget label = new Label(Utils.constants.configurationSchemaVersion());
        label.addStyleName(REQUIRED);
        configurationSchemaVersion = new SchemaListBox();
        configurationSchemaVersion.setWidth("80px");
        List<SchemaDto> confSchemaVersions = schemaVersions.getConfigurationSchemaVersions();
        configurationSchemaVersion.setValue(getMaxSchemaVersions(confSchemaVersions));
        configurationSchemaVersion.setAcceptableValues(confSchemaVersions);
        configurationSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, configurationSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.profileSchemaVersion());
        label.addStyleName(REQUIRED);
        profileSchemaVersion = new SchemaListBox();
        profileSchemaVersion.setWidth("80px");
        List<SchemaDto> pfSchemaVersions = schemaVersions.getProfileSchemaVersions();
        profileSchemaVersion.setValue(getMaxSchemaVersions(pfSchemaVersions));
        profileSchemaVersion.setAcceptableValues(pfSchemaVersions);
        profileSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, profileSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.notificationSchemaVersion());
        label.addStyleName(REQUIRED);
        notificationSchemaVersion = new SchemaListBox();
        notificationSchemaVersion.setWidth("80px");
        List<SchemaDto> notSchemaVersions = schemaVersions.getNotificationSchemaVersions();
        notificationSchemaVersion.setValue(getMaxSchemaVersions(notSchemaVersions));
        notificationSchemaVersion.setAcceptableValues(notSchemaVersions);
        notificationSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, notificationSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.logSchemaVersion());
        label.addStyleName(REQUIRED);
        logSchemaVersion = new SchemaListBox();
        logSchemaVersion.setWidth("80px");
        List<SchemaDto> logSchemaVersions = schemaVersions.getLogSchemaVersions();
        logSchemaVersion.setValue(getMaxSchemaVersions(logSchemaVersions));
        logSchemaVersion.setAcceptableValues(logSchemaVersions);
        logSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, logSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.targetPlatform());
        label.addStyleName(REQUIRED);

        Renderer<SdkPlatform> targetPlatformRenderer = new Renderer<SdkPlatform>() {
            @Override
            public String render(SdkPlatform object) {
                if (object != null) {
                    return Utils.constants.getString(object.getResourceKey());
                } else {
                    return "";
                }
            }

            @Override
            public void render(SdkPlatform object, Appendable appendable) throws IOException {
                appendable.append(render(object));
            }
        };

        targetPlatform = new ValueListBox<>(targetPlatformRenderer);
        targetPlatform.setWidth("80px");
        // Set default sdk platform
         targetPlatform.setValue(SdkPlatform.ANDROID);
        targetPlatform.setAcceptableValues(Arrays.asList(SdkPlatform.values()));
        targetPlatform.addValueChangeHandler(new ValueChangeHandler<SdkPlatform>() {
            @Override
            public void onValueChange(ValueChangeEvent<SdkPlatform> event) {
                fireChanged();
            }
        });

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, targetPlatform);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        FlexTable ecfsTable = new FlexTable();
        ecfsTable.setCellSpacing(6);

        availableAefMaps = new MultiAefMapListBox();
        selectedAefMaps = new MultiAefMapListBox();

        addAefMapButton = new Button(Utils.constants.add());
        removeAefMapButton = new Button(Utils.constants.remove());

        VerticalPanel availableEcfsPanel = new VerticalPanel();
        availableEcfsPanel.setSpacing(6);
        Label availableLabel = new Label(Utils.constants.available());
        availableEcfsPanel.add(availableLabel);
        availableEcfsPanel.add(availableAefMaps);

        VerticalPanel ecfButtonsPanel = new VerticalPanel();
        ecfButtonsPanel.setSpacing(6);
        ecfButtonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        ecfButtonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        ecfButtonsPanel.add(addAefMapButton);
        ecfButtonsPanel.add(removeAefMapButton);

        VerticalPanel selectedEcfsPanel = new VerticalPanel();
        selectedEcfsPanel.setSpacing(6);
        Label selectedLabel = new Label(Utils.constants.selected());
        selectedEcfsPanel.add(selectedLabel);
        selectedEcfsPanel.add(selectedAefMaps);

        ecfsTable.setWidget(0, 0, availableEcfsPanel);
        ecfsTable.setWidget(0, 1, ecfButtonsPanel);
        ecfsTable.setWidget(0, 2, selectedEcfsPanel);

        DisclosurePanel ecfsDisclosure = new DisclosurePanel(Utils.constants.ecfs());
        ecfsDisclosure.setAnimationEnabled(true);
        ecfsDisclosure.setContent(ecfsTable);

        aefMapComparator = new AefMapInfoDtoComparator();
        Collections.sort(aefMaps, aefMapComparator);

        availableAefMaps.setAcceptableValues(aefMaps);

        addAefMapButton.addStyleName(Utils.kaaAdminStyle.bAppButtonSmall());
        removeAefMapButton.addStyleName(Utils.kaaAdminStyle.bAppButtonSmall());

        addAefMapButton.setEnabled(false);
        removeAefMapButton.setEnabled(false);

        addAefMapButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addAefMap();
            }
        });

        removeAefMapButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeAefMap();
            }
        });

        availableAefMaps.setSize("150px", "100px");
        selectedAefMaps.setSize("150px", "100px");

        availableAefMaps.addValueChangeHandler(new ValueChangeHandler<List<AefMapInfoDto>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<AefMapInfoDto>> event) {
                updateAefMapButtons();
            }
        });

        selectedAefMaps.addValueChangeHandler(new ValueChangeHandler<List<AefMapInfoDto>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<AefMapInfoDto>> event) {
                updateAefMapButtons();
            }
        });

        dialogContents.add(table);
        dialogContents.add(ecfsDisclosure);

        generateSdkButton = new Button(Utils.constants.generate_sdk(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performGenerateSdk();
            }
        });

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButton(generateSdkButton);
        addButton(closeButton);

        generateSdkButton.setEnabled(false);
        fireChanged();
    }

    private void addAefMap() {
        List<AefMapInfoDto> selected = availableAefMaps.getValue();
        availableAefMaps.setValue(null, true);
        Map<String, AefMapInfoDto> idMap = new HashMap<String, AefMapInfoDto>();
        for (AefMapInfoDto aefMap : selected) {
            AefMapInfoDto previous = idMap.get(aefMap.getEcfId());
            if (previous == null || aefMap.getVersion() > previous.getVersion()) {
                idMap.put(aefMap.getEcfId(), aefMap);
            }
        }

        List<AefMapInfoDto> totalSelected = new ArrayList<>();
        totalSelected.addAll(idMap.values());
        totalSelected.addAll(selectedAefMaps.getValues());
        updateAefMapLists(totalSelected);
    }

    private void removeAefMap() {
        List<AefMapInfoDto> selected = selectedAefMaps.getValue();
        selectedAefMaps.setValue(null, true);
        List<AefMapInfoDto> totalSelected = new ArrayList<>();
        for (AefMapInfoDto aefMap : selectedAefMaps.getValues()) {
            if (!selected.contains(aefMap)) {
                totalSelected.add(aefMap);
            }
        }
        updateAefMapLists(totalSelected);
    }

    private void updateAefMapLists(List<AefMapInfoDto> totalSelected) {
        List<AefMapInfoDto> available = new ArrayList<>();
        Map<String, AefMapInfoDto> idMap = new HashMap<String, AefMapInfoDto>();
        for (AefMapInfoDto aefMap : totalSelected) {
            idMap.put(aefMap.getEcfId(), aefMap);
        }
        for (AefMapInfoDto aefMap : aefMaps) {
            if (!idMap.containsKey(aefMap.getEcfId())) {
                available.add(aefMap);
            }
        }
        Collections.sort(available, aefMapComparator);
        Collections.sort(totalSelected, aefMapComparator);
        availableAefMaps.setAcceptableValues(available);
        selectedAefMaps.setAcceptableValues(totalSelected);
    }

    class AefMapInfoDtoComparator implements Comparator<AefMapInfoDto> {
        @Override
        public int compare(AefMapInfoDto o1, AefMapInfoDto o2) {
            int result = o1.getEcfName().compareTo(o2.getEcfName());
            if (result == 0) {
                result = o1.getVersion() - o2.getVersion();
            }
            return result;
        }
    }

    private void updateAefMapButtons() {
        boolean availableSelected = availableAefMaps.getValue() != null && !availableAefMaps.getValue().isEmpty();
        boolean selectedSelected = selectedAefMaps.getValue() != null && !selectedAefMaps.getValue().isEmpty();
        addAefMapButton.setEnabled(availableSelected);
        removeAefMapButton.setEnabled(selectedSelected);
    }

    private void fireChanged() {
        boolean valid = validate();
        generateSdkButton.setEnabled(valid);
    }

    private void performGenerateSdk() {
        SchemaDto configurationSchema = configurationSchemaVersion.getValue();
        SchemaDto profileSchema = profileSchemaVersion.getValue();
        SchemaDto notificationSchema = notificationSchemaVersion.getValue();
        SchemaDto logSchema = logSchemaVersion.getValue();
        SdkPlatform targetPlatformVal = targetPlatform.getValue();

        List<String> aefMapIds = new ArrayList<>();
        List<AefMapInfoDto> aefMaps = selectedAefMaps.getValues();
        if (aefMaps != null) {
            for (AefMapInfoDto aefMap : aefMaps) {
                aefMapIds.add(aefMap.getAefMapId());
            }
        }

        KaaAdmin.getDataSource().getSdk(applicationId, configurationSchema.getMajorVersion(), profileSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(), targetPlatformVal, aefMapIds, logSchema.getMajorVersion(), new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Utils.handleException(caught, GenerateSdkDialog.this);
                    }

                    @Override
                    public void onSuccess(String key) {
                        clearError();
                        ServletHelper.downloadSdk(key);
                    }
                });
    }

    private boolean validate() {
        boolean result = configurationSchemaVersion.getValue() != null;
        result &= profileSchemaVersion.getValue() != null;
        result &= notificationSchemaVersion.getValue() != null;
        result &= logSchemaVersion.getValue() != null;
        result &= targetPlatform.getValue() != null;
        return result;
    }

    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }

}
