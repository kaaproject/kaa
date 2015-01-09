/*
 * Copyright 2014 CyberVision, Inc.
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

public class LogAppendersPlace extends TreePlace {

    protected String applicationId;

    public LogAppendersPlace(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String getName() {
        return Utils.constants.logAppenders();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new ApplicationPlace(applicationId);
    }

    @Prefix(value = "logAppends")
    public static class Tokenizer implements PlaceTokenizer<LogAppendersPlace>, PlaceConstants {

        @Override
        public LogAppendersPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new LogAppendersPlace(PlaceParams.getParam(APPLICATION_ID));
        }

        @Override
        public String getToken(LogAppendersPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            return PlaceParams.generateToken();
        }
    }

}
