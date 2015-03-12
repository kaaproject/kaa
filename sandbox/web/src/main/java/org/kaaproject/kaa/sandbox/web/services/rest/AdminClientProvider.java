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

package org.kaaproject.kaa.sandbox.web.services.rest;

import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.springframework.beans.factory.InitializingBean;

public class AdminClientProvider implements InitializingBean {

    //@Value("#{properties[admin_host]}")
    private String adminHost = "10.2.3.93";

    //@Value("#{properties[admin_port]}")
    private int adminPort = 8080;

    public AdminClientProvider() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public AdminClient getClient() {
        return new AdminClient(adminHost, adminPort);
    }

}
