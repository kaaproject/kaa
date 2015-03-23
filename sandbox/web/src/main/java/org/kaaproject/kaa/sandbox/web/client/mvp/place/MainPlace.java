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
package org.kaaproject.kaa.sandbox.web.client.mvp.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class MainPlace extends Place {

    @Prefix(value = "main")
    public static class Tokenizer implements PlaceTokenizer<MainPlace> {

        @Override
        public MainPlace getPlace(String token) {
            return new MainPlace();
        }

        @Override
        public String getToken(MainPlace place) {
            return "";
        }
    }

    @Override
    public boolean equals(Object obj) {
       if (obj != null && obj instanceof MainPlace) {
           return true;
       } else {
           return false;
       }
    }
    
    
    
}
