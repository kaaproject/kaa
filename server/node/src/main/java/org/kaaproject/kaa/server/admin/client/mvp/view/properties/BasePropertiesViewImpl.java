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

package org.kaaproject.kaa.server.admin.client.mvp.view.properties;

import org.kaaproject.kaa.server.admin.client.mvp.view.BasePropertiesView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;

public abstract class BasePropertiesViewImpl extends BaseDetailsViewImpl implements BasePropertiesView, ValueChangeHandler<RecordField> {

    private RecordFieldWidget configuration;
    
    public BasePropertiesViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        configuration = new RecordFieldWidget(new AvroWidgetsConfig.Builder().createConfig());
        configuration.addValueChangeHandler(this);
        getFooter().add(configuration);
    }

    @Override
    protected void resetImpl() {
        configuration.setValue(null);
    }

    @Override
    protected boolean validate() {
        return configuration.validate();
    }
    
    @Override
    public HasValue<RecordField> getConfiguration() {
        return configuration;
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }

}
