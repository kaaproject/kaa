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

package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

import java.util.List;


@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class UserVerifierServiceImplTest extends AbstractTest {

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findUserVerifiersByAppIdTest() {
        UserVerifierDto verifierDto = generateUserVerifierDto(null, null);
        List<UserVerifierDto> found = verifierService.findUserVerifiersByAppId(verifierDto.getApplicationId());
        Assert.assertEquals(2, found.size());
    }

    @Test
    public void findUserVerifiersByAppIdAndVerifierTokenTest() {
        UserVerifierDto verifierDto = generateUserVerifierDto(null, null);
        UserVerifierDto found = verifierService.findUserVerifiersByAppIdAndVerifierToken(verifierDto.getApplicationId(), verifierDto.getVerifierToken());
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void findUserVerifierByIdTest() {
        UserVerifierDto verifierDto = generateUserVerifierDto(null, null);
        UserVerifierDto found = verifierService.findUserVerifierById(verifierDto.getId());
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void saveUserVerifierTest() {
        UserVerifierDto verifierDto = generateUserVerifierDto(null, null);
        UserVerifierDto found = verifierService.saveUserVerifier(verifierDto);
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void saveUserVerifierNullVerifierDtoTest() {
        UserVerifierDto found = verifierService.saveUserVerifier(null);
        Assert.assertNull(found);
    }

    @Test
    public void removeUserVerifierByIdTest() {
        UserVerifierDto verifierDto = generateUserVerifierDto(null, null);
        verifierService.removeUserVerifierById(verifierDto.getId());
        UserVerifierDto found = verifierService.findUserVerifierById(verifierDto.getId());
        Assert.assertNull(found);
    }
}
