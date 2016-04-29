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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;

public class ApplicationsPlace extends TreePlace {

    private ApplicationPlaceDataProvider dataProvider;

    public ApplicationsPlace() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof ApplicationsPlace);
    }

    @Prefix(value = "apps")
    public static class Tokenizer implements PlaceTokenizer<ApplicationsPlace> {

        @Override
        public ApplicationsPlace getPlace(String token) {
            return new ApplicationsPlace();
        }

        @Override
        public String getToken(ApplicationsPlace place) {
            PlaceParams.clear();
            return PlaceParams.generateToken();
        }
    }

    @Override
    public String getName() {
        return Utils.constants.applications();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        if (dataProvider == null) {
            dataProvider = new ApplicationPlaceDataProvider(eventBus);
        }
        return dataProvider;
    }

    class ApplicationPlaceDataProvider extends TreePlaceDataProvider implements DataEventHandler {

        ApplicationPlaceDataProvider(EventBus eventBus) {
            eventBus.addHandler(DataEvent.getType(), this);
        }

        @Override
        public void onDataChanged(DataEvent event) {
            if (event.checkClass(ApplicationDto.class)) {
                refresh();
            }
        }

        @Override
        protected void loadData(
                final LoadCallback callback,
                final HasData<TreePlace> display) {
            KaaAdmin.getDataSource().loadApplications(new AsyncCallback<List<ApplicationDto>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);

                }
                @Override
                public void onSuccess(List<ApplicationDto> result) {

                    callback.onSuccess(toPlaces(result), display);
                }
            });
        }

        private List<TreePlace> toPlaces(List<ApplicationDto> applications) {
            List<TreePlace> result = new ArrayList<TreePlace>();
            for (ApplicationDto application : applications) {
                ApplicationPlace place = new ApplicationPlace(application.getId());
                place.setApplicationName(application.getName());
                result.add(place);
            }
            Collections.sort(result, new Comparator<TreePlace>() {
                @Override
                public int compare(TreePlace o1, TreePlace o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            return result;
        }


    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return null;
    }

}
