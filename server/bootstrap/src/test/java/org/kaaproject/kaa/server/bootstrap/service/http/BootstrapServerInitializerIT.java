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

package org.kaaproject.kaa.server.bootstrap.service.http;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapServerInitializer;
import org.kaaproject.kaa.server.common.http.server.CommandFactory;
import org.kaaproject.kaa.server.common.http.server.CommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/bootstrapTestContext.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class BootstrapServerInitializerIT {

    @Autowired
    protected BootstrapConfig bootstrapConfig;

    @Ignore
    @Test
    public void testBootstrapServerInitializerInit() throws Exception {
        EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(bootstrapConfig.getExecutorThreadSize());
        BootstrapServerInitializer bootstrapServerInitializer = new BootstrapServerInitializer(bootstrapConfig, eventExecutorGroup);
        Assert.assertNotNull(bootstrapServerInitializer);
        bootstrapServerInitializer.init();
        CommandProcessor resolveCommandProcessor = CommandFactory.getCommandProcessor("/DOMAIN/" + CommonBSConstants.BOOTSTRAP_RESOLVE_COMMAND);
        Assert.assertNotNull(resolveCommandProcessor);
    }
}
