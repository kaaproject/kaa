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

package org.kaaproject.kaa.server.admin.client.mvp.view;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.input.SizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.FlumeBalancingTypeListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AppenderInfoListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.SchemaListBox;
import org.kaaproject.kaa.server.admin.shared.form.RecordField;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;

public interface LogAppenderView extends BaseDetailsView {

    SchemaListBox getSchemaVersions();

    SizedTextBox getName();

    CheckBox getStatus();

    AppenderInfoListBox getAppenderInfo();

    SizedTextArea getDescription();

    SizedTextBox getCreatedDateTime();

    Button getActivate();

    SizedTextBox getCreatedUsername();

    void showFlumeCongurationFields(FlumeAppenderParametersDto flumeAppenderParametersDto);

    FlumeBalancingTypeListBox getFlumeBalancingType();

    FlexTable getHostTable();

    void setMetadataListBox(List<LogHeaderStructureDto> header);

    List<LogHeaderStructureDto> getHeader();

    void showFileCongurationFields();

    String getPublicKey();

    void setPublicKey(String publicKey);

    void hideFileCongurationFields();
    
    void showCustomConfigurationFields();
    
    RecordField getConfiguration();
    
    void setConfiguration(RecordField configuration);
    
    void hideCustomConfigurationFields();
}
