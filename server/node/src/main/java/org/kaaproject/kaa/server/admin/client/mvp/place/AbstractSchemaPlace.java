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

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class AbstractSchemaPlace extends SchemasPlace {

    protected String schemaId;

    public AbstractSchemaPlace(String applicationId, String schemaId) {
        super(applicationId);
        this.schemaId = schemaId;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public static abstract class Tokenizer<P extends AbstractSchemaPlace> implements PlaceTokenizer<P>, PlaceConstants {

        @Override
        public P getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return getPlaceImpl(PlaceParams.getParam(APPLICATION_ID), PlaceParams.getParam(SCHEMA_ID));
        }

        protected abstract P getPlaceImpl(String applicationId, String schemaId);

        @Override
        public String getToken(P place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(SCHEMA_ID, place.getSchemaId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlaceDataProvider getDataProvider(EventBus eventBus) {
        return null;
    }
}
