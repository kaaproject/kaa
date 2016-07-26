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


import com.google.gwt.place.shared.PlaceTokenizer;
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class EventClassPlace extends AbstractSchemaPlace {

    private String ecfId;
    private int version;

    public EventClassPlace(String ecfId, int version) {
        super("", "");
        this.ecfId = ecfId;
        this.version = version;
    }

    @Override
    public String getName() {
        return Utils.constants.schemas();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return null;
    }

    public String getEcfId() {
        return ecfId;
    }

    public static abstract class Tokenizer implements PlaceTokenizer<EventClassPlace>, PlaceConstants {

        @Override
        public EventClassPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return getPlaceImpl(PlaceParams.getParam(ECF_ID));
        }

        protected abstract EventClassPlace getPlaceImpl(String ecfId);

        @Override
        public String getToken(EventClassPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(ECF_ID, place.getEcfId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventClassPlace that = (EventClassPlace) o;

        if (version != that.version) return false;
        return ecfId != null ? ecfId.equals(that.ecfId) : that.ecfId == null;

    }

    @Override
    public int hashCode() {
        int result = ecfId != null ? ecfId.hashCode() : 0;
        result = 31 * result + version;
        return result;
    }
}
