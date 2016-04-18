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

package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.flume.source.avro.AvroSourceProtocol;
import org.apache.flume.source.avro.Status;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.compression.ZlibEncoder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroAsyncRpcClientTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(AvroAsyncRpcClientTest.class);
        
        private static final String localhost = "localhost";
        
        
        @Test
        public void testOkAppendBatchAsyncClient() throws FlumeException, EventDeliveryException {
          handlerBatchAppendAsyncTest(new OKAvroHandler(), 10, 5, 5, false, false, 0);
          handlerBatchAppendAsyncTest(new OKAvroHandler(), 10, 5, 10, false, false, 0);
          handlerBatchAppendAsyncTest(new OKAvroHandler(), 10, 5, 10, true, true, 6);
        }
        
        @Test
        public void testOkSleepAppendBatchAsyncClient() throws FlumeException, EventDeliveryException {
          
          //This will send 100,000 messages and wait 50 milliseconds for each batch
          //1 thread will take over 5 seconds
          //10 threads will take under a second
          
          long startTime = System.currentTimeMillis();
          handlerBatchAppendAsyncTest(new OKSleepAvroHandler(50), 1000, 1, 100, false, false, 0);
          System.out.println("Test 1: " + (System.currentTimeMillis() - startTime));
         
          startTime = System.currentTimeMillis();
          handlerBatchAppendAsyncTest(new OKSleepAvroHandler(50), 1000, 5, 100, false, false, 0);
          System.out.println("Test 2: " + (System.currentTimeMillis() - startTime));
          
          startTime = System.currentTimeMillis();
          handlerBatchAppendAsyncTest(new OKSleepAvroHandler(50), 1000, 10, 100, false, false, 0);
          System.out.println("Test 3: " + (System.currentTimeMillis() - startTime));
          
         }
        
        @Test
        public void testOkAppendAsyncClient() throws FlumeException, EventDeliveryException {
          handlerAppendAsyncTest(new OKAvroHandler(), 5, 5, false, false, 0);
          handlerAppendAsyncTest(new OKAvroHandler(), 5, 10, false, false, 0);
          handlerAppendAsyncTest(new OKAvroHandler(), 5, 10, true, true, 6);
        }
        
        public static void handlerBatchAppendAsyncTest(AvroSourceProtocol handler, int batchSize, int numberOfAsyncThreads, int numberOfBatchs, boolean enableServerCompression, boolean enableClientCompression, int compressionLevel)
            throws FlumeException, EventDeliveryException {
          AvroAsyncRpcClient client = null;
          Server server = startServer(handler, 0 , enableServerCompression);
          try {

            Properties starterProp = new Properties();
            if (enableClientCompression) {
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_TYPE, "deflate");
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_LEVEL, "" + compressionLevel);
            } else {
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_TYPE, "none");
            }
            starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_BATCH_SIZE, Integer.toString(batchSize));
            starterProp.setProperty(AvroAsyncRpcClient.ASYNC_MAX_THREADS, Integer.toString(numberOfAsyncThreads));

            client = getStockLocalClient(server.getPort(), starterProp);
            boolean isActive = client.isActive();
            Assert.assertTrue("Client should be active", isActive);

            ArrayList<Future<AppendBatchAsyncResultPojo>> futureList = new  ArrayList<Future<AppendBatchAsyncResultPojo>>(); 
            
            for (int b = 0; b < numberOfBatchs; b++) {
              List<Event> events = new ArrayList<Event>();
              for (int i = 0; i < batchSize; i++) {
                events.add(EventBuilder.withBody(((char)b) + ": " + i, Charset.forName("UTF8")));
              }
              //long startTime = System.currentTimeMillis();
              futureList.add(client.appendBatchAsync(events));
              //System.out.println(" - Send Batch 1:" + (System.currentTimeMillis() - startTime));
             
              
            }
            
            for (int b = 0; b < numberOfBatchs; b++) {
              
              AppendBatchAsyncResultPojo pojo = futureList.get(b).get();
              Assert.assertTrue(pojo.isSuccessful);
              Assert.assertTrue(pojo.getEvents().size() == batchSize);
              Assert.assertTrue(  (pojo.getEvents().get(0).getBody()[0]) == b);
            }
            
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } finally {
            stopServer(server);
            if (client != null) {
                client.close();
            }
          }
        }

        public static void handlerAppendAsyncTest(AvroSourceProtocol handler, int numberOfAsyncThreads, int numberOfEvent, boolean enableServerCompression, boolean enableClientCompression, int compressionLevel)
            throws FlumeException, EventDeliveryException {
          AvroAsyncRpcClient client = null;
          Server server = startServer(handler, 0 , enableServerCompression);
          try {

            Properties starterProp = new Properties();
            if (enableClientCompression) {
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_TYPE, "deflate");
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_LEVEL, "" + compressionLevel);
            } else {
              starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_COMPRESSION_TYPE, "none");
            }
            
            starterProp.setProperty(AvroAsyncRpcClient.ASYNC_MAX_THREADS, Integer.toString(numberOfAsyncThreads));

            client = getStockLocalClient(server.getPort(), starterProp);
            boolean isActive = client.isActive();
            Assert.assertTrue("Client should be active", isActive);

            ArrayList<Future<AppendAsyncResultPojo>> futureList = new  ArrayList<Future<AppendAsyncResultPojo>>(); 
            
            for (int b = 0; b < numberOfEvent; b++) {
              futureList.add(client.appendAsync(EventBuilder.withBody(b + ": 1", Charset.forName("UTF8"))));
            }
            
            for (int b = 0; b < numberOfEvent; b++) {
              
              AppendAsyncResultPojo pojo = futureList.get(b).get();
              Assert.assertTrue(pojo.isSuccessful);
              Assert.assertTrue(Character.getNumericValue(((char)(pojo.getEvent().getBody()[0]))) == b);
            }
            
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } finally {
            stopServer(server);
            if (client != null) {
                client.close();
            }
          }
        }

        
        /**
         * Start a NettyServer, wait a moment for it to spin up, and return it.
         */
        public static Server startServer(AvroSourceProtocol handler, int port, boolean enableCompression) {
          Responder responder = new SpecificResponder(AvroSourceProtocol.class,
              handler);
          Server server;
          if (enableCompression) {
            server = new NettyServer(responder,
                new InetSocketAddress(localhost, port),
                new NioServerSocketChannelFactory
                (Executors.newFixedThreadPool(10), Executors.newFixedThreadPool(10)),
                new CompressionChannelPipelineFactory(), null);
          } else {
            server = new NettyServer(responder,
              new InetSocketAddress(localhost, port));
          }
          server.start();
          LOG.info("Server started on hostname: {}, port: {}",
                  new Object[]{localhost, Integer.toString(server.getPort())});

          try {

            Thread.sleep(300L);

          } catch (InterruptedException ex) {
            LOG.error("Thread interrupted. Exception follows.", ex);
            Thread.currentThread().interrupt();
          }

          return server;
        }
        
        public static void stopServer(Server server) {
          try {
            server.close();
            server.join();
          } catch (InterruptedException ex) {
            LOG.error("Thread interrupted. Exception follows.", ex);
            Thread.currentThread().interrupt();
          }
        }
        
        public static AvroAsyncRpcClient getStockLocalClient(int port) {
          Properties props = new Properties();

          return getStockLocalClient(port, props);
        }
        
        public static AvroAsyncRpcClient getStockLocalClient(int port, Properties starterProp) {

          starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_HOSTS, "h1");
          starterProp.setProperty(RpcClientConfigurationConstants.CONFIG_HOSTS_PREFIX + "h1",  "127.0.0.1" + ":" + port);
          
          AvroAsyncRpcClient client = new AvroAsyncRpcClient(starterProp, 2);
          
          return client;
        }
        
        private static class CompressionChannelPipelineFactory implements
        ChannelPipelineFactory {

          @Override
          public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();
            ZlibEncoder encoder = new ZlibEncoder(6);
            pipeline.addFirst("deflater", encoder);
            pipeline.addFirst("inflater", new ZlibDecoder());
            return pipeline;
          }
        }

        /**
         * A service that logs receipt of the request and returns Failed
         */
        public static class FailedAvroHandler implements AvroSourceProtocol {

          @Override
          public Status append(AvroFlumeEvent event) throws AvroRemoteException {
            LOG.info("Failed: Received event from append(): {}",
                    new String(event.getBody().array(), Charset.forName("UTF8")));
            return Status.FAILED;
          }

          @Override
          public Status appendBatch(List<AvroFlumeEvent> events) throws
              AvroRemoteException {
            LOG.info("Failed: Received {} events from appendBatch()",
                    events.size());
            return Status.FAILED;
          }

        }

        /**
         * A service that logs receipt of the request and returns Unknown
         */
        public static class UnknownAvroHandler implements AvroSourceProtocol {

          @Override
          public Status append(AvroFlumeEvent event) throws AvroRemoteException {
            LOG.info("Unknown: Received event from append(): {}",
                    new String(event.getBody().array(), Charset.forName("UTF8")));
            return Status.UNKNOWN;
          }

          @Override
          public Status appendBatch(List<AvroFlumeEvent> events) throws
              AvroRemoteException {
            LOG.info("Unknown: Received {} events from appendBatch()",
                    events.size());
            return Status.UNKNOWN;
          }

        }
        
        /**
         * A service that logs receipt of the request and returns OK
         */
        public static class OKAvroHandler implements AvroSourceProtocol {

          @Override
          public Status append(AvroFlumeEvent event) throws AvroRemoteException {
            LOG.info("OK: Received event from append(): {}",
                    new String(event.getBody().array(), Charset.forName("UTF8")));
            return Status.OK;
          }

          @Override
          public Status appendBatch(List<AvroFlumeEvent> events) throws
              AvroRemoteException {
            LOG.info("OK: Received {} events from appendBatch()",
                    events.size());
            return Status.OK;
          }

        }
        
        /**
         * A service that logs receipt of the request and returns OK
         */
        public static class OKSleepAvroHandler implements AvroSourceProtocol {

          int sleepTime;
          
          public OKSleepAvroHandler(int sleepTime) {
            this.sleepTime = sleepTime;
          }
          
          @Override
          public Status append(AvroFlumeEvent event) throws AvroRemoteException {
            LOG.info("OK Sleep: Received event from append(): {}",
                    new String(event.getBody().array(), Charset.forName("UTF8")));
            try {
              Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return Status.OK;
          }

          @Override
          public Status appendBatch(List<AvroFlumeEvent> events) throws
              AvroRemoteException {
            //System.out.println("OK Sleep: Received {} events from appendBatch()" + events.size());
            
            LOG.info("OK Sleep: Received {} events from appendBatch()",
                    events.size());
            try {
              Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return Status.OK;
          }

        }
}
