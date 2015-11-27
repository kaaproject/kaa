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

package org.kaaproject.kaa.server.admin.client.mvp.view.profile;

import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseSchemaViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class ServerProfileSchemaViewImpl extends BaseSchemaViewImpl {

    public ServerProfileSchemaViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addProfileSchema();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.serverProfileSchema();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.profileSchemaDetails();
    }

    @Override
    protected void initDetailsTable() {
        super.initDetailsTable();
        /*
            Making invisible Endpoint count column because it is redundant.
            see BaseSchemaViewImpl#initDetailsTable() for details.
         */
        detailsTable.getCellFormatter().setVisible(3, 0, false);
        detailsTable.getCellFormatter().setVisible(3, 1, false);
    }
}
