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

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AefMapPlace extends AefMapsPlace {

    protected String aefMapId;

    public AefMapPlace(String applicationId, String aefMapId) {
        super(applicationId);
        this.aefMapId = aefMapId;
    }

    public String getAefMapId() {
        return aefMapId;
    }

    @Override
    public String getName() {
        return Utils.constants.aefMap();
    }

    @Prefix(value = "aefMap")
    public static class Tokenizer implements PlaceTokenizer<AefMapPlace>, PlaceConstants {

        @Override
        public AefMapPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new AefMapPlace(PlaceParams.getParam(APPLICATION_ID), PlaceParams.getParam(AEF_MAP_ID));
        }

        @Override
        public String getToken(AefMapPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(AEF_MAP_ID, place.getAefMapId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AefMapPlace other = (AefMapPlace) obj;
        if (aefMapId == null) {
            if (other.aefMapId != null) {
                return false;
            }
        } else if (!aefMapId.equals(other.aefMapId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new AefMapsPlace(applicationId);
    }
}
