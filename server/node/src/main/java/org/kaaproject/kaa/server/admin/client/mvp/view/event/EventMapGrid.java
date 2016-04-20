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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ValueSelectionCell;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class EventMapGrid extends AbstractKaaGrid<ApplicationEventMapDto, String> {

    public EventMapGrid(boolean editable) {
        super(Unit.PX, editable, true);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<ApplicationEventMapDto> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table,
                Utils.constants.fqn(),
                new StringValueProvider<ApplicationEventMapDto>() {
                    @Override
                    public String getValue(ApplicationEventMapDto item) {
                        return item.getFqn();
                    }
                }, 160);
        
        
        final Renderer<ApplicationEventAction> actionRenderer = new Renderer<ApplicationEventAction>() {

            @Override
            public String render(ApplicationEventAction action) {
                return Utils.constants.getString(action.name().toLowerCase());
            }

            @Override
            public void render(ApplicationEventAction action,
                    Appendable appendable) throws IOException {
                appendable.append(render(action));                        
            }
        };
        
        if (enableActions) {
            List<ApplicationEventAction> actions = new ArrayList<>();
            for (ApplicationEventAction action : ApplicationEventAction.values()) {
                actions.add(action);
            }
            
            ValueSelectionCell<ApplicationEventAction> actionsCell = new ValueSelectionCell<>(actions, 
                    actionRenderer);
            
            Column<ApplicationEventMapDto, ApplicationEventAction> actionColumn = new Column<ApplicationEventMapDto, ApplicationEventAction>(actionsCell) {
                @Override
                public ApplicationEventAction getValue(ApplicationEventMapDto object) {
                  return object.getAction();
                }
            };
              
              Header<SafeHtml> actionHeader = new SafeHtmlHeader(
                      SafeHtmlUtils.fromSafeConstant(Utils.constants.action()));
              
              table.addColumn(actionColumn, actionHeader);
              actionColumn.setFieldUpdater(new FieldUpdater<ApplicationEventMapDto, ApplicationEventAction>() {
                @Override
                public void update(int index, ApplicationEventMapDto object, ApplicationEventAction value) {
                    object.setAction(value);
                }
              });
              table.setColumnWidth(actionColumn, 80, Unit.PX);
              prefWidth += 80;
        } else {
            prefWidth += constructStringColumn(table,
                    Utils.constants.action(),
                    new StringValueProvider<ApplicationEventMapDto>() {
                        @Override
                        public String getValue(ApplicationEventMapDto item) {
                            return actionRenderer.render(item.getAction());
                        }
                    }, 80);
        }

        return prefWidth;
    }
    
    @Override
    protected String getObjectId(ApplicationEventMapDto value) {
        return value.getEventClassId();
    }

}
