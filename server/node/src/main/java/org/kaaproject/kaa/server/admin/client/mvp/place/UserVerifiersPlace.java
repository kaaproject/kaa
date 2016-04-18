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

import com.google.gwt.place.shared.Prefix;

public class UserVerifiersPlace extends AbstractPluginsPlace {

    public UserVerifiersPlace(String applicationId) {
        super(applicationId);
    }

    @Override
    public String getName() {
        return Utils.constants.userVerifiers();
    }

    @Prefix(value = "userVerifiers")
    public static class Tokenizer extends AbstractPluginsPlace.Tokenizer<UserVerifiersPlace> {

        @Override
        protected UserVerifiersPlace getPlaceImpl(String applicationId) {
            return new UserVerifiersPlace(applicationId);
        }
    }

}
