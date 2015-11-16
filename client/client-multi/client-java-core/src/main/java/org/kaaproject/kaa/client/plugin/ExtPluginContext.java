/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.client.plugin;

import java.util.concurrent.ExecutorService;

import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.channel.KaaChannelManager;

public final class ExtPluginContext implements PluginContext {

    private final int extId;
    private final KaaClientPlatformContext kaaContext;
    private final KaaChannelManager cm;

    public ExtPluginContext(int extId, KaaChannelManager cm, KaaClientPlatformContext kaaContext) {
        super();
        this.extId = extId;
        this.kaaContext = kaaContext;
        this.cm = cm;
    }

    @Override
    public ExecutorService getApiExecutor() {
        return kaaContext.getExecutorContext().getApiExecutor();
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return kaaContext.getExecutorContext().getCallbackExecutor();
    }

    @Override
    public void sync() {
        cm.sync(extId);
    }

}
