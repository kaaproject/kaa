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

public class EcfSchemaPlace extends TreePlace {

    private String ecfId;
    private int version;

    public EcfSchemaPlace(String ecfId, int version) {
        this.ecfId = ecfId;
        this.version = version;
    }

    public String getEcfId() {
        return ecfId;
    }

    public int getVersion() {
        return version;
    }

    @Prefix(value = "ecfSchema")
    public static class Tokenizer implements PlaceTokenizer<EcfSchemaPlace>, PlaceConstants {

        @Override
        public EcfSchemaPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new EcfSchemaPlace(PlaceParams.getParam(ECF_ID), PlaceParams.getIntParam(VERSION));
        }

        @Override
        public String getToken(EcfSchemaPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(ECF_ID, place.getEcfId());
            PlaceParams.putIntParam(VERSION, place.getVersion());
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
        EcfSchemaPlace other = (EcfSchemaPlace) obj;
        if (ecfId == null) {
            if (other.ecfId != null) {
                return false;
            }
        } else if (!ecfId.equals(other.ecfId)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new EcfPlace(ecfId);
    }

}
