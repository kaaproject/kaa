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

package org.kaaproject.kaa.server.admin.client.mvp.data;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EcfVersionsDataProvider
    extends AbstractDataProvider<EventClassFamilyVersionDto, Integer> {

  private List<EventClassFamilyVersionDto> schemas = new ArrayList<>();

  public EcfVersionsDataProvider(AbstractGrid<EventClassFamilyVersionDto, Integer> dataGrid,
                                 HasErrorMessage hasErrorMessage) {
    super(dataGrid, hasErrorMessage);
  }

  /**
   * Sets the event class family version list.
   *
   * @param schemas schema list
   */
  public void setSchemas(List<EventClassFamilyVersionDto> schemas) {
    this.schemas.clear();
    if (schemas != null) {
      this.schemas.addAll(schemas);
      Collections.sort(schemas, new Comparator<EventClassFamilyVersionDto>() {
        @Override
        public int compare(EventClassFamilyVersionDto o1,
                           EventClassFamilyVersionDto o2) {
          return o1.getVersion() - o2.getVersion();
        }
      });
    }
  }

  @Override
  protected void loadData(final LoadCallback callback) {
    callback.onSuccess(schemas);
  }

}

