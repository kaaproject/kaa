/*
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

package org.kaaproject.kaa.server.verifiers.trustful.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.verifiers.trustful.config.gen.TrustfulAvroConfig;

@KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
public class TrustfulVerifierConfig implements PluginConfig {

    private static final String TRUSTFUL_VERIFIER_NAME = "Trustful verifier";

    @Override
    public String getPluginTypeName() {
        return TRUSTFUL_VERIFIER_NAME;
    }

    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier";
    }

    @Override
    public Schema getPluginConfigSchema() {
        return TrustfulAvroConfig.SCHEMA$;
    }

}
