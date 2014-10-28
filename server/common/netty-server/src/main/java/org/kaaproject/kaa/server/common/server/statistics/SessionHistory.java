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

package org.kaaproject.kaa.server.common.server.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;
import org.kaaproject.kaa.server.common.server.Track;

/**
 * Class SessionHistory.
 * Gather history of HTTP session to Operations server.
 * Implement Track interface to collect statistics of HTTP requests
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class SessionHistory implements Track{

    /** session UUID. */
    private final UUID uuid;

    /** Session create timestamp */
    private final long sessionCreateTimestamp;

    /** Boolean representing session state open/close */
    private boolean sessionOpen = false;

    /** List of HTTP requests */
    private final List<RequestHistory> requests;

    /** Request ID random generator */
    private final Random rnd;

    

//    /**
//     * Used to convert String commandName from URI to enup RequestType
//     * @param commandName - URI part of HTTP request which represent command name
//     * @return - enum RequestType
//     */
//    public static RequestType typeFromString(String commandName) {
//        if (commandName != null) {
//            if (commandName.equalsIgnoreCase(SyncCommand.getCommandName())) {
//                return RequestType.SYNC;
//            } else if (commandName.equalsIgnoreCase(LongSyncCommand.getCommandName())) {
//                return RequestType.LONGSYNC;
//            } else {
//                return RequestType.UNKNOWN;
//            }
//        } else {
//            return RequestType.UNKNOWN;
//        }
//    }

    /**
     * Inner Class RequestHistory - used to handle HTTP request statistics
     * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
     *
     */
    public class RequestHistory {

        /** Random HTTP request ID */
        private int id = rnd.nextInt();

        /** Request create timestamp  */
        private final long requestCreateTimestamp;

        /** Processing time, gets from filed in CommandProcessor class. */
        private long syncTime = 0;

        /** Request close time stamp */
        private long requestCloseTimestamp = 0;

        /**
         * Return int request ID
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * Constructor
         * @param type - type of request
         */
        public RequestHistory() {
            requestCreateTimestamp = System.currentTimeMillis();
            id = rnd.nextInt();
        }


        /**
         * SyncTime setter.
         * @param syncTime long
         */
        public void setSyncTime(long syncTime) {
            this.syncTime = syncTime;
        }

        /**
         * SyncTime getter.
         * @return syncTime long
         */
        public long getSyncTime() {
            return syncTime;
        }

        /**
         * Close request and set close Time
         * @return request close timestamp
         */
        public long closeRequest() {
            if (requestCloseTimestamp == 0) {
                requestCloseTimestamp = System.currentTimeMillis();
            }
            return requestCloseTimestamp - requestCreateTimestamp;
        }

        /**
         * Request close timestamp getter.
         * @return the requestCloseTimestamp
         */
        public long getRequestCloseTimestamp() {
            return requestCloseTimestamp;
        }
    }


    /**
     * SessionHistory constructor.
     * @param uuid UUID of session
     */
    public SessionHistory(UUID uuid) {
        sessionCreateTimestamp = System.currentTimeMillis();
        rnd = new Random(sessionCreateTimestamp);
        this.uuid = uuid;
        sessionOpen = true;
        requests = new LinkedList<RequestHistory>();
    }

    /**
     * Return List of request
     * @return List<RequestHistory>
     */
    public final List<RequestHistory> getRequests() {
        return requests;
    }


    /**
     * Session create time getter.
     * @return the sessionCreateTimestamp
     */
    public long getSessionCreateTimestamp() {
        return sessionCreateTimestamp;
    }

    /**
     * Session UUID getter.
     * @return the uuid of session
     */
    public UUID getUuid() {
        return uuid;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.Track#newRequest()
     */
    @Override
    public int newRequest() {
        RequestHistory r = new RequestHistory();
        requests.add(r);
        return r.getId();
    }

    /**
     * Return is Session still opened
     * @return boolean if session open
     */
    public boolean isSessionOpen() {
        return sessionOpen;
    }

    /**
     * Mark session as closed.
     */
    public void sessionClose() {
        sessionOpen = false;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.Track#setProcessTime(int, long)
     */
    @Override
    public void setProcessTime(int requestId, long time) {
        ListIterator<RequestHistory> rl = requests.listIterator(requests.size());
        while(rl.hasPrevious()) {
            RequestHistory r = rl.previous();
            if (r.getId() == requestId) {
                r.setSyncTime(time);
                break;
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.Track#closeRequest(int)
     */
    @Override
    public void closeRequest(int requestId) {
        ListIterator<RequestHistory> rl = requests.listIterator(requests.size());
        while(rl.hasPrevious()) {
            RequestHistory r = rl.previous();
            if (r.getId() == requestId) {
                r.closeRequest();
                break;
            }
        }
    }
}
