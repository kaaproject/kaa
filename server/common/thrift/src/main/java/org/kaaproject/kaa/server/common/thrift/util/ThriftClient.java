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

/**
 * 
 */
package org.kaaproject.kaa.server.common.thrift.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ThriftClient.
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 * @param <T> the generic type
 */
public class ThriftClient<T extends TServiceClient> implements Runnable {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ThriftClient.class);
    
    /** The endpoint host. */
    private String endpointHost;
    
    /** The endpoint port. */
    private int endpointPort;
    
    /** The t class. */
    private Class<T> tClass;
    
    /** The t constructor. */
    private Constructor<T> tConstructor;
    
    /** The client. */
    private T client;
    
    /** The transport. */
    private TTransport transport;
    
    /** The activity. */
    private ThriftActivity<T> activity;
    
    /**
     * The Constructor.
     *
     * @param endpointHost the endpoint host
     * @param endpointPort the endpoint port
     * @param kaaThriftService the kaa thrift service
     * @param clazz the clazz
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws InvocationTargetException the invocation target exception
     */
    public ThriftClient(String endpointHost, int endpointPort, KaaThriftService kaaThriftService, Class<T> clazz) 
            throws NoSuchMethodException,
                   InstantiationException, 
                   IllegalAccessException, 
                   InvocationTargetException {
        this.tClass = clazz;
        this.endpointHost = endpointHost;
        this.endpointPort = endpointPort;
        tConstructor = tClass.getConstructor(TProtocol.class, TProtocol.class);
        transport = new TSocket(endpointHost, endpointPort);
        LOG.debug("ThriftClient sokcet to "+endpointHost+":"+endpointPort+" created.");
        TProtocol protocol = new TBinaryProtocol(transport);
        TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, kaaThriftService.getServiceName());
        client = tConstructor.newInstance(mp, mp);
        LOG.debug("ThriftClient new Client to "+endpointHost+":"+endpointPort+" created.");
    }
    
    /**
     * Sets the thrift activity.
     *
     * @param activity the thrift activity
     */
    public void setThriftActivity(ThriftActivity<T> activity) {
        this.activity  = activity;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            LOG.debug("Call Thrift server ["
                    + endpointHost + ":" + endpointPort + "]");
            transport.open();
            if (activity != null) {
                invoke(activity);

                LOG.debug("Successfuly invoke Thrift server ["
                    + endpointHost + ":" + endpointPort + "]");
                activity.isSuccess(true);
            } else {
                LOG.error(
                        "Error - Activity not set while invoke thrift object "+ endpointHost + ":" + endpointPort);
            }
        } catch (TException 
                | IllegalArgumentException 
                | SecurityException e) {
            LOG.error(
                    "Unexpected error occurred while invoke thrift object "+ endpointHost + ":" + endpointPort,
                    e);
            if (activity != null) {
                activity.isSuccess(false);
            }
        } finally {
            transport.close();
        }

    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    public T getClient() {
        return client;
    }

    /**
     * Invoke.
     *
     * @param a the a
     */
    public void invoke(ThriftActivity<T> a) {
        a.doInTemplate(client);
    }
}
