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

package org.kaaproject.kaa.server.admin.client.mvp.view.event;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EcfVersionView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EcfVersionViewImpl extends BaseListViewImpl<EventClassDto> implements EcfVersionView {

    @UiField
    public final ImageTextButton addSchemaButton;

    public EcfVersionViewImpl(boolean editable) {
        super(editable);
        this.addSchemaButton = new ImageTextButton(Utils.resources.plus(), addButtonEventClassString());
        supportPanel.add(addSchemaButton);
    }

    @Override
    protected AbstractGrid<EventClassDto, String> createGrid() {
        return new EcfVersionGrid();
    }

    @Override
    protected String titleString() {
        return Utils.constants.eventClasses();
    }

    @Override
    protected String addButtonString() {
        return "Save ECF version";
    }

    private String addButtonEventClassString() {
        return "Add event class";
    }

    @Override
    public Button addButtonEventClass() {
        return addSchemaButton;
    }

}
