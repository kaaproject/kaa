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

package org.kaaproject.kaa.server.common.dao.model.sql;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.common.dao.service.SdkTokenGenerator;
import static org.kaaproject.kaa.common.Constants.SDK_TOKEN_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SdkProfileTest {
    

    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(SdkProfile.class).usingGetClass().verify();
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
        Assert.assertEquals(SDK_TOKEN_SIZE, sdkProfile1.getToken().length());
        
        for(int i =0; i < 100000; i++){
            SdkProfileDto tmp = generateSdkProfileDto("1235" + i, "token1235" + i);
            Assert.assertEquals(SDK_TOKEN_SIZE, tmp.getToken().length(), SDK_TOKEN_SIZE); 
        }
    }

    private SdkProfileDto generateSdkProfileDto(String appId, String appToken) {
        List<String> aefMapIdsList = new ArrayList<>(Arrays.asList("firstId", "secondId", "thirdId"));
        SdkProfileDto sdkProfileDto = new SdkProfileDto(appId, 2, 3, 4, 5, aefMapIdsList, "someVerifierToken", appToken,
                "devuser", 100000L, "someName");
        SdkTokenGenerator.generateSdkToken(sdkProfileDto);
        return sdkProfileDto;
    }

    /**
     * Tests that SDK tokens are URL-safe and thus can be used as a part of a file name.
     */
    @Test
    public void testForURLSafeToken() {
        List<String> aefMapIdsList = Collections.singletonList("290");
        SdkProfileDto sdkProfileDto = new SdkProfileDto("113", 1, 0, 1, 1, aefMapIdsList,
                "someVerifierToken", "15643220456970528206", "devuser", 100000L, "");
        SdkTokenGenerator.generateSdkToken(sdkProfileDto);
        Assert.assertFalse(sdkProfileDto.getToken().contains("/"));
    }
}
