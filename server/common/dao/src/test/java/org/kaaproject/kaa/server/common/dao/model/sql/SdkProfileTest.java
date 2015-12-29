/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.model.sql;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.common.dao.service.SdkTokenGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SdkProfileTest {
    private final int SDK_TOKEN_LENGTH = 28;

    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(SdkProfile.class).verify();
    }

    @Test
    public void constructorTest() {
        SdkProfileDto sdkProfileDto1 = generateSdkProfileDto("1234", "token1234");
        SdkProfileDto sdkProfileDto2 = generateSdkProfileDto("1234", "token1234");
        SdkProfileDto sdkProfileDto3 = generateSdkProfileDto("1235", "token1235");
        SdkProfile sdkProfile1 = new SdkProfile(sdkProfileDto1);
        SdkProfile sdkProfile2 = new SdkProfile(sdkProfileDto2);
        SdkProfile sdkProfile3 = new SdkProfile(sdkProfileDto3);
        Assert.assertEquals(sdkProfile1.toDto(), sdkProfile2.toDto());
        Assert.assertEquals(sdkProfile1.getToken(), sdkProfile2.getToken());
        Assert.assertNotEquals(sdkProfile1.toDto(), sdkProfile3.toDto());
        Assert.assertNotEquals(sdkProfile1.getToken(), sdkProfile3.getToken());
        Assert.assertEquals(SDK_TOKEN_LENGTH, sdkProfile1.getToken().length());
    }

    private SdkProfileDto generateSdkProfileDto(String appId, String appToken) {
        List<String> aefMapIdsList = new ArrayList<>(Arrays.asList("firstId", "secondId", "thirdId"));
        SdkProfileDto sdkProfileDto = new SdkProfileDto(appId, 2, 3, 4, 5, aefMapIdsList, "someVerifierToken", appToken,
                "devuser", 100000L, "someName");
        SdkTokenGenerator.generateSdkToken(sdkProfileDto);
        return sdkProfileDto;
    }
}
