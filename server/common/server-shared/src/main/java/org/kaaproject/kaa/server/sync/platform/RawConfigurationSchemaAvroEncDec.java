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

package org.kaaproject.kaa.server.sync.platform;


import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.platform.AvroEncDec;

@KaaPlatformProtocol
public class RawConfigurationSchemaAvroEncDec extends AvroEncDec {

    @Override
    public int getId() {
        return Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID_V2;
    }

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        ClientSync sync = super.decode(data);
        sync.setUseConfigurationRawSchema(true);
        return sync;
    }
}
