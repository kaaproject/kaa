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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.TreeViewModel.DefaultNodeInfo;
import com.google.gwt.view.client.TreeViewModel.NodeInfo;

public abstract class TreePlace extends Place implements PlaceConstants {

    public abstract String getName();

    public abstract boolean isLeaf();

    public abstract TreePlace createDefaultPreviousPlace();

    private TreePlace previousPlace;
    
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        return null;
    }

    public TreePlace getPreviousPlace() {
        if (previousPlace == null) {
            previousPlace = createDefaultPreviousPlace();
        }
        return previousPlace;
    }

    public void setPreviousPlace(TreePlace previousPlace) {
        this.previousPlace = previousPlace;
    }

    public NodeInfo<?> getNodeInfo(SelectionModel<TreePlace> selectionModel, EventBus eventBus) {
        PlaceCell cell = new PlaceCell();
        return new DefaultNodeInfo<TreePlace>(getDataProvider(eventBus), cell, selectionModel, null);
    }

    public static class PlaceCell extends AbstractCell<TreePlace> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context,
                TreePlace value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.appendEscaped(value.getName());
              }
        }
    }

    public static abstract class TreePlaceDataProvider extends AsyncDataProvider<TreePlace> {

        protected List<TreePlace> data;
        private LoadCallback callback;
        private HasData<TreePlace> display;

        public TreePlaceDataProvider() {
            callback = new LoadCallback();
        }

        @Override
        protected void onRangeChanged(HasData<TreePlace> display) {
            loadData(callback, display);
        }

        @Override
        public void addDataDisplay(final HasData<TreePlace> display) {
            this.display = display;
            super.addDataDisplay(display);
        }

        public void refresh() {
            if (display != null) {
                loadData(callback, display);
            }
        }

        protected abstract void loadData(final LoadCallback callback, final HasData<TreePlace> display);

        public class LoadCallback {


            public void onFailure(Throwable caught) {
                GWT.log("TreePlaceDataProvider.LoadCallback.onFailure(caught):", caught);
            }

            public void onSuccess(List<TreePlace> result, final HasData<TreePlace> display) {
                data = result;
                updateRowData(0, data);
            }
        }
    }
}
