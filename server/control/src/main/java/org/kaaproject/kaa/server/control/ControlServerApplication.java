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

package org.kaaproject.kaa.server.control;

import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The Class ControlServerApplication.
 */
public class ControlServerApplication {

    private static final String DEFAULT_APPLICATION_CONTEXT_XML = "controlContext.xml";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ControlServerApplication.class);

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        LOG.info("Control Server Application starting...");
        String applicationContextXml = DEFAULT_APPLICATION_CONTEXT_XML;
        if (args.length > 0) {
            applicationContextXml = args[0];
        }
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                applicationContextXml);
        final ControlService controlService = ctx.getBean("controlService",
                ControlService.class);
        controlService.start();
        ctx.close();
        LOG.info("Control Server Application stopped.");
    }
}
