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

package org.kaaproject.kaa.server.admin.client.mvp.activity.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.ColumnFilterEvent;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public abstract class AbstractDataProvider<T,K> extends AsyncDataProvider<T> implements ColumnSortEvent.Handler, ColumnFilterEvent.Handler {

    protected List<T> data;
    
    private Map<K,T> dataMap = new HashMap<>();

    private boolean loaded = false;

    private LoadCallback callback;

    private AbstractGrid<T,K> dataGrid;
    
    private DataFilter<T> dataFilter;
    
    public AbstractDataProvider(AbstractGrid<T,K> dataGrid, HasErrorMessage hasErrorMessage)
    {
        this(dataGrid, hasErrorMessage, true);
    }

    public AbstractDataProvider(AbstractGrid<T,K> dataGrid, HasErrorMessage hasErrorMessage, boolean addDisplay)
    {
        this.dataGrid = dataGrid;
        callback = new LoadCallback(hasErrorMessage);
        dataGrid.getDataGrid().addColumnSortHandler(this);
        dataGrid.addColumnFilterEventHandler(this);
        if (addDisplay) {
            addDataDisplay(dataGrid.getDataGrid());
        }
    }
    
    protected void addDataDisplay() {
        addDataDisplay(dataGrid.getDataGrid());
    }
    
    @SuppressWarnings("unchecked")
    private K getObjectId(T value) {
        return (K) dataGrid.getDataGrid().getKeyProvider().getKey(value);
    }
    
    public void setDataFilter(DataFilter<T> dataFilter) {
        this.dataFilter = dataFilter;
    }

    public void addRow(T row) {
        data.add(row);
        dataMap.put(getObjectId(row), row);
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
    
    public T getRowData(K key) {
        return dataMap.get(key);
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void reload() {
        this.loaded = false;
        loadData(callback);
    }

    @Override
    protected void onRangeChanged(final HasData<T> display) {
      if (!loaded) {
          loadData(callback);
      } else {
          updateData();
      }
    }

    protected abstract void loadData(final LoadCallback callback);
    
    @Override
    public void onColumnSort(ColumnSortEvent event) {
        updateData();
    }
    
    @Override
    public void onColumnFilter(ColumnFilterEvent event) {
        updateData();
    }    

    public void updateData () {
        List<T> filteredData = dataGrid.filter(data);
        if (dataFilter != null) {
            List<T> newFilteredData = new ArrayList<>();
            for (T value : filteredData) {
                if (dataFilter.accept(value)) {
                    newFilteredData.add(value);
                }
            }
            filteredData = newFilteredData;
        }
        updateRowCount(filteredData.size(), true);
        ColumnSortList sortList = dataGrid.getDataGrid().getColumnSortList();
        Column<?,?> column = (sortList == null || sortList.size() == 0) ? null
                : sortList.get(0).getColumn();
        boolean isSortAscending = (sortList == null || sortList.size() == 0) ? false
                : sortList.get(0).isAscending();
        if (column != null) {
            dataGrid.sort(filteredData, column, isSortAscending);
        }        
        updateRowData(0, filteredData);
    }

    public class LoadCallback {

        private HasErrorMessage hasErrorMessage;

        public LoadCallback(HasErrorMessage hasErrorMessage) {
            this.hasErrorMessage = hasErrorMessage;
        }

        public void onFailure(Throwable caught) {
            GWT.log("AbstractDataProvider.LoadCallback.onFailure(caught):", caught);
            Utils.handleException(caught, hasErrorMessage);
        }

        public void onSuccess(List<T> result) {
            dataGrid.getSelectionModel().clear();
            data = result;
            if (data == null) {
                data = Collections.<T>emptyList();
            }
            dataMap.clear();
            for (T row : data) {
                dataMap.put(getObjectId(row), row);
            }            
            updateData();
            loaded = true;
            hasErrorMessage.clearError();
        }
    }
    
}
