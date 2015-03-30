package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

import java.util.List;


@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class UserVerifierServiceImplTets extends AbstractTest {

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findUserVerifiersByAppIdTest(){
        UserVerifierDto verifierDto1 = generateUserVerifier(null, null);
        UserVerifierDto verifierDto2 = generateUserVerifier(null, null);
        List<UserVerifierDto> found = verifierService.findUserVerifiersByAppId(verifierDto1.getApplicationId());
        Assert.assertEquals(2, found.size());
    }

    @Test
    public void findUserVerifiersByAppIdAndVerifierTokenTest(){
        UserVerifierDto verifierDto = generateUserVerifier(null, null);
        UserVerifierDto found = verifierService.findUserVerifiersByAppIdAndVerifierToken(verifierDto.getApplicationId(), verifierDto.getVerifierToken());
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void findUserVerifierByIdTest(){
        UserVerifierDto verifierDto = generateUserVerifier(null, null);
        UserVerifierDto found = verifierService.findUserVerifierById(verifierDto.getId());
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void saveUserVerifierTest(){
        UserVerifierDto verifierDto = generateUserVerifier(null, null);
        UserVerifierDto found = verifierService.saveUserVerifier(verifierDto);
        Assert.assertEquals(verifierDto, found);
    }

    @Test
    public void saveUserVerifierNullVerifierDtoTest(){
        UserVerifierDto found = verifierService.saveUserVerifier(null);
        Assert.assertNull(found);
    }

    @Test
    public void removeUserVerifierByIdTest(){
        UserVerifierDto verifierDto = generateUserVerifier(null, null);
        verifierService.removeUserVerifierById(verifierDto.getId());
        UserVerifierDto found = verifierService.findUserVerifierById(verifierDto.getId());
        Assert.assertNull(found);
    }
}
