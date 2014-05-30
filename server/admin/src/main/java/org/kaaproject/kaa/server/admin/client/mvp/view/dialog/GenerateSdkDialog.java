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

import java.io.IOException;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AlertPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.client.servlet.ServletHelper;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.dto.SchemaVersions;
import org.kaaproject.kaa.server.admin.shared.dto.SdkPlatform;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GenerateSdkDialog extends KaaDialog {

    private AlertPanel errorPanel;

    private SchemaListBox configurationSchemaVersion;
    private SchemaListBox profileSchemaVersion;
    private SchemaListBox notificationSchemaVersion;
    private ValueListBox<SdkPlatform> targetPlatform;

    private String applicationId;

    private Button generateSdkButton;

    public static void showGenerateSdkDialog(final String applicationId, final AsyncCallback<GenerateSdkDialog> callback) {
        KaaAdmin.getDataSource().getSchemaVersionsByApplicationId(applicationId, new AsyncCallback<SchemaVersions>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(SchemaVersions result) {
                GenerateSdkDialog dialog = new GenerateSdkDialog(applicationId, result);
                dialog.center();
                callback.onSuccess(dialog);
                dialog.show();
            }
        });
    }

    public GenerateSdkDialog(String applicationId, SchemaVersions schemaVersions) {
        super(false, true);

        this.applicationId = applicationId;

        setWidth("500px");

        setTitle(Utils.constants.generate_sdk());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row=0;

        ValueChangeHandler<SchemaDto> schemaValueChangeHandler = new ValueChangeHandler<SchemaDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<SchemaDto> event) {
                fireChanged();
            }
        };

        Widget label = new Label(Utils.constants.configurationSchemaVersion());
        label.addStyleName("required");
        configurationSchemaVersion = new SchemaListBox();
        configurationSchemaVersion.setWidth("80px");
        configurationSchemaVersion.setAcceptableValues(schemaVersions.getConfigurationSchemaVersions());
        configurationSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, configurationSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.profileSchemaVersion());
        label.addStyleName("required");
        profileSchemaVersion = new SchemaListBox();
        profileSchemaVersion.setWidth("80px");
        profileSchemaVersion.setAcceptableValues(schemaVersions.getProfileSchemaVersions());
        profileSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, profileSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;


        label = new Label(Utils.constants.notificationSchemaVersion());
        label.addStyleName("required");
        notificationSchemaVersion = new SchemaListBox();
        notificationSchemaVersion.setWidth("80px");
        notificationSchemaVersion.setAcceptableValues(schemaVersions.getNotificationSchemaVersions());
        notificationSchemaVersion.addValueChangeHandler(schemaValueChangeHandler);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, notificationSchemaVersion);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        row++;

        label = new Label(Utils.constants.targetPlatform());
        label.addStyleName("required");

        Renderer<SdkPlatform> targetPlatformRenderer = new Renderer<SdkPlatform>() {
            @Override
            public String render(SdkPlatform object) {
                if (object != null) {
                    return Utils.constants.getString(object.getResourceKey());
                }
                else {
                    return "";
                }
            }

            @Override
            public void render(SdkPlatform object, Appendable appendable)
                    throws IOException {
                appendable.append(render(object));
            }
        };

        targetPlatform = new ValueListBox<>(targetPlatformRenderer);
        targetPlatform.setWidth("80px");
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

        dialogContents.add(table);

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
    }

    private void fireChanged() {
        boolean valid = validate();
        generateSdkButton.setEnabled(valid);
    }

    private void performGenerateSdk () {
        SchemaDto configurationSchema = configurationSchemaVersion.getValue();
        SchemaDto profileSchema = profileSchemaVersion.getValue();
        SchemaDto notificationSchema = notificationSchemaVersion.getValue();
        SdkPlatform targetPlatformVal = targetPlatform.getValue();
        KaaAdmin.getDataSource().getSdk(applicationId, configurationSchema.getMajorVersion(),
                profileSchema.getMajorVersion(), notificationSchema.getMajorVersion(), targetPlatformVal,
                new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        setError(Utils.getErrorMessage(caught));
                    }

                    @Override
                    public void onSuccess(String key) {
                        ServletHelper.downloadSdk(key);
                    }
        });
    }

    private boolean validate() {
        boolean result = configurationSchemaVersion.getValue() != null;
        result &= profileSchemaVersion.getValue() != null;
        result &= notificationSchemaVersion.getValue() != null;
        result &= targetPlatform.getValue() != null;
        return result;
    }

    private void setError(String error) {
        if (error!= null) {
            errorPanel.setText(error);
            errorPanel.setVisible(true);
        }
        else {
            errorPanel.setText("");
            errorPanel.setVisible(false);
        }
    }

}
