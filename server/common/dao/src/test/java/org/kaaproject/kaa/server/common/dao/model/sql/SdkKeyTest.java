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
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SdkKeyTest {
    private final int SDK_TOKEN_LENGTH = 28;

    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(SdkKey.class).verify();
    }

    @Test
    public void constructorTest() {
        SdkPropertiesDto sdkPropertiesDto1 = generateSdkPropertiesDto("1234");
        SdkPropertiesDto sdkPropertiesDto2 = generateSdkPropertiesDto("1234");
        SdkPropertiesDto sdkPropertiesDto3 = generateSdkPropertiesDto("1235");
        SdkKey sdkKey1 = new SdkKey(sdkPropertiesDto1);
        SdkKey sdkKey2 = new SdkKey(sdkPropertiesDto2);
        SdkKey sdkKey3 = new SdkKey(sdkPropertiesDto3);
        Assert.assertEquals(sdkKey1.toDto(), sdkKey2.toDto());
        Assert.assertEquals(sdkKey1.getToken(), sdkKey2.getToken());
        Assert.assertNotEquals(sdkKey1.toDto(), sdkKey3.toDto());
        Assert.assertNotEquals(sdkKey1.getToken(), sdkKey3.getToken());
        Assert.assertEquals(SDK_TOKEN_LENGTH, sdkKey1.getToken().length());
    }

    private SdkPropertiesDto generateSdkPropertiesDto(String appId) {
        List<String> aefMapIdsList = new ArrayList<>(Arrays.asList("firstId", "secondId", "thirdId"));
        return new SdkPropertiesDto(appId, 2, 3, 4, 5, SdkPlatform.ANDROID, aefMapIdsList, "someVerifierToken", "someApplicationToken",
                "devuser", 100000L, "someName");
    }
}
