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

/**
 * 
 */
package org.kaaproject.kaa.server.bootstrap.service.initialization;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/bootstrapTestContext.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class DefaultBootstrapInitializationServiceIT {
    
    
    class TestLauncher extends Thread {
        DefaultBootstrapInitializationService service;
        
        
        public void run() {
            if (service != null) {
                try {
                    service.start();
                } catch(Throwable twh) {
                    fail(twh.toString());
                }
            }
        }
        
        public void shutdown() {
            if (service != null) {
                try {
                    service.stop();
                } catch(Throwable twh) {
                    twh.printStackTrace();
                    fail(twh.toString());
                }
            }
        }
    }
    
    
    private static final String SERVER_HOME_DIR = "/tmp";
    static {
        System.setProperty("server_home_dir", SERVER_HOME_DIR);
    }
    
    @Autowired
    public DefaultBootstrapInitializationService dbiService;
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.initialization.DefaultBootstrapInitializationService#start()}.
     */
    @Test
    public void start() {
        assertNotNull(dbiService);
        TestLauncher l = new TestLauncher();
        assertNotNull(l);
        l.service = dbiService;
        l.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        assertNotNull(dbiService.getKeyStoreService());
        assertNotNull(dbiService.getBootstrapThriftService());
        assertNotNull(dbiService.getBootstrapNode());
        assertNotNull(l);
        assertNotNull(l.service);
        l.shutdown();
        
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.initialization.DefaultBootstrapInitializationService#stop()}.
     */
    @Test
    public void stop() {
        assertNotNull(dbiService);
        TestLauncher l = new TestLauncher();
        assertNotNull(l);
        l.service = dbiService;
        l.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        assertNotNull(l);
        assertNotNull(l.service);
        l.shutdown();
        assertNotNull(dbiService);
        assertNull(dbiService.getBootstrapNode());
        assertNull(dbiService.getOperationsServerListService());
        assertNull(dbiService.getHttpService());
    }
    
}
