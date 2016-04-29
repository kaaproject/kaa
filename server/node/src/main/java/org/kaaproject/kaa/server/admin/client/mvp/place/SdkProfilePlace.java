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
import org.kaaproject.kaa.server.admin.client.util.Utils;

public class SdkProfilePlace extends SdkProfilesPlace {

    protected String sdkProfileId;

    public SdkProfilePlace(String applicationId, String sdkProfileId) {
        super(applicationId);
        this.sdkProfileId = sdkProfileId;
    }

    public String getSdkProfileId() {
        return sdkProfileId;
    }


    @Prefix(value = "sdkProfileId")
    public static class Tokenizer implements PlaceTokenizer<SdkProfilePlace> {

        @Override
        public SdkProfilePlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new SdkProfilePlace(PlaceParams.getParam(PlaceConstants.APPLICATION_ID),
                    PlaceParams.getParam(PlaceConstants.SDK_ID));
        }

        @Override
        public String getToken(SdkProfilePlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(PlaceConstants.APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(PlaceConstants.SDK_ID, place.getSdkProfileId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SdkProfilePlace that = (SdkProfilePlace) o;

        return !(sdkProfileId != null ? !sdkProfileId.equals(that.sdkProfileId) : that.sdkProfileId != null);

    }

    @Override
    public String getName() {
        return Utils.constants.sdkProfileDetails();
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new SdkProfilesPlace(applicationId);
    }
}
