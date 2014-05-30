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
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;

public class ChangeTest {

    @Test
    public void changeTest() {
        ChangeDto dto = new ChangeDto();
        ObjectId groupId = new ObjectId();
        ObjectId filterId = new ObjectId();
        ObjectId configId = new ObjectId();
        ObjectId topicId = new ObjectId();
        dto.setConfigurationId(configId.toString());
        dto.setProfileFilterId(filterId.toString());
        dto.setEndpointGroupId(groupId.toString());
        dto.setTopicId(topicId.toString());
        dto.setPfMajorVersion(1);
        dto.setCfMajorVersion(1);
        dto.setType(ChangeType.ADD_CONF);

        Change change = new Change(dto);
        ChangeDto converted = change.toDto();
        Assert.assertEquals(dto, converted);
        Change changeTwo = new Change(converted);
        Assert.assertEquals(change, changeTwo);
        Assert.assertEquals(change.hashCode(), changeTwo.hashCode());
        Assert.assertEquals(change.toString(), changeTwo.toString());
    }
}
