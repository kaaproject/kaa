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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;

public class CallbacksTest {

    @Test
    public void multiLogDeliveryCallbackTest() {
        MultiLogDeliveryCallback callback;
        ActorSystem system = ActorSystem.create("actorSystem");
        ActorRef actorRef = system.actorOf(Props.create(TestActor.class));
        callback = new MultiLogDeliveryCallback(actorRef, 1, 1);
        Assert.assertNotNull(callback);
        callback.onSuccess();
        callback = new MultiLogDeliveryCallback(actorRef, 1, 3);
        callback.onSuccess();
        callback.sendSuccessToEndpoint();
        callback.sendFailureToEndpoint(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
        callback.onInternalError();
        callback.onRemoteError();
        callback.onConnectionError();
    }

    @Test
    public void singleLogDeliveryCallbackTest() {
        SingleLogDeliveryCallback callback;
        ActorSystem system = ActorSystem.create("actorSystem");
        ActorRef actorRef = system.actorOf(Props.create(TestActor.class));
        callback = new SingleLogDeliveryCallback(actorRef, 1);
        Assert.assertNotNull(callback);
    }

    private static class TestActor extends UntypedActor {
        @Override
        public void onReceive(Object arg0) throws Exception {
            unhandled(arg0);
        }
    }

}
