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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseRecordView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public abstract class BaseRecordViewImpl<T extends AbstractStructureDto,V> extends BaseDetailsViewImpl 
                        implements BaseRecordView<T,V>, ValueChangeHandler<VersionDto>  {

    protected AbstractRecordPanel<T,V> recordPanel;

    public BaseRecordViewImpl(boolean create) {
        super(create);
        getSaveButtonWidget().setVisible(false);
        getCancelButtonWidget().setVisible(false);
        getBackButtonPanelWidget().setVisible(true);
    }

    @Override
    protected void initDetailsTable() {

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "500px");
        
        int row = initDetailsTableImpl();

        recordPanel = createRecordPanel();
        detailsTable.setWidget(++row, 0, recordPanel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);

    }
    
    @Override
    public void onValueChange(ValueChangeEvent<VersionDto> event) {
        recordPanel.fireSchemaSelected();
    }
    
    protected abstract int initDetailsTableImpl();

    protected abstract AbstractRecordPanel<T,V> createRecordPanel();

    @Override
    protected void resetImpl() {
        recordPanel.reset();
    }

    @Override
    protected boolean validate() {
        return false;
    }

    @Override
    public AbstractRecordPanel<T,V> getRecordPanel() {
        return recordPanel;
    }


}
