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

package org.kaaproject.kaa.server.admin.client.mvp.view.ctl;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.CtlSchemasView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;

public class CtlSchemasViewImpl extends BaseListViewImpl<CTLSchemaMetaInfoDto> implements CtlSchemasView {

        private CheckBox showHigherScopeCheckBox;
    
        public CtlSchemasViewImpl() {
            super(true);
            if (displayShowHigherLevelScopeCheckBox()) {
                showHigherScopeCheckBox = new CheckBox(Utils.constants.displayHigherScopes());
                showHigherScopeCheckBox.addStyleName(Utils.kaaAdminStyle.bAppContentTitle());
                Element.as(showHigherScopeCheckBox.getElement().getChild(0)).
                    getStyle().setMarginRight(10, Unit.PX);
                showHigherScopeCheckBox.setValue(defaultShowHigherLevelScopes());
                appendToolbarWidget(showHigherScopeCheckBox);
            }
        }

        @Override
        protected AbstractGrid<CTLSchemaMetaInfoDto, String> createGrid() {
            return new CtlGrid(Unit.PX);
        }

        @Override
        protected String titleString() {
            return Utils.constants.ctl();
        }

        @Override
        protected String addButtonString() {
            return Utils.constants.addNewCtl();
        }
        
        @Override
        public boolean displayShowHigherLevelScopeCheckBox() {
            return false;
        }

        @Override
        public HasValue<Boolean> getShowHigherScopeCheckBox() {
            return showHigherScopeCheckBox;
        }
        
        protected boolean defaultShowHigherLevelScopes() {
            return true;
        }
}

