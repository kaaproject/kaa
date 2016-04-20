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

public class TenantCtlSchemasPlace extends TreePlace {

    public TenantCtlSchemasPlace() {
    }

    @Prefix(value = "tenantCtlSchemas")
    public static class Tokenizer implements PlaceTokenizer<TenantCtlSchemasPlace> {

        @Override
        public TenantCtlSchemasPlace getPlace(String token) {
            return new TenantCtlSchemasPlace();
        }

        @Override
        public String getToken(TenantCtlSchemasPlace place) {
            PlaceParams.clear();
            return PlaceParams.generateToken();
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof TenantCtlSchemasPlace);
    }

    @Override
    public String getName() {
        return Utils.constants.tenantCtl();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return null;
    }

}
