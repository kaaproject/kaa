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


import com.google.gwt.user.cellview.client.DataGrid;

import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.schema.BaseCtlSchemasGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EcfVersionGrid extends BaseCtlSchemasGrid<EventClassDto> {

  public EcfVersionGrid() {
  }

  @Override
  protected float constructColumnsImpl(DataGrid<EventClassDto> table) {
    float prefWidth = super.constructColumnsImpl(table);
    prefWidth += constructStringColumn(table,
        Utils.constants.classType(),
        new StringValueProvider<EventClassDto>() {
          @Override
          public String getValue(EventClassDto item) {
            return item.getType().toString();
          }
        }, 80);

    return prefWidth;
  }

}
