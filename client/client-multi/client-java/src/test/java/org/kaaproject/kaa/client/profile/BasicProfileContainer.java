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

package org.kaaproject.kaa.client.profile;

import java.io.IOException;

import org.kaaproject.kaa.client.profile.AbstractProfileContainer;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;

public class BasicProfileContainer extends AbstractProfileContainer<BasicEndpointProfile> {
    private BasicEndpointProfile profile = new BasicEndpointProfile();

    public BasicProfileContainer() {
        profile.setProfileBody("profileBody1");
    }

    @Override
    public BasicEndpointProfile getProfile() {
        return profile;
    }

    public void setProfileBody(String body) throws IOException {
        profile.setProfileBody(body);
        this.updateProfile();
    }

    @Override
    protected Class<BasicEndpointProfile> getProfileClass() {
        return BasicEndpointProfile.class;
    }
}
