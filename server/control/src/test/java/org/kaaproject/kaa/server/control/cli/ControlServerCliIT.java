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

package org.kaaproject.kaa.server.control.cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDataLoader;
import org.kaaproject.kaa.server.control.TestCluster;
import org.kaaproject.kaa.server.control.cli.ControlApiCliThriftClient;
import org.kaaproject.kaa.server.control.cli.ControlClientSessionState;
import org.kaaproject.kaa.server.control.cli.ControlOptionsProcessor;
import org.kaaproject.kaa.server.control.cli.ControlApiCommandProcessor.EntityType;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The Class ControlServerCliIT.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ControlServerCliIT {

    /** The Constant LENGTH_OF_ID_IN_MONGODB. */
    private static final int LENGTH_OF_ID_IN_MONGODB = 24;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerCliIT.class);
    
    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10090;

    /** The Constant FAKE_ID. */
    private static final String FAKE_ID = RandomStringUtils.randomNumeric(24);

    /** The control service. */
    @Autowired
    private ControlService controlService;

    /** The Control Client CLI session. */
    private ControlClientSessionState controlClientSession;
    
    /** The System output stream. */
    private ByteArrayOutputStream systemOut;
    
    /** The System error stream. */
    private ByteArrayOutputStream systemErr;
    
    /** The System output stream. */
    private ByteArrayOutputStream cliOut;
    
    /** The System error stream. */
    private ByteArrayOutputStream cliErr;
    
    // DATA dir :         
    // String userDir = System.getProperty("user.dir");
    // userDir + "/target/test-classes/data"
    
    /**
     * Inits the.
     * 
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }
    
    /**
     * After.
     * 
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void after() throws Exception {
        TestCluster.stop();
        MongoDBTestRunner.tearDown();
    }
    
    /**
     * Before test.
     *
     * @throws Exception the exception
     */
    @Before
    public void beforeTest() throws Exception {
        MongoDataLoader.loadData();
        
        TestCluster.checkStarted(controlService);

        controlClientSession = new ControlClientSessionState();
        controlClientSession.in = System.in;
        
        systemOut = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(systemOut, true, "UTF-8");
        System.setOut(out);

        systemErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(systemErr, true, "UTF-8");
        System.setErr(err);
        
        cliOut = new ByteArrayOutputStream();
        controlClientSession.out = new PrintStream(cliOut, true, "UTF-8");
        
        cliErr = new ByteArrayOutputStream();
        controlClientSession.err = new PrintStream(cliErr, true, "UTF-8");
        
        ControlClientSessionState.start(controlClientSession);
    }
    
    /**
     * Connect to control CLI.
     *
     * @throws TException the t exception
     */
    private void controlClientConnect() throws TException {
        ControlOptionsProcessor optionsProcessor = new ControlOptionsProcessor();
        optionsProcessor.parse(new String[] {"-h", HOST, "-p", PORT+""});
        optionsProcessor.process(controlClientSession);
        controlClientSession.connect();
    }
    
    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        if (controlClientSession.isRemoteMode()) {
            controlClientSession.close();
        }
    }
    
    /**
     * Test connect to remote server.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testConnect() throws TException {
        controlClientConnect();
        Assert.assertTrue(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute disconnect command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteDisconnectCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("disconnect");
        Assert.assertEquals(result, 0);
//        String output = systemOut.toString("UTF-8");
//        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertFalse(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute disconnect command when not connected.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteDisconnectCommandWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("disconnect");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String error = cliErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.startsWith("Not connected!"));
        Assert.assertFalse(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute connect command when not connected.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteConnectCommandWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("connect " + HOST + ":" + PORT);
        Assert.assertEquals(result, 0);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.trim().isEmpty());
        Assert.assertTrue(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute connect command to not available server when not connected.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteConnectCommandToNotAvailableServerWhenDisconnectedFromCli() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("connect " + HOST + ":" + (PORT+10));
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String error = cliErr.toString("UTF-8");
        Assert.assertTrue(output.trim().isEmpty());
        Assert.assertTrue(error.startsWith("[Thrift Error]: "));
        Assert.assertFalse(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute connect with invalid arguments.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteConnectWithInvalidArgumentsFromCli() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("connect ");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String error = cliErr.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Unable to parse arguments."));
        Assert.assertTrue(error.trim().isEmpty());
        Assert.assertFalse(controlClientSession.isRemoteMode());
    }
    
    /**
     * Test execute unknown command from cli in local mode.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteUnknownCommandFromCliLocal() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("fakeCommand");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Unknown command 'fakeCommand'"));
    }
    
    /**
     * Test execute help command in local mode.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteHelpCommandFromCliLocal() throws TException, UnsupportedEncodingException {
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("help");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String error = cliErr.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Available commands: "));
        Assert.assertTrue(error.trim().isEmpty());
    }
    
    /**
     * Test execute help command without session.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteHelpCommandWithoutSession() throws TException, UnsupportedEncodingException {
        ControlOptionsProcessor optionsProcessor = new ControlOptionsProcessor();
        optionsProcessor.parse(new String[] {"-H"});
        boolean result = optionsProcessor.process(controlClientSession);
        Assert.assertFalse(result);
        String output = systemOut.toString("UTF-8");
        Assert.assertTrue(output.startsWith("usage: thriftCli"));
    }
    
    /**
     * Test execute command with unknown option without session.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteUnknownCommandWithoutSession() throws TException, UnsupportedEncodingException {
        ControlOptionsProcessor optionsProcessor = new ControlOptionsProcessor();
        boolean result = optionsProcessor.parse(new String[] {"--unknown"});
        Assert.assertFalse(result);
        String output = systemOut.toString("UTF-8");
        String error = systemErr.toString("UTF-8");
        Assert.assertTrue(output.startsWith("usage: thriftCli"));
        Assert.assertTrue(error.trim().equals("Unrecognized option: --unknown"));
    }
    
    /**
     * Test execute help command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteHelpCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("help");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Available Control Server API commands:"));
    }
    
    /**
     * Test execute unknown command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteUnknownCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("fakeCommand");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Error: unknown command 'fakeCommand'"));
    }
    
    /**
     * Test show help for command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testShowHelpCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("listTenant -h");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("listTenant - list Tenants"));
    }
    
    /**
     * Test execute command with invalid arguments from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteCommandWithInvalidArgsFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        int result = cli.processLine("createTenant -a");
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Unable to parse command arguments"));
    }
    
    /**
     * Test execute tenants command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteTenantsCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String tenantId = editTenantCli(cli, null, true);
        tenantId = editTenantCli(cli, null, false);
        Assert.assertFalse(strIsEmpty(tenantId));
        Assert.assertEquals(tenantId.length(),24);
        editTenantCli(cli, tenantId, false);
        editTenantCli(cli, FAKE_ID, false);
        showEntityCli(cli, tenantId, EntityType.TENANT);
        listTenantsCli(cli, false);
        listTenantsCli(cli, true);
        deleteEntityCli(cli, tenantId, EntityType.TENANT);
    }
    
    /**
     * Test execute users command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteUsersCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String userId = editUserCli(cli, null, true);
        userId = editUserCli(cli, null, false);
        Assert.assertFalse(strIsEmpty(userId));
        Assert.assertEquals(userId.length(),24);
        editUserCli(cli, userId, false);
        editUserCli(cli, FAKE_ID, false);
        showEntityCli(cli, userId, EntityType.USER);
        listUsersCli(cli, false);
        listUsersCli(cli, true);
        deleteEntityCli(cli, userId, EntityType.USER);
    }
    
    /**
     * Test execute applications command from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteApplicationsCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String tenantId = editTenantCli(cli, null, false);
        String applicationId = editApplicationCli(cli, null, tenantId, true);
        applicationId = editApplicationCli(cli, null, tenantId, false);
        Assert.assertFalse(strIsEmpty(applicationId));
        Assert.assertEquals(applicationId.length(),24);
        editApplicationCli(cli, applicationId, tenantId, false);
        editApplicationCli(cli, FAKE_ID, tenantId, false);
        showEntityCli(cli, applicationId, EntityType.APPLICATION);
        listApplicationsCli(cli, tenantId, false);
        listApplicationsCli(cli, tenantId, true);
        deleteEntityCli(cli, applicationId, EntityType.APPLICATION);
    }
    
    /**
     * Test execute profile schema commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteProfileSchemaCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String profileSchemaId = editProfileSchemaCli(cli, null, applicationId, true);
        profileSchemaId = editProfileSchemaCli(cli, null, applicationId, false);
        Assert.assertFalse(strIsEmpty(profileSchemaId));
        Assert.assertEquals(profileSchemaId.length(),24);
        editProfileSchemaCli(cli, profileSchemaId, applicationId, false);
        editProfileSchemaCli(cli, FAKE_ID, applicationId, false);
        showEntityCli(cli, profileSchemaId, EntityType.PROFILE_SCHEMA);
        listProfileSchemasCli(cli, applicationId, false);
        listProfileSchemasCli(cli, applicationId, true);
    }
    
    /**
     * Test execute configuration schema commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteConfigSchemaCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String configSchemaId = editConfigSchemaCli(cli, null, applicationId, true);
        configSchemaId = editConfigSchemaCli(cli, null, applicationId, false);
        Assert.assertFalse(strIsEmpty(configSchemaId));
        Assert.assertEquals(configSchemaId.length(),24);
        editConfigSchemaCli(cli, configSchemaId, applicationId, false);
        editConfigSchemaCli(cli, FAKE_ID, applicationId, false);
        showEntityCli(cli, configSchemaId, EntityType.CONFIGURATION_SCHEMA);
        listConfigSchemasCli(cli, applicationId, false);
        listConfigSchemasCli(cli, applicationId, true);
    }    
    
    /**
     * Test execute endpoint group commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteEndpointGroupCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "10", true);
        endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "15", false);
        Assert.assertFalse(strIsEmpty(endpointGroupId));
        Assert.assertEquals(LENGTH_OF_ID_IN_MONGODB, endpointGroupId.length());
        editEndpointGroupCli(cli, endpointGroupId, applicationId, "17", false);
        editEndpointGroupCli(cli, FAKE_ID, applicationId, "19", false);
        showEntityCli(cli, endpointGroupId, EntityType.ENDPOINT_GROUP);
        listEndpointGroupsCli(cli, applicationId, false);
        listEndpointGroupsCli(cli, applicationId, true);
        deleteEntityCli(cli, endpointGroupId, EntityType.ENDPOINT_GROUP);
    }

    /**
     * Test execute topic commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteTopicCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        
        String topicId = editTopicCli(cli, null, applicationId, SubscriptionType.MANDATORY, true);
        topicId = editTopicCli(cli, null, applicationId, SubscriptionType.MANDATORY, false);
        Assert.assertFalse(strIsEmpty(topicId));
        Assert.assertEquals(LENGTH_OF_ID_IN_MONGODB, topicId.length());
        editTopicCli(cli, topicId, applicationId, SubscriptionType.MANDATORY, false);
        editTopicCli(cli, FAKE_ID, applicationId, SubscriptionType.MANDATORY, false);
        showEntityCli(cli, topicId, EntityType.TOPIC);
        listTopicsCli(cli, applicationId, false);
        listTopicsCli(cli, applicationId, true);
        deleteEntityCli(cli, topicId, EntityType.TOPIC);
    }
    
    /**
     * Test execute profile schema commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteNotificationSchemaCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String notificationSchemaId = editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.SYSTEM, true, true);
        notificationSchemaId = editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.SYSTEM, false, false);
        Assert.assertFalse(strIsEmpty(notificationSchemaId));
        Assert.assertEquals(LENGTH_OF_ID_IN_MONGODB, notificationSchemaId.length());
        editNotificationSchemaCli(cli, notificationSchemaId, applicationId, NotificationTypeDto.SYSTEM, false, false);
        editNotificationSchemaCli(cli, FAKE_ID, applicationId, NotificationTypeDto.SYSTEM, false, false);
        showEntityCli(cli, notificationSchemaId, EntityType.NOTIFICATION_SCHEMA);
        listNotificationSchemasCli(cli, applicationId, false);
        listNotificationSchemasCli(cli, applicationId, true);
    }
    
    /**
     * Test execute notification commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteNotificationCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String notificationSchemaId = editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.SYSTEM, false, false);
        String topicId = editTopicCli(cli, null, applicationId, SubscriptionType.MANDATORY, false);
        notificationSchemaId = createNotificationCli(cli, notificationSchemaId, topicId, 0, false, false);
        Assert.assertFalse(strIsEmpty(notificationSchemaId));
        Assert.assertEquals(LENGTH_OF_ID_IN_MONGODB + 2, notificationSchemaId.length());
        showEntityCli(cli, notificationSchemaId, EntityType.NOTIFICATION);
        listNotificationsCli(cli, topicId, false);
        listNotificationsCli(cli, topicId, true);
    }     
    
    /**
     * Test execute profile filter commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteProfileFilterCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String profileFilterId = editProfileFilterCli(cli, null, null, null, false);
        String endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
        String profileSchemaId = editProfileSchemaCli(cli, null, applicationId, false);
        profileFilterId = editProfileFilterCli(cli, null, profileSchemaId, endpointGroupId, true);
        Assert.assertFalse(strIsEmpty(profileFilterId));
        Assert.assertEquals(profileFilterId.length(),24);
        editProfileFilterCli(cli, profileFilterId, profileSchemaId, endpointGroupId, false);
        editProfileFilterCli(cli, FAKE_ID, profileSchemaId, endpointGroupId, false);
        showEntityCli(cli, profileFilterId, EntityType.PROFILE_FILTER);
        activateProfileFilterCli(cli, profileFilterId);
    }         
    
    /**
     * Test execute configuration commands from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteConfigurationCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String configurationId = editConfigurationCli(cli, null, null, null, false);
        String endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
        String configSchemaId = editConfigSchemaCli(cli, null, applicationId, false);
        configurationId = editConfigurationCli(cli, null, configSchemaId, endpointGroupId, true);
        Assert.assertFalse(strIsEmpty(configurationId));
        Assert.assertEquals(configurationId.length(),24);
        editConfigurationCli(cli, configurationId, configSchemaId, endpointGroupId, false);
        editConfigurationCli(cli, FAKE_ID, configSchemaId, endpointGroupId, false);
        showEntityCli(cli, configurationId, EntityType.CONFIGURATION);
        activateConfigurationCli(cli, configurationId);
    }         
    
    /**
     * Test execute generate SDK conmmand from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteGenerateSdkCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        editProfileSchemaCli(cli, null, applicationId, false);
        editConfigSchemaCli(cli, null, applicationId, false);
        editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.USER, false, false);
        boolean result = generateSdkCli(cli, applicationId, 1, 1, 2);
        Assert.assertTrue(result);
    }

    /**
     * Test execute add topic to endpoint group from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteAddTopicToEndpointGroupCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String groupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
        String topicId = editTopicCli(cli,null, applicationId,SubscriptionType.MANDATORY, false);
        String output = editTopicInEndpointGroupCli(cli, groupId, topicId, false, false);
        Assert.assertTrue(output.trim().startsWith("Topic was added to Endpoint Group"));
    }
    
    /**
     * Test execute commands with invalid args from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteInvalidCommandsFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();

        testInvalidCommand(cli, "createEndpointGroup -n testEndpointGroup -a 1 -w a", "Unable to parse weight option!");

        testInvalidCommand(cli, "createTopic -a fake -n topic -t mandatory", "Invalid application id for topic.");

        testInvalidCommand(cli, "addTopicToEndpointGroup -i fake -t fake", "Incorrect endpoint group id.");
        testInvalidCommand(cli, "removeTopicFromEndpointGroup -i fake -t fake", "Incorrect endpoint group id.");
        testInvalidCommand(cli, "addTopicToEndpointGroup -i " + FAKE_ID + " -t fake", "Incorrect topic id.");
        testInvalidCommand(cli, "removeTopicFromEndpointGroup -i " + FAKE_ID + " -t fake", "Incorrect topic id.");
        
        testInvalidCommand(cli, "createNotification -s fake -t fake", "Invalid schema id for notification.");
        testInvalidCommand(cli, "createNotification -s " + FAKE_ID + " -t fake", "Invalid topic id for notification.");
        testInvalidCommand(cli, "createNotification -s " + FAKE_ID + " -t " + FAKE_ID + " -l a", "Incorrect format of ttl:");
        testInvalidCommand(cli, "createNotification -s " + FAKE_ID + " -t " + FAKE_ID + " -l 10", "Need to set body or file with body for notification");
        testInvalidCommand(cli, "createNotification -s " + FAKE_ID + " -t " + FAKE_ID + " -l 10 -f fake", "Can't read file. Please check file name.");

        testInvalidCommand(cli, "createUnicastNotification -t fake -s fake -k fake", "Invalid topic id for notification.");
        testInvalidCommand(cli, "createUnicastNotification -t " + FAKE_ID + " -s fake -k fake", "Invalid schema id for notification.");
        testInvalidCommand(cli, "createUnicastNotification -t " + FAKE_ID + " -s" + FAKE_ID + " -k ''", "Empty key hash for unicast notification.");
        testInvalidCommand(cli, "createUnicastNotification -t " + FAKE_ID + " -s" + FAKE_ID + " -k keyhash -l a", "Incorrect format of ttl:");
        testInvalidCommand(cli, "createUnicastNotification -t " + FAKE_ID + " -s" + FAKE_ID + " -k keyhash -l 10", "Need to set body or file with body for notification");
        testInvalidCommand(cli, "createUnicastNotification -t " + FAKE_ID + " -s" + FAKE_ID + " -k keyhash -l 10 -f fake", "Can't read file. Please check file name.");

        testInvalidCommand(cli, "createNotificationSchema -a fake -t fake", "Invalid application id for notification.");
        testInvalidCommand(cli, "createNotificationSchema -a " + FAKE_ID + " -t ''", "Empty type of notification");
        testInvalidCommand(cli, "createNotificationSchema -a " + FAKE_ID + " -t fake", "Incorrect type of notification");
        testInvalidCommand(cli, "createNotificationSchema -a " + FAKE_ID + " -t USER", "Need to set body or file with body for notification schema");
        testInvalidCommand(cli, "createNotificationSchema -a " + FAKE_ID + " -t USER -f fake", "Can't read file. Please check file name.");

        testInvalidCommand(cli, "deleteConfigurationSchema -i " + FAKE_ID, "Command not supported!");
    }
    
    private void testInvalidCommand(ControlApiCliThriftClient cli, String cmdLine, String expectedMessage) throws UnsupportedEncodingException {
        cliOut.reset();
        cliErr.reset();
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String errorOut = cliErr.toString("UTF-8");
        Assert.assertTrue(errorOut.trim().contains(expectedMessage));
    }

    /**
     * Test execute create notification from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteCreateUnicastNotificationFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String schemaId = editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.SYSTEM, false, false);
        //look into mongo.data
        String notificationId = createUnicastNotificationCli(cli, schemaId, "530db773687f16fec3527354", "ZThNRW56Wm9GeU1tRDdXU0hkTnJGSnlFazhNPQ==", false, false);
        Assert.assertTrue(ObjectId.isValid(notificationId.trim()));
        notificationId = createUnicastNotificationCli(cli, schemaId, "530db773687f16fec3527354", "ZThNRW56Wm9GeU1tRDdXU0hkTnJGSnlFazhNPQ==", true, false);
        Assert.assertTrue(ObjectId.isValid(notificationId.trim()));
    }

    /**
     * Test execute create unicast notification from cli.
     *
     * @throws TException the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteCreateNotificationFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String schemaId = editNotificationSchemaCli(cli, null, applicationId, NotificationTypeDto.SYSTEM, false, false);
        String topicId = editTopicCli(cli, null, applicationId, SubscriptionType.MANDATORY, false);
        String notificationId = createNotificationCli(cli, schemaId, topicId, 220, true, false);
        Assert.assertTrue(StringUtils.isNotBlank(notificationId));
    }


    /**
     * Test execute remove topic from endpoint group from cli.
     *
     * @throws TException             the t exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @Test
    public void testExecuteRemoveTopicFromEndpointGroupCommandFromCli() throws TException, UnsupportedEncodingException {
        controlClientConnect();
        ControlApiCliThriftClient cli = new ControlApiCliThriftClient();
        String applicationId = editApplicationCli(cli, null, null, false);
        String groupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
        String topicId = editTopicCli(cli, null, applicationId, SubscriptionType.MANDATORY, false);
        editTopicInEndpointGroupCli(cli, groupId, topicId, false, false);
        String output = editTopicInEndpointGroupCli(cli, groupId, topicId, false, true);
        Assert.assertTrue(output.trim().startsWith("Topic was removed from Endpoint Group"));
    }


    /**
     * Edits the topic in endpoint group cli.
     *
     * @param cli the cli
     * @param groupId the group id
     * @param topicId the topic id
     * @param createOut the create out
     * @param isRemove the is remove
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editTopicInEndpointGroupCli(ControlApiCliThriftClient cli, String groupId, String topicId, boolean createOut, boolean isRemove) throws UnsupportedEncodingException {
        cliOut.reset();
        StringBuilder commandBuilder = new StringBuilder();
        if (isRemove) {
            commandBuilder.append("removeTopicFromEndpointGroup");
        } else {
            commandBuilder.append("addTopicToEndpointGroup");
        }
        commandBuilder.append(" -i ").append(groupId).append(" -t ").append(topicId);
        if (createOut) {
            commandBuilder.append(" -o dummy.obj");
        }
        int result = cli.processLine(commandBuilder.toString());
        Assert.assertEquals(result, 0);
        return cliOut.toString("UTF-8");
    }
    
    /**
     * Shows the entity from cli.
     *
     * @param cli the control cli client
     * @param entityId the entity id
     * @param type the entity type
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void showEntityCli(ControlApiCliThriftClient cli, String entityId, EntityType type) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = cli.processLine("show" + type.getName() + " -i " + entityId);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Found " + type.getName() + ":"));
    }
    
    /**
     * Deletes the entity from cli.
     *
     * @param cli the control cli client
     * @param entityId the entity id
     * @param type the entity type
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void deleteEntityCli(ControlApiCliThriftClient cli, String entityId, EntityType type) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = cli.processLine("delete" + type.getName() + " -i " + entityId);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Deleted " + type.getName() + " with id: " + entityId));
    }
    
    /**
     * Edits/Creates the tenant from cli.
     *
     * @param cli the control cli client
     * @param tenantId the tenant id (if null new tenant will be created)
     * @param createOut create output file with object id
     * @return the tenantId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editTenantCli(ControlApiCliThriftClient cli, String tenantId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(tenantId);
        String cmdLine = (create ? "create" : "edit") + "Tenant -n testTenant" + (create ? "" : (" -i " + tenantId));
        if (createOut) {
            cmdLine += " -o dummy.out";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new tenant with id: ".length()).trim();
            return id;
        }
        else if (tenantId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Tenant with id " + FAKE_ID + " not found!"));
            return tenantId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Tenant updated."));
            return tenantId;
        }
    }
    
    /**
     * Lists the tenants from cli.
     *
     * @param cli the control cli client
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listTenantsCli(ControlApiCliThriftClient cli, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listTenant";
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Tenants:"));
    }
    
    /**
     * Edits/Creates the user from cli.
     *
     * @param cli the control cli client
     * @param userId the user id (if null new user will be created)
     * @param createOut create output file with object id
     * @return the userId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editUserCli(ControlApiCliThriftClient cli, String userId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(userId);
        int result = -1;
        if (create) {
            String tenantId = editTenantCli(cli, null, false);
            cliOut.reset();
            String cmdLine = "createUser -uid 23894729 -t " + tenantId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editUser -uid 3664353 -i " + userId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new user with id: ".length()).trim();
            return id;
        }
        else if (userId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("User with id " + FAKE_ID + " not found!"));
            return userId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("User updated."));
            return userId;
        }
    }
    
    /**
     * Lists the users from cli.
     *
     * @param cli the control cli client
     * @param createOut create output file with object id  
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listUsersCli(ControlApiCliThriftClient cli, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listUser";
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Users:"));
    }
    
    /**
     * Edits/Creates the application from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application id (if null new application will be created)
     * @param tenantId the tenant Id
     * @param createOut create output file with object id
     * @return the applicationId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editApplicationCli(ControlApiCliThriftClient cli, String applicationId, String tenantId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(applicationId);
        int result = -1;
        if (create) {
            if (strIsEmpty(tenantId)) {
                tenantId = editTenantCli(cli, null, false);
                cliOut.reset();
            }
            String cmdLine = "createApplication -n testApplication -t " + tenantId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editApplication -n testApplication2 -i " + applicationId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new application with id: ".length()).trim();
            return id;
        }
        else if (applicationId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Application with id " + FAKE_ID + " not found!"));
            return applicationId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Application updated."));
            return applicationId;
        }
    }
    
    /**
     * Lists the applications from cli.
     *
     * @param cli the control cli client
     * @param tenantId the tenant Id
     * @param createOut create output file with object id 
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listApplicationsCli(ControlApiCliThriftClient cli, String tenantId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listApplications -t " + tenantId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Applications:"));
    }
    
    /**
     * Edits/Creates the notification schema from cli.
     *
     * @param cli the control cli client
     * @param notificationSchemaId the notification schema id
     * @param applicationId the application Id
     * @param notificationType the notification type
     * @param createOut create output file with object id
     * @return the profileSchemaId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editNotificationSchemaCli(ControlApiCliThriftClient cli, String notificationSchemaId, String applicationId, NotificationTypeDto notificationType, boolean useBody, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(notificationSchemaId);
        int result = -1;
        if (create) {
            if (strIsEmpty(applicationId)) {
                applicationId = editApplicationCli(cli, null, null, false);
                cliOut.reset();
            }
            String schemaFile = notificationType==NotificationTypeDto.SYSTEM ? "testSystemNotificationSchema.json" : "testUserNotificationSchema.json";
            String cmdLine = "createNotificationSchema -a " + applicationId + " -t " + notificationType.name() + (useBody ? (" -b " + getTestFileContent(schemaFile)) : (" -f " + getTestFile(schemaFile)));
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editNotificationSchema -f " + getTestFile(notificationType==NotificationTypeDto.SYSTEM ? "testSystemNotificationSchema.json" : "testUserNotificationSchema.json") + " -i " + notificationSchemaId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Notification Schema with id: ".length()).trim();
            return id;
        }
        else if (notificationSchemaId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Notification Schema with id " + FAKE_ID + " not found!"));
            return notificationSchemaId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Notification Schema updated."));
            return notificationSchemaId;
        }
    }

    /**
     * Create the unicast notification from cli.
     *
     * @param cli       the control cli client
     * @param schemaId  the notification schema id
     * @param keyHashBase64 the key hash
     * @param createOut create output file with object id
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String createUnicastNotificationCli(ControlApiCliThriftClient cli, String schemaId, String topicId, String keyHashBase64, boolean useBody, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = -1;
        StringBuilder command = new StringBuilder();
        command.append("createUnicastNotification");
        if (useBody) {
            command.append(" -b ").append("'").append(getTestFileContent("testSystemNotification.json")).append("'");
        }
        else {
            command.append(" -f ").append(getTestFile("testSystemNotification.json"));
        }
        command.append(" -s ").append(schemaId).append(" -t ").append(topicId);
        if (createOut) {
            command.append(" -o dummy.out");
        }
        command.append(" -k ");
        command.append(keyHashBase64);
        command.append(" -l ");
        command.append("120");
        result = cli.processLine(command.toString());
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String id = output.trim().substring("Created new Unicast Notification with id: ".length()).trim();
        return id;
    }

    /**
     * Create the notification from cli.
     *
     * @param cli the control cli client
     * @param notificationSchemaId the notification schema id
     * @param topicId the topic id
     * @param ttl time to live for notification
     * @param createOut create output file with object id
     * @return the profileSchemaId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String createNotificationCli(ControlApiCliThriftClient cli, String notificationSchemaId, String topicId, int ttl, boolean sendBody, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = -1;
        String cmdLine = "createNotification  -s " + notificationSchemaId + " -t " + topicId + (sendBody ? (" -b '" + getTestFileContent("testSystemNotification.json")+"'") : (" -f " + getTestFile("testSystemNotification.json")));
        if (createOut) {
            cmdLine += " -o dummy.out";
        }
        if (ttl != 0) {
            cmdLine += " -l " + ttl;
        }
        result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        String id = output.trim().substring("Created new Notification with id: ".length()).trim();
        return id;
    }
    
    
    /**
     * Edits/Creates the profile schema from cli.
     *
     * @param cli the control cli client
     * @param profileSchemaId the profile schema id (if null new profile schema will be created)
     * @param applicationId the application Id
     * @param createOut create output file with object id     
     * @return the profileSchemaId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editProfileSchemaCli(ControlApiCliThriftClient cli, String profileSchemaId, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(profileSchemaId);
        int result = -1;
        if (create) {
            if (strIsEmpty(applicationId)) {
                applicationId = editApplicationCli(cli, null, null, false);
                cliOut.reset();
            }
            String cmdLine = "createProfileSchema -f " + getTestFile("testProfileSchema.json") + " -a " + applicationId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            else {
                cmdLine += " -vo dummy.ver";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editProfileSchema -f " + getTestFile("testProfileSchemaUpdated.json") + " -i " + profileSchemaId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Profile Schema with id: ".length()).trim();
            return id;
        }
        else if (profileSchemaId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Profile Schema with id " + FAKE_ID + " not found!"));
            return profileSchemaId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Profile Schema updated."));
            return profileSchemaId;
        }
    }
    
    /**
     * Lists the notification schemas from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application Id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listNotificationSchemasCli(ControlApiCliThriftClient cli, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listNotificationSchemas -a " + applicationId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Notification schemas:"));
    }
    
    /**
     * Lists the notification schemas from cli.
     *
     * @param cli the control cli client
     * @param topicId the topic id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listNotificationsCli(ControlApiCliThriftClient cli, String topicId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listNotifications -t " + topicId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Notifications:"));
    }      
    
    /**
     * Lists the profile schemas from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application Id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listProfileSchemasCli(ControlApiCliThriftClient cli, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listProfileSchemas -a " + applicationId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Profile Schemas:"));
    }
    
    /**
     * Edits/Creates the configuration schema from cli.
     *
     * @param cli the control cli client
     * @param configSchemaId the configuration schema id (if null new configuration schema will be created)
     * @param applicationId the application Id
     * @param createOut create output file with object id     
     * @return the configSchemaId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editConfigSchemaCli(ControlApiCliThriftClient cli, String configSchemaId, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(configSchemaId);
        int result = -1;
        if (create) {
            if (strIsEmpty(applicationId)) {
                applicationId = editApplicationCli(cli, null, null, false);
                cliOut.reset();
            }
            String cmdLine = "createConfigurationSchema -f " + getTestFile("testConfigSchema.json") + " -a " + applicationId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            else {
                cmdLine += " -vo dummy.ver";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editConfigurationSchema -f " + getTestFile("testConfigSchemaUpdated.json") + " -i " + configSchemaId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Configuration Schema with id: ".length()).trim();
            return id;
        }
        else if (configSchemaId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Configuration Schema with id " + FAKE_ID + " not found!"));
            return configSchemaId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Configuration Schema updated."));
            return configSchemaId;
        }
    }
    
    /**
     * Lists the configuration schemas from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application Id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listConfigSchemasCli(ControlApiCliThriftClient cli, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listConfigurationSchemas -a " + applicationId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Configuration Schemas:"));
    }
    
    /**
     * Edits/Creates the endpoint group from cli.
     *
     * @param cli the control cli client
     * @param endpointGroupId the endpoint group id (if null new endpoint group will be created)
     * @param applicationId the application Ids
     * @param createOut create output file with object id     
     * @return the endpointGroupId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editEndpointGroupCli(ControlApiCliThriftClient cli, String endpointGroupId, String applicationId, String weight, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(endpointGroupId);
        int result = -1;
        if (create) {
            if (strIsEmpty(applicationId)) {
                applicationId = editApplicationCli(cli, null, null, false);
                cliOut.reset();
            }
            String cmdLine = "createEndpointGroup -n testEndpointGroup -a " + applicationId + " -w " + weight;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editEndpointGroup -n testEndpointGroup2 -i " + endpointGroupId + " -w " + weight);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Endpoint Group with id: ".length()).trim();
            return id;
        }
        else if (endpointGroupId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Endpoint Group with id " + FAKE_ID + " not found!"));
            return endpointGroupId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Endpoint Group updated."));
            return endpointGroupId;
        }
    }
    
    /**
     * Lists the endpoint groups from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application Id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listEndpointGroupsCli(ControlApiCliThriftClient cli, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listEndpointGroups -a " + applicationId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Endpoint Groups:"));
    }
    
    /**
     * Lists the topics from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application Id
     * @param createOut create output file with object id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void listTopicsCli(ControlApiCliThriftClient cli, String applicationId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        String cmdLine = "listTopics -a " + applicationId;
        if (createOut) {
            cmdLine += " -o dummy.obj";
        }
        int result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("List of Topics:"));
    }
    
    
    /**
     * Edits/Creates the profile filter from cli.
     *
     * @param cli the control cli client
     * @param profileFilterId the profile filter id (if null new profile filter will be created)
     * @param profileSchemaId the profile schema id
     * @param endpointGroupId the endpoint group Id
     * @param createOut create output file with object id     
     * @return the profileFilterId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editProfileFilterCli(ControlApiCliThriftClient cli, String profileFilterId, String profileSchemaId, String endpointGroupId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(profileFilterId);
        int result = -1;
        if (create) {
            if (strIsEmpty(profileSchemaId) || strIsEmpty(endpointGroupId)) {
                String applicationId = editApplicationCli(cli, null, null, false);
                profileSchemaId = editProfileSchemaCli(cli, null, applicationId, false);
                cliOut.reset();
                endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
                cliOut.reset();
            }
            String cmdLine = "createProfileFilter -f " + getTestFile("testProfileFilter.json") + " -s " + profileSchemaId + " -e " + endpointGroupId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editProfileFilter -f " + getTestFile("testProfileFilterUpdated.json") + " -i " + profileFilterId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Profile Filter with id: ".length()).trim();
            return id;
        }
        else if (profileFilterId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Profile Filter with id " + FAKE_ID + " not found!"));
            return profileFilterId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Profile Filter updated."));
            return profileFilterId;
        }
    }
    
    /**
     * Activates profile filter from cli.
     *
     * @param cli the control cli client
     * @param profileFilterId the profile filter Id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void activateProfileFilterCli(ControlApiCliThriftClient cli, String profileFilterId) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = cli.processLine("activateProfileFilter -i " + profileFilterId);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Profile Filter Activated."));
    }        
    
    /**
     * Edits/Creates the configuration from cli.
     *
     * @param cli the control cli client
     * @param configurationId the configuration id (if null new configuration will be created)
     * @param configSchemaId the configuration schema id
     * @param endpointGroupId the endpoint group Id
     * @param createOut create output file with object id     
     * @return the configurationId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editConfigurationCli(ControlApiCliThriftClient cli, String configurationId, String configSchemaId, String endpointGroupId, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(configurationId);
        int result = -1;
        if (create) {
            if (strIsEmpty(configSchemaId) || strIsEmpty(endpointGroupId)) {
                String applicationId = editApplicationCli(cli, null, null, false);
                configSchemaId = editConfigSchemaCli(cli, null, applicationId, false);
                cliOut.reset();
                endpointGroupId = editEndpointGroupCli(cli, null, applicationId, "10", false);
                cliOut.reset();
            }
            String cmdLine = "createConfiguration -f " + getTestFile("testConfig.json") + " -s " + configSchemaId + " -e " + endpointGroupId;
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editConfiguration -f " + getTestFile("testConfigUpdated.json") + " -i " + configurationId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String out = output.trim();
            int index = "Created new Configuration with id: ".length();
            if (out.length() > index) {
                return out.substring(index).trim();
            } else {
                logger.debug("Can't edit configuration: {}", out);
                return null;
            }
        }
        else if (configurationId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Configuration with id " + FAKE_ID + " not found!"));
            return configurationId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Configuration updated."));
            return configurationId;
        }
    }
    
    /**
     * Activates configuration from cli.
     *
     * @param cli the control cli client
     * @param configurationId the profile filter Id
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void activateConfigurationCli(ControlApiCliThriftClient cli, String configurationId) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = cli.processLine("activateConfiguration -i " + configurationId);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Configuration Activated."));
    }        
    
    /**
     * Edits/Creates the endpoint group from cli.
     *
     * @param cli the control cli client
     * @param topicId the topic id
     * @param applicationId the application Id
     * @param subscriptionType the subscription type
     * @param createOut create output file with object id
     * @return the endpointGroupId
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private String editTopicCli(ControlApiCliThriftClient cli, String topicId, String applicationId, SubscriptionType subscriptionType, boolean createOut) throws UnsupportedEncodingException {
        cliOut.reset();
        boolean create = strIsEmpty(topicId);
        int result = -1;
        if (create) {
            if (strIsEmpty(applicationId)) {
                applicationId = editApplicationCli(cli, null, null, false);
                cliOut.reset();
            }
            String cmdLine = "createTopic -n testEndpointGroup -a " + applicationId + " -t " + subscriptionType.name();
            if (createOut) {
                cmdLine += " -o dummy.out";
            }
            result = cli.processLine(cmdLine);
        }
        else {
            result = cli.processLine("editTopic -n testEndpointGroup2 -i " + topicId);
        }
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        if (create) {
            String id = output.trim().substring("Created new Topic with id: ".length()).trim();
            return id;
        }
        else if (topicId.equals(FAKE_ID)) {
            Assert.assertTrue(output.trim().startsWith("Topic with id " + FAKE_ID + " not found!"));
            return topicId;
        }    
        else {
            Assert.assertTrue(output.trim().startsWith("Topic updated."));
            return topicId;
        }
    }    
    
    /**
     * Generates Java sdk from cli.
     *
     * @param cli the control cli client
     * @param applicationId the application id
     * @param profileSchemaVersion the profile schema version
     * @param configSchemaVersion the configuration schema version
     * @param notificationSchemaVersion the notification schema version
     * @return true if generation was success
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private boolean generateSdkCli(ControlApiCliThriftClient cli, String applicationId, int profileSchemaVersion, int configSchemaVersion, int notificationSchemaVersion) throws UnsupportedEncodingException {
        cliOut.reset();
        int result = -1;
        String outDir = getSdkOutDir();
        String cmdLine = "generateSdk -sdk java -a " + applicationId + 
                " -psv " + profileSchemaVersion + 
                " -csv " + configSchemaVersion + 
                " -nsv " + notificationSchemaVersion +
                " -out " + outDir;
        result = cli.processLine(cmdLine);
        Assert.assertEquals(result, 0);
        String output = cliOut.toString("UTF-8");
        Assert.assertTrue(output.trim().startsWith("Generated SDK: "));
        
        File sdkOutDir = new File(outDir);
        if (sdkOutDir.exists() && sdkOutDir.isDirectory()) {
            return sdkOutDir.listFiles().length==1;
        }
        return false;
    }    

    /**
     * Str is empty.
     * 
     * @param str
     *            the str
     * @return true, if successful
     */
    private static boolean strIsEmpty(String str) {
        return str == null || str.trim().equals("");
    }
    
    /**
     * Gets the test file.
     *
     * @param file the file
     * @return the test file
     */
    private String getTestFile(String file) {
        String targetPath = System.getProperty("targetPath");
        File targetDir = new File(targetPath);
        File testFile = new File(targetDir.getAbsolutePath() + 
                File.separator + 
                "test-classes" + 
                File.separator + 
                "data" + 
                File.separator + 
                file);
        return testFile.getAbsolutePath();
    }
    
    /**
     * Gets the test file content.
     *
     * @param file the file
     * @return the test file content
     */
    private String getTestFileContent(String file) {
        String targetPath = System.getProperty("targetPath");
        File targetDir = new File(targetPath);
        File testFile = new File(targetDir.getAbsolutePath() + 
                File.separator + 
                "test-classes" + 
                File.separator + 
                "data" + 
                File.separator + 
                file);
        BufferedReader reader = null;
        String result = null;
        try {
            StringWriter stringWriter = new StringWriter();
            reader = new BufferedReader(new FileReader(testFile));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                stringWriter.write(buf, 0, numRead);
            }
            result = stringWriter.toString();

        } catch (IOException e) {
            logger.error("Unable to read from specified file '"
                    + testFile + "'! Error: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    logger.error("Unable to close file '"
                            + testFile + "'! Error: " + ioe.getMessage(), ioe);
                }
            }
        }
        return result;
    }
    
    /**
     * Gets the sdk out dir.
     *
     * @return the sdk out dir
     */
    private String getSdkOutDir() {
        String targetPath = System.getProperty("targetPath");
        File targetDir = new File(targetPath);
        File sdkOutDir = new File(targetDir.getAbsolutePath() + 
                File.separator + 
                "test_sdk");
        return sdkOutDir.getAbsolutePath();
    }
}
