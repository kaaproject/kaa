/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TenantPlace extends UserPlace {

    private String tenantName;

    public TenantPlace(String userId) {
        super(userId);
    }

    public void setTenantName(String name) {
        this.tenantName = name;
    }

    @Prefix(value = "ten")
    public static class Tokenizer implements PlaceTokenizer<TenantPlace>, PlaceConstants {

        @Override
        public TenantPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new TenantPlace(PlaceParams.getParam(USER_ID));
        }

        @Override
        public String getToken(TenantPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(USER_ID, place.getUserId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public String getName() {
        return tenantName;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new TenantsPlace();
    }


}
