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

package org.kaaproject.kaa.server.common.avro;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;

public class AvroByteArrayConverterTest {

    @Test
    public void convertTest() {
        ControlNodeInfo nodeInfo = new ControlNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10,11,12,45,34,23,67,89,66,12});
        nodeInfo.setConnectionInfo(new ConnectionInfo("kaahost1", 1001, testKeyData));
        nodeInfo.setBootstrapServerCount(101);
        nodeInfo.setOperationsServerCount(102);

        AvroByteArrayConverter<ControlNodeInfo> converter = new AvroByteArrayConverter<ControlNodeInfo>(
                ControlNodeInfo.class);
        try {
            byte[] data = converter.toByteArray(nodeInfo);
            Assert.assertNotNull(data);
            ControlNodeInfo copy = converter.fromByteArray(data);
            Assert.assertNotNull(copy);
            Assert.assertEquals(nodeInfo.getBootstrapServerCount(),
                    copy.getBootstrapServerCount());
            Assert.assertEquals(nodeInfo.getOperationsServerCount(),
                    copy.getOperationsServerCount());
            Assert.assertEquals(nodeInfo.getConnectionInfo().getThriftHost()
                    .toString(), copy.getConnectionInfo().getThriftHost().toString());
            Assert.assertEquals(nodeInfo.getConnectionInfo().getThriftPort(), copy
                    .getConnectionInfo().getThriftPort());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }
}
