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

package org.kaaproject.kaa.server.bootstrap.service;

import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolVersionId;

/**
 * Maintains list of active operation servers and allow to filter
 * this list based on @{link }
 * 
 * @author Andrey Shvayka
 *
 */
public interface OperationsServerListService {

    void init(BootstrapNode bootstrapNode);
    
    Set<ProtocolConnectionData> filter(List<ProtocolVersionId> keys);

}
