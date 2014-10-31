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
package org.kaaproject.kaa.server.operations.service.netty;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class NettySessionInfoTest {

    @Test
    public void equalsHashCode(){
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        NettySessionInfo info1 = new NettySessionInfo(uuid1, null, null, null, null, null, 0, false);
        NettySessionInfo info2 = new NettySessionInfo(uuid1, null, null, null, null, null, 0, false);
        NettySessionInfo info3 = new NettySessionInfo(uuid2, null, null, null, null, null, 0, false);
        Assert.assertEquals(info1, info2);
        Assert.assertEquals(info1.hashCode(), info2.hashCode());
        Assert.assertNotEquals(info1, info3);
        Assert.assertNotEquals(info1.hashCode(), info3.hashCode());
    }
}
