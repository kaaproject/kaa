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

package org.kaaproject.kaa.server.admin.client.mvp.view.navigation;

import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class NavigationViewImpl extends Composite implements NavigationView {

    interface NavigationViewImplUiBinder extends UiBinder<Widget, NavigationViewImpl> { }
    private static NavigationViewImplUiBinder uiBinder = GWT.create(NavigationViewImplUiBinder.class);

    @UiField(provided = true) final CellTree menuTree;
    @UiField(provided = true) final KaaAdminStyle kaaAdminStyle;

    private NavigationTreeViewModel treeModel;

    public NavigationViewImpl() {
        treeModel = new NavigationTreeViewModel();
        menuTree = new CellTree(treeModel, null);
        kaaAdminStyle = Utils.kaaAdminStyle;

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
    }

    @Override
    public SingleSelectionModel<TreePlace> getSelectionModel() {
        return treeModel.getSelectionModel();
    }

    @Override
    public CellTree getMenuTree() {
        return menuTree;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        treeModel.setEventBus(eventBus);
    }

}
