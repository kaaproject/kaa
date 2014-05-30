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

package org.kaaproject.kaa.client.update;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.persistance.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.profile.SerializedProfileContainer;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.client.update.DefaultUpdateManager;
import org.kaaproject.kaa.client.update.UpdateListener;
import org.kaaproject.kaa.client.update.commands.AbstractCommand;
import org.kaaproject.kaa.client.update.commands.Command;
import org.kaaproject.kaa.client.update.commands.CommandFactory;
import org.kaaproject.kaa.client.update.commands.DefaultCommandFactory;
import org.kaaproject.kaa.client.update.strategies.UpdateStrategy;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;

public class DefaultUpdateManagerTest {

    private static KaaClientProperties getProperties() {
        KaaClientProperties props = new KaaClientProperties();
        props.setProperty("state.file_location", "state.properties");
        props.setProperty("keys.public", "key.public");
        File pub = new File("key.public");
        pub.deleteOnExit();
        props.setProperty("keys.private", "key.private");
        File priv = new File("key.private");
        priv.deleteOnExit();
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_DELAY, "0");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_PERIOD, "1");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_UNIT, "SECONDS");
        props.setProperty(KaaClientProperties.CONFIG_VERSION, "1");
        props.setProperty(KaaClientProperties.PROFILE_VERSION, "1");
        props.setProperty(KaaClientProperties.SYSTEM_NT_VERSION, "1");
        props.setProperty(KaaClientProperties.USER_NT_VERSION, "1");
        props.setProperty(KaaClientProperties.APPLICATION_TOKEN, "123456");
        return props;
    }

    private static KaaClientState getClientState(KaaClientProperties props) {
        return new KaaClientPropertiesState(props);
    }

    private static DefaultUpdateManager getManager(OperationsTransport transport,
            KaaClientProperties props, KaaClientState state)
            throws TransportException, IOException {
        DefaultUpdateManager manager = new DefaultUpdateManager(props, new KaaClientPropertiesState(props));

        when(transport.sendRegisterCommand(any(EndpointRegistrationRequest.class))).thenReturn(new SyncResponse());
        when(transport.sendSyncRequest(any(SyncRequest.class))).thenReturn(new SyncResponse());
        when(transport.sendLongSyncRequest(any(LongSyncRequest.class))).thenReturn(new SyncResponse());
        when(transport.sendUpdateCommand(any(ProfileUpdateRequest.class))).thenReturn(new SyncResponse());
        manager.setTransport(transport);

        SerializedProfileContainer container = mock(SerializedProfileContainer.class);
        when(container.getSerializedProfile()).thenReturn(new byte [] {1, 2, 3});
        manager.setSerializedProfileContainer(container);
        return manager;
    }

    private static class CommandTest {
        private OperationsTransport transport;
        private KaaClientProperties props;
        private KaaClientState state;
        private DefaultUpdateManager manager;
        private UpdateListener listener;
        private CommandFactory factory;

        public CommandTest() throws TransportException, IOException {
            this.transport = mock(OperationsTransport.class);
            this.props = getProperties();
            this.state = getClientState(props);
            this.manager = getManager(transport, props, state);
            this.listener = mock(UpdateListener.class);
            this.factory = new DefaultCommandFactory(manager, state, props);
        }

        public OperationsTransport getTransport() {
            return transport;
        }

        public DefaultUpdateManager getUpdateManager() {
            return manager;
        }

        public CommandFactory getCommandFactory () {
            return factory;
        }

        public void verifyListener(Command cmd) throws TransportException, IOException {
            assertEquals(true, manager.addUpdateListener(listener));
            assertEquals(false, manager.addUpdateListener(listener));
            assertEquals(false, manager.addUpdateListener(null));
            cmd.execute();
            assertEquals(true, manager.removeUpdateListener(listener));
            assertEquals(false, manager.removeUpdateListener(listener));
            assertEquals(false, manager.removeUpdateListener(null));
            cmd.execute();
            verify(listener, times(1)).onDeltaUpdate(any(SyncResponse.class));
        }
    }

    class TestCommand extends AbstractCommand {

        public TestCommand(TransportExceptionHandler handler) {
            super(handler);
        }

        @Override
        protected void doExecute() throws TransportException {
            throw new TransportException("exception");
        }

    }

    @Test
    public void commandNegativeTest() throws TransportException, IOException {
        TransportExceptionHandler handler = mock(TransportExceptionHandler.class);
        TestCommand cmd = new TestCommand(handler);
        cmd.execute();
        verify(handler, times(1)).onTransportException();
    }

    @Test
    public void testPollComand() throws TransportException, IOException {
        CommandTest cmdTest = new CommandTest();
        Set<String> acceptedUnicastNotificationIds = new HashSet<String>();
        List<SubscriptionCommand> notificationCommands = new LinkedList<SubscriptionCommand>();
        Command cmd = cmdTest.getCommandFactory().createPollCommand(cmdTest.getTransport(), acceptedUnicastNotificationIds, notificationCommands);
        cmdTest.verifyListener(cmd);
        verify(cmdTest.getTransport(), times(2)).sendSyncRequest(any(SyncRequest.class));
    }

    @Test
    public void testLongPollComand() throws TransportException, IOException {
        CommandTest cmdTest = new CommandTest();
        Command cmd = cmdTest.getUpdateManager().getNextTask();
        cmdTest.verifyListener(cmd);
        verify(cmdTest.getTransport(), times(2)).sendLongSyncRequest(any(LongSyncRequest.class));
    }

    @Test
    public void testRegisterCommand() throws TransportException, IOException {
        CommandTest cmdTest = new CommandTest();
        Command cmd = cmdTest.getCommandFactory().createRegisterCommand(cmdTest.getTransport(), new byte [] {1, 2, 3});
        cmdTest.verifyListener(cmd);
        verify(cmdTest.getTransport(), times(2)).sendRegisterCommand(any(EndpointRegistrationRequest.class));
    }

    @Test
    public void testUpdateCommand() throws TransportException, IOException {
        CommandTest cmdTest = new CommandTest();
        Command cmd = cmdTest.getCommandFactory().createProfileUpdateCommand(cmdTest.getTransport(), new byte [] {1, 2, 3}, new HashSet<String>());
        cmdTest.verifyListener(cmd);
        verify(cmdTest.getTransport(), times(2)).sendUpdateCommand(any(ProfileUpdateRequest.class));
    }

    @Test
    public void testUpdateStrategy() throws TransportException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        UpdateStrategy strategy = mock(UpdateStrategy.class);
        OperationsTransport transport = mock(OperationsTransport.class);
        KaaClientProperties props = getProperties();
        KaaClientState state = getClientState(props);
        state.setRegistered(false);
        DefaultUpdateManager manager = getManager(transport, props, state);

        Class<?> managerClass = manager.getClass();
        Field strategyField = managerClass.getDeclaredField("pollStrategy");
        strategyField.setAccessible(true);
        strategyField.set(manager, strategy);

        manager.start();
        manager.stop();
        state.setRegistered(true);
        manager.start();
        manager.onProfileChange(new byte[] {4, 3, 2, 1});

        verify(strategy, times(2)).executeCommand(any(Command.class));
        verify(strategy, times(3)).startPoll();
        verify(strategy, times(3)).stopPoll();

        manager.failover(1L);
        verify(strategy, times(1)).retryCommand(eq(1L), any(Command.class));
    }
}
