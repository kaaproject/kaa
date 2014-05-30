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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;

public class EndpointGroupStateTest {

    @Test
    public void EndpointStateTest(){
        EndpointGroupStateDto state = new EndpointGroupStateDto();
        ObjectId groupId = new ObjectId();
        ObjectId filterId = new ObjectId();
        ObjectId configId = new ObjectId();
        state.setConfigurationId(configId.toString());
        state.setProfileFilterId(filterId.toString());
        state.setEndpointGroupId(groupId.toString());

        EndpointGroupState stateOne = new EndpointGroupState(state);
        EndpointGroupState stateTwo = new EndpointGroupState(groupId, filterId, configId);
        Assert.assertEquals(stateOne, stateTwo);
        Assert.assertEquals(stateOne.hashCode(), stateTwo.hashCode());
        Assert.assertEquals(stateOne.toDto(), stateTwo.toDto());
        Assert.assertEquals(stateOne.toString(), stateTwo.toString());
    }


}
