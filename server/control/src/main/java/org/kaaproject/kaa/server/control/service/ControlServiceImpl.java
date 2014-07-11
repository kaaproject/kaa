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

package org.kaaproject.kaa.server.control.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The Class ControlServiceImpl.
 */
@Service
public class ControlServiceImpl implements ControlService {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ControlServiceImpl.class);

    /** The thrift host. */
    @Value("#{properties[thrift_host]}")
    private String host;

    /** The thrift port. */
    @Value("#{properties[thrift_port]}")
    private int port;

    /** The Thrift server. */
    private TServer server;
    
    /** The Thrift server executor service. */
    private ExecutorService executorService;

    /** The control service thrift interface. */
    @Autowired
    private ControlThriftService.Iface controlService;

    /** The control zookeeper service. */
    @Autowired
    private ControlZkService controlZkService;
    
    /** Dynamic Load Distribution Service */
    @Autowired
    private LoadDistributionService loadMgmtService;
    /** The control service is started. */
    private boolean serviceStarted = false;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#start()
     */
    @Override
    public void start() {
        if (!serviceStarted) {
            serviceStarted = true;
            controlZkService.start();
            loadMgmtService.setZkService(controlZkService);
            loadMgmtService.start();
            // blocking method
            startThrift();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#stop()
     */
    @Override
    public void stop() {
        if (serviceStarted) {
            serviceStarted = false;
            loadMgmtService.shutdown();
            controlZkService.stop();
            server.stop();
        }
    }

    /**
     * Start thrift.
     */
    private void startThrift() {
        LOG.info("Initializing Thrift Service for Control Server....");
        LOG.info("host: " + host);
        LOG.info("port: " + port);

        try {
            ControlThriftService.Processor<ControlThriftService.Iface> processor = new ControlThriftService.Processor<ControlThriftService.Iface>(
                    controlService);
            TServerTransport serverTransport = createServerSocket();
            server = createServer(serverTransport, processor);

            LOG.info("Control Server Started.");

            server.serve();
            
            if (executorService != null && !executorService.isTerminated()) {
                for (TSocketWrapper socket : new ArrayList<>(openedSockets)) {
                    if (socket.getSocket() != null && !socket.getSocket().isClosed()) {
                        socket.close();
                    }
                }
                LOG.info("Terminating executor service.");
                executorService.shutdownNow();
            }

            LOG.info("Control Server Stopped.");

        } catch (TTransportException e) {
            LOG.error("TTransportException", e);
        }
    }
    
    /**
     * Creates the server socket.
     *
     * @return the t server transport
     * @throws TTransportException the t transport exception
     */
    public TServerTransport createServerSocket() throws TTransportException {
        return new TServerSocket(new InetSocketAddress(host, port)) {
          @Override
          protected TSocket acceptImpl() throws TTransportException {
              ServerSocket serverSocket_ = getServerSocket();
              if (serverSocket_ == null) {
                  throw new TTransportException(TTransportException.NOT_OPEN, "No underlying server socket.");
                }
                try {
                  Socket result = serverSocket_.accept();
                  TSocketWrapper result2 = new TSocketWrapper(result);
                  result2.setTimeout(0);
                  openedSockets.add(result2);
                  return result2;
                } catch (IOException iox) {
                  throw new TTransportException(iox);
                }
          }
        };
    }
    
    private Set<TSocketWrapper> openedSockets = new HashSet<TSocketWrapper>();
    
    class TSocketWrapper extends TSocket {
        public TSocketWrapper(Socket socket) throws TTransportException {
            super(socket);
        }
        
        @Override
        public void close() {
            super.close();
            openedSockets.remove(this);
        }
    }
    
    /**
     * Creates the server.
     *
     * @param serverTransport the server transport
     * @param processor the processor
     * @return the t server
     */
    public TServer createServer(TServerTransport serverTransport,
                                ControlThriftService.Processor<ControlThriftService.Iface> processor) {
        TThreadPoolServer.Args args = new Args(serverTransport).processor(processor);
        args.stopTimeoutVal = 3;
        args.stopTimeoutUnit = TimeUnit.SECONDS;
        
        SynchronousQueue<Runnable> executorQueue =
                new SynchronousQueue<Runnable>();
        executorService = new ThreadPoolExecutor(args.minWorkerThreads,
                                            args.maxWorkerThreads,
                                            60,
                                            TimeUnit.SECONDS,
                                            executorQueue);
        args.executorService = executorService;
        return new TThreadPoolServer(args);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.service.ControlService#started()
     */
    @Override
    public boolean started() {
        return serviceStarted;
    }
}
