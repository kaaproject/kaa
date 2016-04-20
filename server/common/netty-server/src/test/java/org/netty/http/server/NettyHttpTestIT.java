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

package org.netty.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration Test which demonstrates Netty framework error in case when parses Multipart-mixed POST
 * HTTP request. In case when multipart entity ends with odd number of '0x0D' bytes, Netty framework append 
 * one more '0x0D' byte.
 * 
 * To extract multipart entities used flowing code:
 * 
 *      HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
 *      HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
 *      InterfaceHttpData data = decoder.getBodyHttpData(HTTP_TEST_ATTRIBUTE);
 *      Attribute attribute = (Attribute) data;
 *      requestData = attribute.get();
 * 
 * Test initialize Netty server and produce Http request with POST multipart entity.
 * 
 * @author Andrey Panasenko
 *
 */
public class NettyHttpTestIT {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpTestIT.class);
    
    private static String[] hex = new String[] {"0","1","2","3","4","5","6", "7","8","9","A","B","C","D","E","F"};
    
    /** Netty bind port */
    private static final int HTTP_BIND_PORT = 9878;
    
    private static final int DEFAULT_REQUEST_MAX_SIZE = 2048;
    
    /** Multipart entity Name in POST request */
    public static final String HTTP_TEST_ATTRIBUTE = "Test-attribute";
    
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "x-application/kaaproject";
    
    /** Thread executor, used for HttpClient operation and Netty starter operation */
    private static ExecutorService executor = null;
    
    private NettyStarter netty = null;

    /** Byte array filled out with sending bytes in multipart entity */
    private byte[] sendingArray;
    
    /**
     * Netty starter class.
     * Initialize netty framework.
     * Start Netty.
     * Shutdown netty.
     * @author Andrey Panasenko
     *
     */
    public class NettyStarter implements Runnable {

        private  EventLoopGroup bossGroup;
        private  EventLoopGroup workerGroup;
        private  ServerBootstrap bServer;
        private  EventExecutorGroup eventExecutor;
        private  Channel bindChannel;

        public NettyStarter() throws InterruptedException {
            LOG.info("NettyHttpServer Initializing...");
            bossGroup = new NioEventLoopGroup();
            LOG.trace("NettyHttpServer bossGroup created.");
            workerGroup = new NioEventLoopGroup();
            LOG.trace("NettyHttpServer workGroup created.");
            bServer = new ServerBootstrap();
            LOG.trace("NettyHttpServer ServerBootstrap created.");
            eventExecutor = new DefaultEventExecutorGroup(1);
                    
            LOG.trace("NettyHttpServer Task Executor created.");

            DefaultServerInitializer sInit = new DefaultServerInitializer(eventExecutor);
            LOG.trace("NettyHttpServer InitClass instance created.");
            
            LOG.trace("NettyHttpServer InitClass instance Init().");
            bServer.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).childHandler(sInit)
                    .option(ChannelOption.SO_REUSEADDR, true);
            LOG.trace("NettyHttpServer ServerBootstrap group initialized.");
            bindChannel = bServer.bind(HTTP_BIND_PORT).sync().channel();
        }
        
        public void shutdown() {
            LOG.info("NettyHttpServer stopping...");
            if (bossGroup != null) {
                try {
                    Future<? extends Object> f = bossGroup.shutdownGracefully();
                    f.await();
                } catch (InterruptedException e) {
                    LOG.trace("NettyHttpServer stopping: bossGroup error", e);
                } finally {
                    bossGroup = null;
                    LOG.trace("NettyHttpServer stopping: bossGroup stoped");
                }
            }
            if (workerGroup != null) {
                try {
                    Future<? extends Object> f = workerGroup.shutdownGracefully();
                    f.await();
                } catch (InterruptedException e) {
                    LOG.trace("NettyHttpServer stopping: workerGroup error", e);
                } finally {
                    workerGroup = null;
                    LOG.trace("NettyHttpServer stopping: workerGroup stopped");
                }
            }
            if (eventExecutor != null) {
                try {
                    Future<? extends Object> f = eventExecutor.shutdownGracefully();
                    f.await();
                } catch (InterruptedException e) {
                    LOG.trace("NettyHttpServer stopping: task executor error", e);
                } finally {
                    eventExecutor = null;
                    LOG.trace("NettyHttpServer stopping: task executor stopped.");
                }
            }
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LOG.info("NettyHttpServer starting...");
            try {
                bindChannel.closeFuture().sync();
            } catch (InterruptedException e) {
                LOG.error("NettyHttpServer error", e);
            } finally {
                shutdown();
                LOG.info("NettyHttpServer shut down");
            }
        }
    }
    
    /**
     * Netty channel initializer.
     * @author Andrey Panasenko
     *
     */
    public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {

        private EventExecutorGroup eventExecutor;

        public DefaultServerInitializer(EventExecutorGroup eventExecutor) {
            this.eventExecutor = eventExecutor;
        }
        /* (non-Javadoc)
         * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
         */
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline p = ch.pipeline();
            LOG.info("New connection from {}",ch.remoteAddress().toString());
            p.addLast("httpDecoder", new HttpRequestDecoder());
            p.addLast("httpAggregator",
                    new HttpObjectAggregator(DEFAULT_REQUEST_MAX_SIZE));
            p.addLast("httpEncoder", new HttpResponseEncoder());
            p.addLast("handler", new DefaultHandler(eventExecutor));
            p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
            
        }
        
    }
    
    /**
     * HTTP Request handler.
     * @author Andrey Panasenko
     *
     */
    public class DefaultHandler extends
        SimpleChannelInboundHandler<HttpRequest> {

        private EventExecutorGroup eventExecutor;
        
        public DefaultHandler(EventExecutorGroup eventExecutor) {
            this.eventExecutor = eventExecutor;
        }

        /* (non-Javadoc)
         * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
         */
        @Override
        protected void channelRead0(final ChannelHandlerContext ctx,
                HttpRequest msg) throws Exception {
            HttpHandler handler = new HttpHandler();
            handler.setHttpRequest(msg);
            
            final Future<HttpHandler> future = (Future<HttpHandler>) eventExecutor.submit(handler);
            future.addListener(new GenericFutureListener<Future<HttpHandler>>() {

                @Override
                public void operationComplete(Future<HttpHandler> future) throws Exception {
                    LOG.trace("HttpHandler().operationComplete...");
                    if (future.isSuccess()) {
                        HttpResponse response = future.get().getHttpResponse();
                        if (response != null) {
                            ctx.writeAndFlush(response);
                        } else {
                            ctx.fireExceptionCaught(new Exception("Error creating response"));
                        }
                    } else {
                        ctx.fireExceptionCaught(future.cause());
                    }
                    
                }
                
            });
            
        }
        
    }
    
    /**
     * HTTP Request handler.
     * Parse HTTP Request.
     * Check received bytes[] in multipart entity with sending bytes[]
     * Produce response if check correct.
     * @author Andrey Panasenko
     *
     */
    public class HttpHandler implements Callable<HttpHandler> {

        private FullHttpResponse response;
        private byte[] requestData;
        
        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public HttpHandler call() throws Exception {
            if (requestData.length <= 0) {
                LOG.error("HttpRequest not received byte[]");
                throw new BadRequestException("HttpRequest not received byte[]");
            }
            
            LOG.info(bytesToString(sendingArray, ":"));
            LOG.info(bytesToString(requestData, ":"));
            assertEquals(ByteBuffer.wrap(sendingArray), ByteBuffer.wrap(requestData));
            
            LOG.info("Received array equalse sent array");
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(requestData));
            response.headers().set(CONTENT_TYPE, HTTP_RESPONSE_CONTENT_TYPE);
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
            return this;
        }
        
        public void setHttpRequest(HttpRequest request) throws BadRequestException, IOException {
            if (request == null) {
                LOG.error("HttpRequest not initialized");
                throw new BadRequestException("HttpRequest not initialized");
            }
            if (!request.getMethod().equals(HttpMethod.POST)) {
                LOG.error("Got invalid HTTP method: expecting only POST");
                throw new BadRequestException("Incorrect method "
                        + request.getMethod().toString() + ", expected POST");
            }
            HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
            InterfaceHttpData data = decoder.getBodyHttpData(HTTP_TEST_ATTRIBUTE);
            if (data == null) {
                LOG.error("HTTP Resolve request inccorect, {} attribute not found", HTTP_TEST_ATTRIBUTE);
                throw new BadRequestException("HTTP Resolve request inccorect, " +
                        HTTP_TEST_ATTRIBUTE +" attribute not found");
            }
            Attribute attribute = (Attribute) data;
            requestData = attribute.get();
            LOG.trace("Name {}, type {} found, data size {}", data.getName(), data.getHttpDataType().name(), requestData.length);
        }
        
        public HttpResponse getHttpResponse() {
            return response;
        }
    }
    
    /**
     * Netty exception handler.
     * @author Andrey Panasenko
     *
     */
    public class DefaultExceptionHandler extends ChannelInboundHandlerAdapter {
        @Override
        public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
                throws Exception {
            LOG.error("Exception caught", cause);

            HttpResponseStatus status;
            if (cause instanceof BadRequestException) {
                status =  BAD_REQUEST;
            } else {
                status = INTERNAL_SERVER_ERROR;
            }

            String content = cause.getMessage();

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);

            ChannelFuture future = ctx.writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
            ctx.close();
        }        
    }
    
    /**
     * Test client.
     * Generate HTTP request.
     * Open URLConnection to Netty.
     * Return bytes[] from HTTP response body.
     * @author Andrey Panasenko
     *
     */
    public class HttpTestClient implements Runnable {

       /**
        * The pool of ASCII chars to be used for generating a multipart boundary.
        */
        private final char[] MULTIPART_CHARS =
                "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();
        /** boundary size */
        public static final int BOUNDARY_LENGTH = 35;
        
        /** ContentType constant string */
        public static final String CONTENT_TYPE_CONST = "multipart/form-data; boundary=";
        
        /** ContentDisposition constant string */
        public static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; ";
        
        /** Content name filed */ 
        public static final String CONTENT_NAME = "name=";
        
        /** CRLF */
        public static final String crlf = "\r\n";
        
        /** Generated boundary */
        private String boundary;
        
        /** Random number generator */
        private Random rnd = new Random();
        
        private HttpURLConnection connection;
        
        private byte[] object;
        private byte[] response;
        private boolean responseComplete = false;
        private Object sync = new Object();
        
        public HttpTestClient(String host, int port, byte[] object) throws MalformedURLException, IOException {
            String url = "http://"+host+":"+port+"/test";
            connection = (HttpURLConnection)new URL(url).openConnection();
            this.object = object;
            boundary = getRandomString(BOUNDARY_LENGTH);
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LOG.info("Run Http test to {}", connection.getURL().toString());
            List<Byte> bodyArray = new Vector<>();
            try {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", CONTENT_TYPE_CONST+boundary);
                
                DataOutputStream out =
                        new DataOutputStream(connection.getOutputStream());
                dumbObject(HTTP_TEST_ATTRIBUTE, object, out);
                out.flush();
                out.close();
                
                DataInputStream r = new DataInputStream(connection.getInputStream());
                while (true) {
                    bodyArray.add(new Byte(r.readByte()));
                }
            } catch (EOFException eof) {
                response = new byte[bodyArray.size()];
                for (int i = 0; i < response.length; i++) {
                    response[i] = bodyArray.get(i);
                }
            } catch (IOException e) {
                LOG.error("Error request HTTP to {}", connection.getURL().toString());
            } finally {
                connection.disconnect();
                synchronized (sync) {
                    responseComplete = true;
                    sync.notify();
                }
            }
        }
        
        /**
         * Return HTTP response body.
         * Method blocks, until body received.
         * @return byte[]
         */
        public byte[] getResponseBody() {
            synchronized (sync) {
                if (!responseComplete) {
                    try {
                        sync.wait();
                    } catch (InterruptedException e) {
                        LOG.error("Error wait ",e);
                    }
                }
            }
            return response;
        }
        
        /**
         * generate random String.
         * @param int size of String
         * @return random String
         */
        public String getRandomString(int size) {
            StringBuffer sb = new StringBuffer();
            for(int i=0; i < size; i++) {
                int j = rnd.nextInt(MULTIPART_CHARS.length);
                sb.append(MULTIPART_CHARS[j]);
            }
            return sb.toString();
        }
        
        /**
         * Write multipart entity
         * @param name multipart entity name
         * @param bytes multipart entity body
         * @param out DataOutputStream connection stream
         * @throws IOException throws in case ot write error.
         */
        public void dumbObject(String name, byte[] bytes, DataOutputStream out) throws IOException {

            out.writeBytes("--"+boundary+crlf);
            out.writeBytes(CONTENT_DISPOSITION+CONTENT_NAME+"\""+name+"\""+crlf);
            out.writeBytes("Content-Type: application/octet-stream"+crlf);
            out.writeBytes(crlf);
            out.write(bytes);
            out.writeBytes(crlf);
            out.writeBytes("--"+boundary+"--"+crlf);
        }
    }
    
    /**
     * Initialize Thread executor.
     * @throws InterruptedException
     */
    @BeforeClass
    public static void before() throws InterruptedException {
        executor = Executors.newFixedThreadPool(5);
    }
    
    /**
     * Shutdown Thread executor.
     * @throws InterruptedException
     */
    @AfterClass
    public static void after() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    
    /**
     * Start Netty server.
     */
    @Before
    public void beforeTest() {
        try {
            netty = new NettyStarter();
        } catch (InterruptedException e) {
            LOG.error("Netty start Error",e);
            fail(e.toString());
        }
        executor.execute(netty);
    }
    
    /**
     * Stop Netty server.
     */
    @After
    public void afterTest() {
        netty.shutdown();
        netty = null;
    }
    
    /**
     * Test which demonstrates Netty parsing bug.
     * In case of multipart POST entity ends with CR with odd number,
     * Netty return one more CR byte in multipart entity body.
     */
    @Test
    public void testOddCr() {
        LOG.info("Starting HTTP test");
        assertNotNull(netty);
        String host = "localhost";
        Random rnd = new Random();
        int arrayLength = 16;
        sendingArray = new byte[arrayLength];
        rnd.nextBytes(sendingArray);
        //sendingArray[arrayLength-5] = 0x0d;
        //sendingArray[arrayLength-4] = 0x0d;
        sendingArray[arrayLength-3] = 0x0d;
        sendingArray[arrayLength-2] = 0x0d;
        sendingArray[arrayLength-1] = 0x0d;
        try {
            LOG.info("Starting HTTP test to {}:{} ",host,HTTP_BIND_PORT);
            HttpTestClient test = new HttpTestClient(host, HTTP_BIND_PORT, sendingArray);
            executor.submit(test);
            byte[] response = test.getResponseBody();
            assertEquals(ByteBuffer.wrap(sendingArray), ByteBuffer.wrap(response));
            
        } catch (IOException e) {
            LOG.error("Error: ",e);
            fail(e.toString());
        }
        LOG.info("Test complete");
    }
    
    /**
     * Return hex String of bytes.
     * @param bytes
     * @param delim
     * @return
     */
    public static String bytesToString(byte[] bytes, String delim) {
        if (delim == null) {
            delim = ":";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int f = (bytes[i]&0xF0)>>4;
            int s = bytes[i]&0x0F;
            sb.append(hex[f]);
            sb.append(hex[s]);
            sb.append(delim);
        }
        return sb.toString();
    }
    
}
