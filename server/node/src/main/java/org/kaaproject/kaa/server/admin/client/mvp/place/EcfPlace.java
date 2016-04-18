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
import com.google.gwt.place.shared.Prefix;

public class EcfPlace extends TreePlace {

    private String ecfId;
    private String name;

    public EcfPlace(String ecfId) {
        this.ecfId = ecfId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEcfId() {
        return ecfId;
    }

    @Prefix(value = "ecf")
    public static class Tokenizer implements PlaceTokenizer<EcfPlace>, PlaceConstants {

        @Override
        public EcfPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new EcfPlace(PlaceParams.getParam(ECF_ID));
        }

        @Override
        public String getToken(EcfPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(ECF_ID, place.getEcfId());
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
        EcfPlace other = (EcfPlace) obj;
        if (ecfId == null) {
            if (other.ecfId != null) {
                return false;
            }
        } else if (!ecfId.equals(other.ecfId)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new EcfsPlace();
    }

}
