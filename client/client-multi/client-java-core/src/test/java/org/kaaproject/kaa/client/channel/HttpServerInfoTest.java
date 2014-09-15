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

package org.kaaproject.kaa.client.channel;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;

public class HttpServerInfoTest {
    @Test
    public void testURL() {
        try {
            String host;

            HttpServerInfo hsi1 = new HttpServerInfo(ServerType.OPERATIONS, "test.com", 80, KeyUtil.generateKeyPair().getPublic());
            host = "http://" + hsi1.getHost() + ":" + hsi1.getPort() + CommonEPConstans.SYNC_URI;

            Assert.assertTrue(hsi1.getURL().equals(host));

            HttpServerInfo hsi2 = new HttpServerInfo(ServerType.BOOTSTRAP, "test.com", 80, KeyUtil.generateKeyPair().getPublic());
            host = "http://" + hsi2.getHost() + ":" + hsi2.getPort() + CommonBSConstants.BOOTSTRAP_RESOLVE_URI;

            Assert.assertTrue(hsi2.getURL().equals(host));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

}
