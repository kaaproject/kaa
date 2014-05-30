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
package org.kaaproject.kaa.server.operations.service.statistics;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.http.commands.LongSyncCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.RegisterEndpointCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.SyncCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.UpdateEndpointCommand;
import org.kaaproject.kaa.server.operations.service.statistics.SessionHistory;
import org.kaaproject.kaa.server.operations.service.statistics.SessionHistory.RequestHistory;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class SessionHistoryTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#typeFromString(java.lang.String)}.
     */
    @Test
    public void testTypeFromString() {
        assertEquals(SessionHistory.RequestType.LONGSYNC, SessionHistory.typeFromString(LongSyncCommand.getCommandName()));
        assertEquals(SessionHistory.RequestType.SYNC, SessionHistory.typeFromString(SyncCommand.getCommandName()));
        assertEquals(SessionHistory.RequestType.UPENDPOINT, SessionHistory.typeFromString(UpdateEndpointCommand.getCommandName()));
        assertEquals(SessionHistory.RequestType.REGISTER, SessionHistory.typeFromString(RegisterEndpointCommand.getCommandName()));
        assertEquals(SessionHistory.RequestType.UNKNOWN, SessionHistory.typeFromString("test"));
        assertEquals(SessionHistory.RequestType.UNKNOWN, SessionHistory.typeFromString(null));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#SessionHistory(java.util.UUID)}.
     */
    @Test
    public void testSessionHistory() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#getRequests()}.
     */
    @Test
    public void testGetRequests() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        assertNotNull(history.getRequests());
        assertEquals(0, history.getRequests().size());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#getSessionCreateTimestamp()}.
     */
    @Test
    public void testGetSessionCreateTimestamp() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        assertTrue(history.getSessionCreateTimestamp() <= System.currentTimeMillis());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#getUuid()}.
     */
    @Test
    public void testGetUuid() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        assertEquals(uuid, history.getUuid());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#newRequest(org.kaaproject.kaa.server.operations.service.statistics.SessionHistory.RequestType)}.
     */
    @Test
    public void testNewRequestRequestType() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        RequestHistory rh = history.newRequest(SessionHistory.RequestType.SYNC);
        assertNotNull(rh);
        assertEquals(SessionHistory.RequestType.SYNC, rh.getType());
        assertTrue(rh.isType(SessionHistory.RequestType.SYNC));
        rh.setSyncTime(100);
        assertEquals(100, rh.getSyncTime());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        long l = rh.closeRequest();
        assertTrue(l >= 10);
        assertTrue(rh.getRequestCloseTimestamp() > 0);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#isSessionOpen()}.
     */
    @Test
    public void testIsSessionOpen() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        assertTrue(history.isSessionOpen());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#sessionClose()}.
     */
    @Test
    public void testSessionClose() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        assertTrue(history.isSessionOpen());
        history.sessionClose();
        assertTrue(!history.isSessionOpen());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#newRequest(java.lang.String)}.
     */
    @Test
    public void testNewRequestString() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        history.newRequest(SyncCommand.getCommandName());
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 1);
        RequestHistory rhSync = history.getRequests().get(0);
        assertNotNull(rhSync);
        assertEquals(SessionHistory.RequestType.SYNC, rhSync.getType());
        
        history.newRequest(LongSyncCommand.getCommandName());
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 2);
        RequestHistory rhLongSync = history.getRequests().get(1);
        assertNotNull(rhLongSync);
        assertEquals(SessionHistory.RequestType.LONGSYNC, rhLongSync.getType());
        
        history.newRequest(RegisterEndpointCommand.getCommandName());
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 3);
        RequestHistory rhReg = history.getRequests().get(2);
        assertNotNull(rhReg);
        assertEquals(SessionHistory.RequestType.REGISTER, rhReg.getType());
        
        history.newRequest("test");
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 4);
        RequestHistory rhTest = history.getRequests().get(3);
        assertNotNull(rhTest);
        assertEquals(SessionHistory.RequestType.UNKNOWN, rhTest.getType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#setProcessTime(int, long)}.
     */
    @Test
    public void testSetProcessTime() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        int rhId = history.newRequest(SyncCommand.getCommandName());
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 1);
        RequestHistory rh = history.getRequests().get(0);
        assertNotNull(rh);
        history.setProcessTime(rhId, 100);
        assertEquals(100, rh.getSyncTime());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.SessionHistory#closeRequest(int)}.
     */
    @Test
    public void testCloseRequest() {
        UUID uuid = UUID.randomUUID();
        SessionHistory history = new SessionHistory(uuid);
        assertNotNull(history);
        int rhId = history.newRequest(SyncCommand.getCommandName());
        assertNotNull(history.getRequests());
        assertTrue(history.getRequests().size() == 1);
        RequestHistory rh = history.getRequests().get(0);
        assertNotNull(rh);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        history.closeRequest(rhId);
        long l = rh.closeRequest();
        assertTrue(l >= 10);
        assertTrue(rh.getRequestCloseTimestamp() > 0);
    }

}
