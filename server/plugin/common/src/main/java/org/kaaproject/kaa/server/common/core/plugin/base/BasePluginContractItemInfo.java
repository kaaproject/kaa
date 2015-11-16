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
package org.kaaproject.kaa.server.common.core.plugin.base;

import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;

public class BasePluginContractItemInfo implements PluginContractItemInfo {

    private final byte[] data;
    private final String inMsgSchema;
    private final String outMsgSchema;

    private BasePluginContractItemInfo(byte[] data, String inMsgSchema, String outMsgSchema) {
        super();
        this.data = data;
        this.inMsgSchema = inMsgSchema;
        this.outMsgSchema = outMsgSchema;
    }
    
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private byte[] data;
        private String inMsgSchema;
        private String outMsgSchema;

        private Builder() {
            super();
        }

        public Builder withData(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder withInMsgSchema(String schema) {
            this.inMsgSchema = schema;
            return this;
        }

        public Builder withOutMsgSchema(String schema) {
            this.outMsgSchema = schema;
            return this;
        }

        public BasePluginContractItemInfo build() {
            validate();
            return new BasePluginContractItemInfo(data, inMsgSchema, outMsgSchema);
        }

        private void validate() {
            // TODO implement
        }

    }

    @Override
    public byte[] getConfigurationData() {
        return data;
    }

    @Override
    public String getInMessageSchema() {
        return inMsgSchema;
    }

    @Override
    public String getOutMessageSchema() {
        return outMsgSchema;
    }

}
