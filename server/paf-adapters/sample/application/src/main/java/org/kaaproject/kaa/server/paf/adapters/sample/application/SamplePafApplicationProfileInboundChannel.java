/**
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

package org.kaaproject.kaa.server.paf.adapters.sample.application;

import java.util.Collections;
import java.util.Set;

import org.kaaproject.kaa.server.common.paf.shared.application.AbstractPafApplicationProfileInboundChannel;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.impl.StringApplicationProfileRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePafApplicationProfileInboundChannel extends AbstractPafApplicationProfileInboundChannel {
    
    private static final Logger LOG = LoggerFactory.getLogger(SamplePafApplicationProfileInboundChannel.class);

    private ApplicationProfileRoute applicationProfileRoute;

    public void setApplicationProfilePath(String path) {
        this.applicationProfileRoute = new StringApplicationProfileRoute(path);
    }

    @Override
    protected Set<ApplicationProfileRoute> getApplicationProfileRoutes() {
        return Collections.singleton(this.applicationProfileRoute);
    }


}
