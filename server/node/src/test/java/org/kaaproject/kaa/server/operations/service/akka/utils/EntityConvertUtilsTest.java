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

package org.kaaproject.kaa.server.operations.service.akka.utils;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;
import org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode;


public class EntityConvertUtilsTest {
    @Test
    public void toUserVerifierErrorCodeTest() {
        Assert.assertEquals(EntityConvertUtils.toErrorCode((UserVerifierErrorCode) null), null);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.NO_VERIFIER_CONFIGURED),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.NO_VERIFIER_CONFIGURED);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.TOKEN_INVALID),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.TOKEN_INVALID);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.TOKEN_EXPIRED),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.TOKEN_EXPIRED);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.INTERNAL_ERROR),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.INTERNAL_ERROR);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.CONNECTION_ERROR),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.CONNECTION_ERROR);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.REMOTE_ERROR),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.REMOTE_ERROR);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(UserVerifierErrorCode.OTHER),
                org.kaaproject.kaa.server.sync.UserVerifierErrorCode.OTHER);
    }

    @Test
    public void toLogDeliveryErrorCodeTest() {
        Assert.assertEquals(EntityConvertUtils.toErrorCode((LogDeliveryErrorCode) null), null);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR),
                org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED),
                org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR),
                org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR);
        Assert.assertEquals(EntityConvertUtils.toErrorCode(LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR),
                org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR);
    }
}
