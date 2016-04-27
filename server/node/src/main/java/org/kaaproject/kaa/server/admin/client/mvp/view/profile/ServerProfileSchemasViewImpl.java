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

package org.kaaproject.kaa.server.admin.client.mvp.view.profile;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class ServerProfileSchemasViewImpl extends BaseListViewImpl<ServerProfileSchemaDto> {

    public ServerProfileSchemasViewImpl() {
        super(true);
    }

    @Override
    protected AbstractGrid<ServerProfileSchemaDto, String> createGrid() {
        return new BaseCtlSchemasGrid<ServerProfileSchemaDto>();
    }

    @Override
    protected String titleString() {
        return Utils.constants.serverProfileSchemas();
    }

    @Override
    protected String addButtonString() {
        return Utils.constants.addNewSchema();
    }
}
