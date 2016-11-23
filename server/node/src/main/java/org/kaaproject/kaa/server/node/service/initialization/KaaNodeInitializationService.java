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

package org.kaaproject.kaa.server.node.service.initialization;

import static org.kaaproject.kaa.server.common.thrift.KaaThriftService.BOOTSTRAP_SERVICE;
import static org.kaaproject.kaa.server.common.thrift.KaaThriftService.KAA_NODE_SERVICE;
import static org.kaaproject.kaa.server.common.thrift.KaaThriftService.OPERATIONS_SERVICE;

import org.apache.curator.framework.CuratorFramework;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.node.KaaNodeThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class KaaNodeInitializationService extends AbstractInitializationService {

  private static final Logger LOG = LoggerFactory.getLogger(KaaNodeInitializationService.class);
  private final Set<TSocketWrapper> openedSockets = new HashSet<TSocketWrapper>();

  private TServer server;

  private ExecutorService executorService;

  @Autowired
  private KaaNodeThriftService.Iface kaaNodeThriftService;

  @Autowired
  @Lazy
  private BootstrapThriftService.Iface bootstrapThriftService;


  @Autowired
  @Lazy
  private OperationsThriftService.Iface operationsThriftService;

  @Autowired
  @Lazy
  private InitializationService controlInitializationService;

  @Autowired
  @Lazy
  private InitializationService bootstrapInitializationService;

  @Autowired
  @Lazy
  private InitializationService operationsInitializationService;

  @Autowired
  private CuratorFramework zkClient;

  /*
   * (non-Javadoc)
   *
   * @see org.kaaproject.kaa.server.node.bootstrap.KaaNodeBootstrapService#start()
   */
  @Override
  public void start() {

    final CountDownLatch thriftStartupLatch = new CountDownLatch(1);
    final CountDownLatch thriftShutdownLatch = new CountDownLatch(1);

    startThrift(thriftStartupLatch, thriftShutdownLatch);

    try {
      thriftStartupLatch.await();
    } catch (InterruptedException ex) {
      LOG.error("Interrupted while waiting for thrift to start...", ex);
    }

    if (waitZkConnection()) {
      if (getNodeConfig().isControlServiceEnabled()) {
        controlInitializationService.start();
      }
      if (getNodeConfig().isBootstrapServiceEnabled()) {
        bootstrapInitializationService.start();
      }
      if (getNodeConfig().isOperationsServiceEnabled()) {
        operationsInitializationService.start();
      }

      LOG.info("Kaa Node Server Started.");
    } else {
      LOG.error("Failed to connect to Zookeeper within {} minutes."
          + " Kaa Node Server will be stopped.", getNodeConfig().getZkWaitConnectionTime());
      stopThrift();
    }

    try {
      thriftShutdownLatch.await();
    } catch (InterruptedException ex) {
      LOG.error("Interrupted while waiting for thrift to stop...", ex);
    }

  }

  private boolean waitZkConnection() {
    if (!zkClient.isStarted()) {
      zkClient.start();
    }
    try {
      LOG.info("Waiting connection to Zookeeper at ", getNodeConfig().getZkHostPortList());
      return zkClient.blockUntilConnected(getNodeConfig().getZkWaitConnectionTime(),
          TimeUnit.MINUTES);
    } catch (InterruptedException ex) {
      LOG.error("Zookeeper client was interrupted while waiting for connection! ",
          getNodeConfig().getZkHostPortList(), ex);
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.kaaproject.kaa.server.node.bootstrap.KaaNodeBootstrapService#stop()
   */
  @Override
  public void stop() {
    if (getNodeConfig().isControlServiceEnabled()) {
      controlInitializationService.stop();
    }
    if (getNodeConfig().isBootstrapServiceEnabled()) {
      bootstrapInitializationService.stop();
    }
    if (getNodeConfig().isOperationsServiceEnabled()) {
      operationsInitializationService.stop();
    }
    zkClient.close();

    server.stop();
    ThriftExecutor.shutdown();

    LOG.info("Kaa Node Server Stopped.");
  }

  /**
   * Stop thrift.
   */
  public void stopThrift() {
    if (zkClient != null) {
      zkClient.close();
    }
    if (server != null) {
      server.stop();
    }
    ThriftExecutor.shutdown();

    LOG.info("Kaa Node Server Stopped.");
  }

  /**
   * Start thrift.
   */
  private void startThrift(final CountDownLatch thriftStartupLatch,
                           final CountDownLatch thriftShutdownLatch) {

    Runnable thriftRunnable = new Runnable() {

      @Override
      public void run() {
        LOG.info("Initializing Thrift Service for Kaa Node Server....");
        LOG.info("host: " + getNodeConfig().getThriftHost());
        LOG.info("port: " + getNodeConfig().getThriftPort());

        try {
          TMultiplexedProcessor processor = new TMultiplexedProcessor();

          KaaNodeThriftService.Processor<KaaNodeThriftService.Iface> kaaNodeProcessor =
              new KaaNodeThriftService.Processor<>(kaaNodeThriftService);
          processor.registerProcessor(KAA_NODE_SERVICE.getServiceName(), kaaNodeProcessor);

          if (getNodeConfig().isBootstrapServiceEnabled()) {
            BootstrapThriftService.Processor<BootstrapThriftService.Iface> bootstrapProcessor =
                new BootstrapThriftService.Processor<>(bootstrapThriftService);
            processor.registerProcessor(BOOTSTRAP_SERVICE.getServiceName(), bootstrapProcessor);
          }

          if (getNodeConfig().isOperationsServiceEnabled()) {
            OperationsThriftService.Processor<OperationsThriftService.Iface> operationsProcessor =
                new OperationsThriftService.Processor<>(operationsThriftService);
            processor.registerProcessor(OPERATIONS_SERVICE.getServiceName(), operationsProcessor);
          }

          TServerTransport serverTransport = createServerSocket();
          server = createServer(serverTransport, processor);

          LOG.info("Thrift Kaa Node Server Started.");

          thriftStartupLatch.countDown();

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

          LOG.info("Thrift Kaa Node Server Stopped.");

          thriftShutdownLatch.countDown();

        } catch (TTransportException ex) {
          LOG.error("TTransportException", ex);
        } finally {
          if (thriftStartupLatch.getCount() > 0) {
            thriftStartupLatch.countDown();
          }
          if (thriftShutdownLatch.getCount() > 0) {
            LOG.info("Thrift Kaa Node Server Stopped.");
            thriftShutdownLatch.countDown();
          }
        }
      }
    };

    new Thread(thriftRunnable).start();

  }

  /**
   * Creates the server socket.
   *
   * @return the t server transport
   * @throws TTransportException the t transport exception
   */
  public TServerTransport createServerSocket() throws TTransportException {
    return new TServerSocket(
        new InetSocketAddress(getNodeConfig().getThriftHost(), getNodeConfig().getThriftPort())) {
      @Override
      protected TSocket acceptImpl() throws TTransportException {
        ServerSocket serverSocket = getServerSocket();
        if (serverSocket == null) {
          throw new TTransportException(
              TTransportException.NOT_OPEN,
              "No underlying server socket."
          );
        }
        try {
          Socket result = serverSocket.accept();
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

  /**
   * Creates the server.
   *
   * @param serverTransport the server transport
   * @param processor       the processor
   * @return the t server
   */
  public TServer createServer(TServerTransport serverTransport, TMultiplexedProcessor processor) {
    TThreadPoolServer.Args args = new Args(serverTransport).processor(processor);
    args.stopTimeoutVal = 3;
    args.stopTimeoutUnit = TimeUnit.SECONDS;

    SynchronousQueue<Runnable> executorQueue = // NOSONAR
        new SynchronousQueue<Runnable>();
    executorService = new ThreadPoolExecutor(args.minWorkerThreads, args.maxWorkerThreads,
        60, TimeUnit.SECONDS, executorQueue);
    args.executorService = executorService;
    return new TThreadPoolServer(args);
  }

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

}
