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

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.TreePlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseDetailsView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public abstract class AbstractDetailsActivity<T, V extends BaseDetailsView, P extends TreePlace> extends AbstractActivity implements BaseDetailsView.Presenter {

    protected final ClientFactory clientFactory;
    protected final String entityId;
    protected P place;

    protected T entity;
    protected boolean create;

    protected V detailsView;
    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    protected boolean canceled = false;

    public AbstractDetailsActivity(P place, ClientFactory clientFactory) {
        this.place = place;
        this.entityId = getEntityId(place);
        this.clientFactory = clientFactory;
        this.create = isEmpty(entityId);
    }

    protected abstract String getEntityId(P place);

    protected abstract V getView(boolean create);

    protected abstract T newEntity();

    protected abstract void onEntityRetrieved();

    protected abstract void onSave();

    protected abstract void getEntity(String id, AsyncCallback<T> callback);

    protected abstract void editEntity(T entity, AsyncCallback<T> callback);


    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        detailsView = getView(create);
        detailsView.setPresenter(this);
        bind(eventBus);
        containerWidget.setWidget(detailsView.asWidget());
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
    
    protected void reload() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
          }
          registrations.clear();
          bind(null);
    }

    protected void bind(final EventBus eventBus) {
        registrations.add(detailsView.getSaveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doSave(eventBus);
            }
        }));

        final Place previousPlace = place.getPreviousPlace();
        if (previousPlace != null) {
            detailsView.setBackEnabled(true);
            detailsView.setCancelEnabled(true);
            registrations.add(detailsView.getCancelButton().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    canceled = true;
                    goTo(place.getPreviousPlace());
                }
            }));

            registrations.add(detailsView.getBackButton().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    goTo(place.getPreviousPlace());
                }
            }));
        } else {
            detailsView.setBackEnabled(false);
            detailsView.setCancelEnabled(false);
        }

        detailsView.reset();

        if (create) {
            entity = newEntity();
            onEntityRetrieved();
        } else {
            loadEntity();
        }
    }

    protected void loadEntity() {
        getEntity(entityId, new BusyAsyncCallback<T>() {
            @Override
            public void onFailureImpl(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }

            @Override
            public void onSuccessImpl(T result) {
                entity = result;
                onEntityRetrieved();
            }
        });
    }

    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
                new BusyAsyncCallback<T>() {
                    public void onSuccessImpl(T result) {
                        if (place.getPreviousPlace() != null) {
                            goTo(place.getPreviousPlace());
                        }
                    }

                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, detailsView);
                    }
        });
    }

    @Override
    public String mayStop() {
        if (detailsView.hasChanged() && !canceled) {
            return Utils.messages.detailsMayStopMessage();
        } else {
            return super.mayStop();
        }
    }

}
