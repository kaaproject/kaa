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

package org.kaaproject.kaa.server.admin.client.mvp.view.grid;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.HasId;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

public abstract class AbstractKaaGrid<T,K> extends AbstractGrid<T,K> {

    private static final int DEFAULT_PAGE_SIZE = 14;
    
    public AbstractKaaGrid(Unit unit) {
        super(unit, DEFAULT_PAGE_SIZE);
    }
    
    public AbstractKaaGrid(Style.Unit unit, boolean enableActions) {
        super(unit, enableActions, false, DEFAULT_PAGE_SIZE);
    }
    
    public AbstractKaaGrid(Style.Unit unit, boolean enableActions, int defaultPageSize) {
        super(unit, enableActions, defaultPageSize);
    }

    public AbstractKaaGrid(Style.Unit unit, boolean enableActions, boolean embedded) {
        super(unit, enableActions, embedded, DEFAULT_PAGE_SIZE, true);
    }

    public AbstractKaaGrid(Style.Unit unit, boolean enableActions, boolean embedded, boolean init) {
        super(unit, enableActions, embedded, DEFAULT_PAGE_SIZE, init);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected K getObjectId(T value) {
        if (value != null && value instanceof HasId) {
            return (K) ((HasId)value).getId();
        } else {
            return null;
        }
    }

}
