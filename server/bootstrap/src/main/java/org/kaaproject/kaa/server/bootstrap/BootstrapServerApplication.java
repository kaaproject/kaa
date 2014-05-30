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

package org.kaaproject.kaa.server.bootstrap;

import java.nio.charset.Charset;

import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * The Class BootstrapServerApplication.
 * Implements main() to start Bootstrap Service.
 */
public class BootstrapServerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapServerApplication.class);

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        LOG.info("Bootstrap Server application starting... {}", Charset.defaultCharset().name());

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bootstrapContext.xml");
        final BootstrapInitializationService bootstrapInitializationService
            = (BootstrapInitializationService) ctx.getBean("bootstrapInitializationService");

        bootstrapInitializationService.start();
        ctx.close();

        LOG.info("Bootstrap Server application stopped");
    }
}
