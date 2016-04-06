package org.kaaproject.kaa.server.node.service.credentials;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class DefaultCredentialsServiceLocatorTest {
    
    @Autowired
    private DefaultCredentialsServiceLocator credentialsServiceLocator;
        
    @Test
    public void testCredentailsServiceLoad(){
        List<String> serviceNames = credentialsServiceLocator.getCredentialsServiceNames();
        Assert.assertEquals(2, serviceNames.size());
        Assert.assertEquals("Internal", serviceNames.get(0));
        Assert.assertEquals("Trustful", serviceNames.get(1));
    }

}
