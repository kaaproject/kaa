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

package org.kaaproject.kaa.server.admin.client.mvp.view;


import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.topic.TopicGrid;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public interface EndpointProfileView extends BaseDetailsView {

    SizedTextBox getKeyHash();

    SizedTextBox getUserID();
    SizedTextBox getUserExternalID();
    List<Widget> getUserInfoList();

    Anchor getEndpointProfSchemaName();
    RecordPanel getEndpointProfForm();
    HasClickHandlers getDownloadEndpointProfileJsonButton();

    Anchor getServerProfSchemaName();
    RecordPanel getServerProfForm();
    HasClickHandlers getDownloadServerProfileJsonButton();
    HasClickHandlers getEditServerProfileButton();

    Anchor getSdkAnchor();
    AbstractGrid<EndpointGroupDto, String> getGroupsGrid();
    TopicGrid getTopicsGrid();
}
