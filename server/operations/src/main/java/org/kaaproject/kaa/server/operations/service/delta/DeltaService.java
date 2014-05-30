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

package org.kaaproject.kaa.server.operations.service.delta;

import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;


/**
 * Service that performs delta calculation process.
 * 
 * @author ashvayka
 */
public interface DeltaService {

    /**
     * Gets the delta.
     *
     * @param request the request
     * @param historyDelta the history delta
     * @param curAppSeqNumber the cur app seq number
     * @return the delta
     * @throws GetDeltaException the get delta exception
     */
    GetDeltaResponse getDelta(GetDeltaRequest request, HistoryDelta historyDelta, int curAppSeqNumber) throws GetDeltaException;

}
