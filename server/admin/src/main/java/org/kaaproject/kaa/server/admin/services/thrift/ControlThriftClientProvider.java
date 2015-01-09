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

package org.kaaproject.kaa.server.admin.services.thrift;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import com.twitter.common.thrift.Thrift;
import com.twitter.common.thrift.ThriftFactory;

public class ControlThriftClientProvider implements InitializingBean, DisposableBean {

    @Value("#{properties[control_thrift_host]}")
    private String controlThriftHost;

    @Value("#{properties[control_thrift_port]}")
    private int controlThriftPort;

    private ThriftFactory<ControlThriftService.Iface> clientFactory;
    private Thrift<ControlThriftService.Iface> thrift;

    public ControlThriftClientProvider() {
        clientFactory = ThriftFactory.create(ControlThriftService.Iface.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        clientFactory = ThriftFactory.create(ControlThriftService.Iface.class);
        InetSocketAddress address = new InetSocketAddress(controlThriftHost, controlThriftPort);
        Set<InetSocketAddress> backends = new HashSet<InetSocketAddress>();
        backends.add(address);
        thrift = clientFactory.withMaxConnectionsPerEndpoint(20).withSocketTimeout(Amount.of(20L, Time.SECONDS)).build(backends);
    }

    @Override
    public void destroy() throws Exception {
        thrift.close();
    }

    public ControlThriftService.Iface getClient() {
        return thrift.builder().disableStats().withRequestTimeout(Amount.of(20L, Time.SECONDS)).create();
    }

}
