/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.activity.grid;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;

public abstract class AbstractDataProvider<T> extends AsyncDataProvider<T>{

    protected List<T> data;

    private boolean loaded = false;

    private LoadCallback callback;

    private MultiSelectionModel<T> selectionModel;

    public AbstractDataProvider(MultiSelectionModel<T> selectionModel, AsyncCallback<List<T>> asyncCallback)
    {
        this.selectionModel = selectionModel;
        callback = new LoadCallback(asyncCallback);
    }

    public void addRow(T row) {
        data.add(row);
        updateRowCount(data.size(), true);
        updateRowData(data.size()-1, data.subList(data.size()-1, data.size()));
    }

    public void updateRow(T row) {
        int index = data.indexOf(row);
        updateRowData(index, data.subList(index, index+1));
    }

    public List<T> getData() {
        return data;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void reload(HasData<T> display) {
        this.loaded = false;
        loadData(callback, display);
    }

    @Override
    protected void onRangeChanged(final HasData<T> display) {
      if (!loaded) {
          loadData(callback, display);
      }
      else {
          updateData(display);
      }
    }

    protected abstract void loadData(final LoadCallback callback, final HasData<T> display);

    private void updateData (HasData<T> display) {
        selectionModel.clear();
        int start = display.getVisibleRange().getStart();
        int end = start + display.getVisibleRange().getLength();
        end = end >= data.size() ? data.size() : end;
        List<T> sub = data.subList(start, end);
        updateRowData(start, sub);
    }

    public class LoadCallback {

        private final AsyncCallback<List<T>> asyncCallback;

        public LoadCallback(AsyncCallback<List<T>> asyncCallback) {
            this.asyncCallback = asyncCallback;
        }

        public void onFailure(Throwable caught) {
            GWT.log("AbstractDataProvider.LoadCallback.onFailure(caught):", caught);
            asyncCallback.onFailure(caught);
        }

        public void onSuccess(List<T> result, final HasData<T> display) {
            data = result;
            updateRowCount(data.size(), true);
            updateData(display);
            loaded = true;
            asyncCallback.onSuccess(result);
        }
    }
}
