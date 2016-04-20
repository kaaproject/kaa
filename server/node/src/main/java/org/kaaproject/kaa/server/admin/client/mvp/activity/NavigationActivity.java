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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.NavigationView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.SelectionChangeEvent;

public class NavigationActivity extends AbstractActivity implements NavigationView.Presenter {

    private final ClientFactory clientFactory;
    private final NavigationView navigationView;

    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

    public NavigationActivity(ClientFactory clientFactory, EventBus eventBus) {
        this.clientFactory = clientFactory;
        this.navigationView = clientFactory.getNavigationView();
        this.navigationView.setPresenter(this);
        this.navigationView.setEventBus(eventBus);
    }

    private TreePlace pendingPlace;

    public void onPlaceChanged(TreePlace place) {
        TreePlace selected = navigationView.getSelectionModel().getSelectedObject();
        if (!place.equals(selected)) {
            pendingPlace = place;
            selectPlace(place);
        }
    }

    private void selectPlace(TreePlace place) {
        TreeNode node = navigationView.getMenuTree().getRootTreeNode();
        if (openNode(node, place)) {
            navigationView.getSelectionModel().setSelected(place, true);
            pendingPlace = null;
        } else {
            navigationView.getSelectionModel().clear();
        }
    }

    private boolean openNode(TreeNode node, TreePlace place) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (node.getChildValue(i).equals(place)) {
              //node.setChildOpen(i, true, true);
              return true;
            } else if (!node.isChildLeaf(i)) {
                boolean wasOpen = node.isChildOpen(i);
                TreeNode child = node.setChildOpen(i, true);
                if (child != null && openNode(child, place)) {
                    return true;
                } else if (!wasOpen) {
                    node.setChildOpen(i, false);
                }
            }
        }
        return false;
    }

    private void refreshTree() {
        TreeNode node = navigationView.getMenuTree().getRootTreeNode();
        refreshTree(node);
    }

    private void refreshTree(TreeNode node) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (!node.isChildLeaf(i) && node.isChildOpen(i)) {
                node.setChildOpen(i, false);
                TreeNode child = node.setChildOpen(i, true);
                if (child != null) {
                    refreshTree(child);
                }
            }
        }
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus eventBus) {
        containerWidget.setWidget(navigationView.asWidget());
        registrations.add(navigationView.getSelectionModel().addSelectionChangeHandler(
            new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                   TreePlace place = navigationView.getSelectionModel().getSelectedObject();
                   if (place != null) {
                       goTo(place);

                       TreePlace current = (TreePlace) clientFactory.getPlaceController().getWhere();
                       if (!current.equals(place)) {
                           navigationView.getSelectionModel().setSelected(current, true);
                       }
                   }
                }
            }
        ));
        registrations.add(eventBus.addHandler(DataEvent.getType(), new DataEventHandler() {

            @Override
            public void onDataChanged(DataEvent event) {
                if (event.refreshTree()) {
                    refreshTree();
                    if (pendingPlace != null) {
                        selectPlace(pendingPlace);
                    }
                }
            }
        }));
    }

   @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

}
