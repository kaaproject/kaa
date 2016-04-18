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

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class SdkProfilesPlace extends TreePlace {

    protected String applicationId;

    public String getApplicationId() {
        return applicationId;
    }

    public SdkProfilesPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    @Prefix(value = "sdkProfiles")
    public static class Tokenizer implements PlaceTokenizer<SdkProfilesPlace> {

        @Override
        public SdkProfilesPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new SdkProfilesPlace(PlaceParams.getParam(PlaceConstants.APPLICATION_ID));
        }

        @Override
        public String getToken(SdkProfilesPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(PlaceConstants.APPLICATION_ID, place.getApplicationId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public String getName() {
        return Utils.constants.sdkProfiles();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new ApplicationPlace(applicationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        SdkProfilesPlace other = (SdkProfilesPlace) o;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        return true;
    }
}
