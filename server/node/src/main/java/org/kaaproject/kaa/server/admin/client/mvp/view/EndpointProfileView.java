/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ServerProfileSchemasInfoListBox;

import java.util.List;

public interface EndpointProfileView extends BaseDetailsView {

    SizedTextBox getKeyHash();

    SizedTextBox getUserID();
    SizedTextBox getUserExternalID();
    List<Widget> getUserInfoList();

    Anchor getEndpointProfSchemaName();
    RecordPanel getEndpointProfForm();

    Anchor getServerProfSchemaName();
    RecordPanel getServerProfForm();

    Button getAddButton();
    Button getDeleteButton();
    Button getEditButton();
    Button getSaveProfileButton();

    RecordFieldWidget getServerProfRecord();
    ServerProfileSchemasInfoListBox getServerSchemasListBox();

    AbstractGrid<EndpointGroupDto, String> getGroupsGrid();
    TopicGrid getTopicsGrid();
}
