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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

public class ConfigFormStructView extends BaseStructView<ConfigurationRecordFormDto, RecordField> {

    public ConfigFormStructView(HasErrorMessage hasErrorMessage) {
        super(hasErrorMessage);
    }

    @Override
    protected HasValue<RecordField> createBody(HasErrorMessage hasErrorMessage) {
        RecordPanel field = new RecordPanel(Utils.constants.configurationBody(), hasErrorMessage, false, false);
        field.getRecordWidget().setForceNavigation(true);
        field.setPreferredHeightPx(200);
        return field;
    }

    @Override
    protected boolean hasLabel() {
        return false;
    }

    @Override
    protected void setBodyReadOnly(boolean readOnly) {
        ((RecordPanel)body).setReadOnly(readOnly);
    }

    @Override
    protected void setBodyValue(ConfigurationRecordFormDto struct) {
        body.setValue(struct.getConfigurationRecord());
    }

    @Override
    protected HandlerRegistration addBodyChangeHandler() {
        return body.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                fireChanged();
            }
        });
    }

    @Override
    protected boolean validateBody() {
        return ((RecordPanel)body).validate();
    }

    @Override
    protected void onShown() {
        ((RecordPanel)body).getRecordWidget().onShown();
    }

}
