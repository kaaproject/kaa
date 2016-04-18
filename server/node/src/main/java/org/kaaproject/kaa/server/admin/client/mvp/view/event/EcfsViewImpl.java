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

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseListViewImpl;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;

public class EcfsViewImpl extends BaseListViewImpl<EventClassFamilyDto> {

        public EcfsViewImpl() {
            super(true);
        }

        @Override
        protected AbstractGrid<EventClassFamilyDto, String> createGrid() {
            return new EcfsGrid(Unit.PX);
        }

        @Override
        protected String titleString() {
            return Utils.constants.ecfs();
        }

        @Override
        protected String addButtonString() {
            return Utils.constants.addNewEcf();
        }
}

