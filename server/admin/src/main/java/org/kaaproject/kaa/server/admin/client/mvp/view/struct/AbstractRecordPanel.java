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

package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TabPanel;

public abstract class AbstractRecordPanel<T extends AbstractStructureDto, V> extends TabPanel {

    private BaseStructView<T,V> activePanel;
    private BaseStructView<T,V> inactivePanel;

    public AbstractRecordPanel(HasErrorMessage hasErrorMessage) {
        activePanel = createStructView(hasErrorMessage);
        inactivePanel = createStructView(hasErrorMessage);
        activePanel.setBodyLabelText(bodyLabelText());
        inactivePanel.setBodyLabelText(bodyLabelText());
        add(activePanel, Utils.constants.active());
        add(inactivePanel, Utils.constants.draft());
    }
    
    protected abstract BaseStructView<T,V> createStructView(HasErrorMessage hasErrorMessage);

    public void reset() {
        activePanel.reset();
        inactivePanel.reset();
    }

    public void setData(StructureRecordDto<T> record) {
        if (record.hasActive()) {
            activePanel.setData(record.getActiveStructureDto());
        }
        if (record.hasDraft()) {
            inactivePanel.setData(record.getInactiveStructureDto());
        }
    }

    public void openActive() {
        this.selectTab(0);
    }

    public void openDraft() {
        this.selectTab(1);
    }

    public void setSchemaSelected(boolean selected) {
        inactivePanel.setSchemaSelected(selected);
    }

    public HasValue<String> getDescription() {
        return inactivePanel.getDescription();
    }

    public HasValue<V> getBody() {
        return inactivePanel.getBody();
    }

    public HasClickHandlers getSaveButton() {
        return inactivePanel.getSaveButton();
    }

    public HasClickHandlers getActivateButton() {
        return inactivePanel.getActivateButton();
    }

    public HasClickHandlers getDeactivateButton() {
        return activePanel.getDeactivateButton();
    }

    public void setActiveReadOnly() {
        activePanel.setReadOnly();
    }

    public void setInactiveReadOnly() {
        inactivePanel.setReadOnly();
    }

    protected abstract String bodyLabelText();

    public abstract void setReadOnly();

}
