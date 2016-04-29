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

package org.kaaproject.kaa.it.thrift.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.cli.client.BaseCliThriftClient;
import org.kaaproject.kaa.server.common.thrift.cli.client.CliSessionState;
import org.kaaproject.kaa.server.common.thrift.cli.client.OptionsProcessor;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CommandResult;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CommandStatus;
import org.kaaproject.kaa.server.common.thrift.gen.cli.MemoryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliThriftIT {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(CliThriftIT.class);
    
    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10090;
    
    private static final String THRIFT_SERVER_SHORT_NAME = "baseCliThriftService";
    
    /** The server. */
    private static TServer server;

    /** The thrift server thread. */
    private static Thread thriftServerThread;
    
    /** The thrift server started. */
    private static boolean thriftServerStarted = false;

    /** The CLI session. */
    private CliSessionState cliSession;
    
    /** The System output stream. */
    private ByteArrayOutputStream systemOut;
    
    /** The System error stream. */
    private ByteArrayOutputStream systemErr;
    
    /**
     * Inits the.
     * 
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
    }
    
    /**
     * After.
     * 
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void after() throws Exception {
        if (thriftServerStarted) {
            server.stop();
            thriftServerThread.join();
            thriftServerStarted = false;
        }
    }
    
    /**
     * Before test.
     *
     * @throws Exception the exception
     */
    @Before
    public void beforeTest() throws Exception {
        if (!thriftServerStarted) {
            CliThriftService.Processor<CliThriftService.Iface> cliProcessor = new CliThriftService.Processor<CliThriftService.Iface>(
                    new TestCliThriftService(THRIFT_SERVER_SHORT_NAME));
            TMultiplexedProcessor processor = new TMultiplexedProcessor();
            processor.registerProcessor(KaaThriftService.KAA_NODE_SERVICE.getServiceName(), cliProcessor);
            TServerTransport serverTransport = new TServerSocket(
                    new InetSocketAddress(HOST, PORT));
            server = new TThreadPoolServer(
                    new Args(serverTransport).processor(processor));
            thriftServerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    LOG.info("Thrift Server started.");
                    server.serve();
                    LOG.info("Thrift Server stopped.");
                }
            });

            thriftServerThread.start();

            Thread.sleep(100);

            thriftServerStarted = true;
        }
        cliSession = new CliSessionState();
        cliSession.in = System.in;
        
        systemOut = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(systemOut, true, "UTF-8");
        System.setOut(out);

        systemErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(systemErr, true, "UTF-8");
        System.setErr(err);

        cliSession.out = System.out;
        cliSession.err = System.err;
        
        CliSessionState.start(cliSession);
    }
    
    /**
     * Cli connect.
     *
     * @throws TException the t exception
     */
    private void cliConnect() throws TException {
        OptionsProcessor optionsProcessor = new OptionsProcessor();
        optionsProcessor.parse(new String[] {"-h", HOST, "-p", PORT+""});
        optionsProcessor.process(cliSession);
        cliSession.connect();
    }
    
    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        if (cliSession.isRemoteMode()) {
            cliSession.close();
        }
    }
    
    /**
     * Test get remote server name.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testRemoteServerName() throws TException {
        cliConnect();
        Assert.assertTrue(cliSession.isRemoteMode());
        Assert.assertEquals(cliSession.remoteServerName, THRIFT_SERVER_SHORT_NAME);
    }
    
    /**
     * Test get memory usage.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetMemoryUsage() throws TException {
        cliConnect();
        MemoryUsage memoryUsage = cliSession.getClient().getMemoryUsage(true);
        Assert.assertNotNull(memoryUsage);
        Assert.assertTrue(memoryUsage.getFree()>0);
        Assert.assertTrue(memoryUsage.getMax()>0);
        Assert.assertTrue(memoryUsage.getTotal()>0);
    }
    
    /**
     * Test execute unknown command.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testExecuteUnknownCommand() throws TException {
        cliConnect();
        CommandResult commandResult = cliSession.getClient().executeCommand("fakeCommand");
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(commandResult.getStatus(),CommandStatus.OK);
        Assert.assertTrue(commandResult.getMessage().startsWith("Error: unknown command 'fakeCommand'"));
    }
    
    /**
     * Test execute help command.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testExecuteHelpCommand() throws TException {
        cliConnect();
        CommandResult commandResult = cliSession.getClient().executeCommand("help");
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(commandResult.getStatus(),CommandStatus.OK);
        Assert.assertTrue(commandResult.getMessage().startsWith("Available commands:"));
    }
    
    /**
     * Test execute memory command.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testExecuteMemoryCommand() throws TException {
        cliConnect();
        CommandResult commandResult = cliSession.getClient().executeCommand("memory");
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(commandResult.getStatus(),CommandStatus.OK);
        Assert.assertTrue(commandResult.getMessage().startsWith("Memory Usage:"));
    }
    
    /**
     * Test execute threads command.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testExecuteThreadsCommand() throws TException {
        cliConnect();
        CommandResult commandResult = cliSession.getClient().executeCommand("threads");
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(commandResult.getStatus(),CommandStatus.OK);
        Assert.assertTrue(commandResult.getMessage().startsWith("THREADS DUMP:"));
    }
    
    /**
     * Test execute unknown command from cli.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteUnknownCommandFromCli() throws TException, UnsupportedEncodingException {
        cliConnect();
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("fakeCommand");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Error: unknown command 'fakeCommand'"));
    }
    
    /**
     * Test execute help command from cli.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteHelpCommandFromCli() throws TException, UnsupportedEncodingException {
        cliConnect();
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("help");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Available commands:"));
    }
    
    /**
     * Test execute memory command from cli.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteMemoryCommandFromCli() throws TException, UnsupportedEncodingException {
        cliConnect();
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("memory -gc");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Memory Usage:"));
    }
    
    /**
     * Test execute threads command from cli.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteThreadsCommandFromCli() throws TException, UnsupportedEncodingException {
        cliConnect();
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("threads");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("THREADS DUMP:"));
    }
    
    /**
     * Test execute disconnect command from cli.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteDisconnectCommandFromCli() throws TException, UnsupportedEncodingException {
        cliConnect();
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("disconnect");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertFalse(cliSession.isRemoteMode());
    }
    
    /**
     * Test execute disconnect command when not connected.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteDisconnectCommandWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("disconnect");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.startsWith("Not connected!"));
        Assert.assertFalse(cliSession.isRemoteMode());
    }
    
    /**
     * Test execute connect command when not connected.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteConnectCommandWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("connect " + HOST + ":" + PORT);
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.trim().isEmpty());
        Assert.assertTrue(cliSession.isRemoteMode());
    }
    
    /**
     * Test execute connect command to not available server when not connected.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteConnectCommandToNotAvailableServerWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("connect " + HOST + ":" + (PORT+10));
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.startsWith("[Thrift Error]: "));
        Assert.assertFalse(cliSession.isRemoteMode());
    }
    
    /**
     * Test execute connect with invalid arguments.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteConnectWithInvalidArgumentsFromCli() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("connect ");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Unable to parse arguments."));
        Assert.assertTrue(error.trim().isEmpty());
        Assert.assertFalse(cliSession.isRemoteMode());
    }
    
    /**
     * Test execute unknown command from cli in local mode.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteUnknownCommandFromCliLocal() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("fakeCommand");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Unknown command 'fakeCommand'"));
    }
    
    /**
     * Test execute help command in local mode.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteHelpCommandFromCliLocal() throws TException, UnsupportedEncodingException {
        BaseCliThriftClient cli = new BaseCliThriftClient();
        int result = cli.processLine("help");
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Available commands: "));
        Assert.assertTrue(error.trim().isEmpty());
    }
    
    /**
     * Test execute help command without session.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteHelpCommandWithoutSession() throws TException, UnsupportedEncodingException {
        OptionsProcessor optionsProcessor = new OptionsProcessor();
        optionsProcessor.parse(new String[] {"-H"});
        boolean result = optionsProcessor.process(cliSession);
        Assert.assertFalse(result);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.startsWith("usage: thriftCli"));
    }
    
    /**
     * Test execute option command without session.
     * 
     * @throws TException
     *             the t exception
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testExecuteUmknownCommandWithoutSession() throws TException, UnsupportedEncodingException {
        OptionsProcessor optionsProcessor = new OptionsProcessor();
        boolean result = optionsProcessor.parse(new String[] {"--unknown"});
        Assert.assertFalse(result);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.startsWith("usage: thriftCli"));
        Assert.assertTrue(error.trim().equals("Unrecognized option: --unknown"));
    }
    
    
}
