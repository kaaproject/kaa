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
package org.kaaproject.kaa.server.operations.service.config;

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.server.common.http.server.Config;

/**
 * @author Andrey Panasenko
 *
 */
abstract public class NettyHttpServiceChannelConfig extends Config implements ServiceChannelConfig {

    private boolean channelEnabled;
    private OperationsServerConfig operationServerConfig;
    
    abstract public ChannelType getChannelType();

    /**
     * @return the channelEnabled
     */
    public boolean isChannelEnabled() {
        return channelEnabled;
    }

    /**
     * @param channelEnabled the channelEnabled to set
     */
    public void setChannelEnabled(boolean channelEnabled) {
        this.channelEnabled = channelEnabled;
    }

    /**
     * @return the operationServerConfig
     */
    public OperationsServerConfig getOperationServerConfig() {
        return operationServerConfig;
    }

    /**
     * @param operationServerConfig the operationServerConfig to set
     */
    public void setOperationServerConfig(OperationsServerConfig operationServerConfig) {
        this.operationServerConfig = operationServerConfig;
    }
    
}
