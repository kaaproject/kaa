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
import java.util.List;

import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.event.shared.EventBus;

public class EcfsPlace extends TreePlace {

    private EcfPlaceDataProvider dataProvider;

    public EcfsPlace() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof EcfsPlace);
    }

    @Prefix(value = "ecfs")
    public static class Tokenizer implements PlaceTokenizer<EcfsPlace> {

        @Override
        public EcfsPlace getPlace(String token) {
            return new EcfsPlace();
        }

        @Override
        public String getToken(EcfsPlace place) {
            PlaceParams.clear();
            return PlaceParams.generateToken();
        }
    }

    @Override
    public String getName() {
        return Utils.constants.ecfs();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        if (dataProvider == null) {
            dataProvider = new EcfPlaceDataProvider(eventBus);
        }
        return dataProvider;
    }

    class EcfPlaceDataProvider extends TreePlaceDataProvider implements DataEventHandler {

        EcfPlaceDataProvider(EventBus eventBus) {
            eventBus.addHandler(DataEvent.getType(), this);
        }

        @Override
        public void onDataChanged(DataEvent event) {
            if (event.checkClass(EventClassFamilyDto.class)) {
                refresh();
            }
        }

        @Override
        protected void loadData(
                final LoadCallback callback,
                final HasData<TreePlace> display) {
            KaaAdmin.getDataSource().loadEcfs(new AsyncCallback<List<EventClassFamilyDto>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);

                }
                @Override
                public void onSuccess(List<EventClassFamilyDto> result) {

                    callback.onSuccess(toPlaces(result), display);
                }
            });
        }

        private List<TreePlace> toPlaces(List<EventClassFamilyDto> ecfs) {
            List<TreePlace> result = new ArrayList<TreePlace>();
            for (EventClassFamilyDto ecf : ecfs) {
                EcfPlace place = new EcfPlace(ecf.getId());
                place.setName(ecf.getName());
                result.add(place);
            }
            return result;
        }


    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return null;
    }

}
