package org.kaaproject.kaa.server.control.service.modularization;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.control.service.exception.KaaPluginLoadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
public class KaaPluginLoadServiceImplTest {

    @Autowired
    private KaaPluginLoadService kaaPluginLoadService;

    @Autowired
    private PluginService pluginService;

    @Test
    public void loadTest() throws KaaPluginLoadException {
        kaaPluginLoadService.load();
        Assert.assertEquals(1, pluginService.findAllPlugins().size());
    }
}
