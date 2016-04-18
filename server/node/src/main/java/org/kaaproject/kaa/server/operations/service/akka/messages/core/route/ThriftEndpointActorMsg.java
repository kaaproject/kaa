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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public class ThriftEndpointActorMsg<T> extends AbstractEndpointActorMsg {

    private final T msg;

    public ThriftEndpointActorMsg(EndpointAddress address, ActorClassifier classifier, T msg) {
        super(address, classifier);
        this.msg = msg;
    }

    public T getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ThriftEndpointActorMsg [msg=" + msg + ", address=" + getAddress() + ", classifier=" + getClassifier() + "]";
    }

}
