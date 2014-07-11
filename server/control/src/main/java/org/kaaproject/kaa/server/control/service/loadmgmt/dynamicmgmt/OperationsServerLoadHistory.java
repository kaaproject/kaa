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

package org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt;

import java.util.LinkedList;
import java.util.List;

/**
 * The Class OperationsServerLoadHistory
 *
 * @author Andrey Panasenko
 */
public class OperationsServerLoadHistory {
    private final List<OperationsServerLoad> history;
    private long maxHistoryTimeLive = 600*1000;

    /**
     * The Class OperationsServerLoad.
     */
    public class OperationsServerLoad {
        private final long time;
        private int registeredUsersCount;
        private int processedRequestCount;
        private int deltaCalculationCount;

        protected OperationsServerLoad() {
            time = System.currentTimeMillis();
        }

        /**
         * Gets the time.
         *
         * @return the time
         */
        public long getTime() {
            return time;
        }

        /**
         * Gets the registered users count.
         *
         * @return the registeredUsersCount
         */
        public int getRegisteredUsersCount() {
            return registeredUsersCount;
        }

        /**
         * Sets the registered users count.
         *
         * @param registeredUsersCount the registeredUsersCount to set
         */
        public void setRegisteredUsersCount(int registeredUsersCount) {
            this.registeredUsersCount = registeredUsersCount;
        }

        /**
         * Gets the processed request count.
         *
         * @return the processedRequestCount
         */
        public int getProcessedRequestCount() {
            return processedRequestCount;
        }

        /**
         * Sets the processed request count.
         *
         * @param processedRequestCount the processedRequestCount to set
         */
        public void setProcessedRequestCount(int processedRequestCount) {
            this.processedRequestCount = processedRequestCount;
        }

        /**
         * Gets the delta calculation count.
         *
         * @return the deltaCalculationCount
         */
        public int getDeltaCalculationCount() {
            return deltaCalculationCount;
        }

        /**
         * Sets the delta calculation count.
         *
         * @param deltaCalculationCount the deltaCalculationCount to set
         */
        public void setDeltaCalculationCount(int deltaCalculationCount) {
            this.deltaCalculationCount = deltaCalculationCount;
        }
    }

    public OperationsServerLoadHistory(long maxHistoryTimeLiv) {
        setMaxHistoryTimeLive(maxHistoryTimeLiv);
        history = new LinkedList<OperationsServerLoad>();
    }

    /**
     * Adds the Operations server load to the history
     *
     * @param registeredUsersCount the registered users count
     * @param processedRequestCount the processed request count
     * @param deltaCalculationCount the delta calculation count
     */
    public void addOpsServerLoad(int registeredUsersCount, int processedRequestCount, int deltaCalculationCount) {
        removeOldHistory();
        OperationsServerLoad snap = new OperationsServerLoad();
        snap.setRegisteredUsersCount(registeredUsersCount);
        snap.setProcessedRequestCount(processedRequestCount);
        snap.setDeltaCalculationCount(deltaCalculationCount);
        history.add(snap);
    }

    /**
     * Gets the history.
     *
     * @return the history
     */
    public final List<OperationsServerLoad> getHistory() {
        return history;
    }

    /**
     * Removes the old history.
     */
    private void removeOldHistory() {
        long current = System.currentTimeMillis();
        List<OperationsServerLoad> toDelete = new LinkedList<OperationsServerLoad>();
        for(OperationsServerLoad snap : history) {
            if ((current - snap.getTime()) > maxHistoryTimeLive) {
                //Remove record.
                toDelete.add(snap);
            }
        }
        if (!toDelete.isEmpty()) {
            for(OperationsServerLoad snap : toDelete) {
                history.remove(snap);
            }
            toDelete.clear();
        }

    }

    /**
     * Gets the max history time live.
     *
     * @return the maxHistoryTimeLive
     */
    public long getMaxHistoryTimeLive() {
        return maxHistoryTimeLive;
    }

    /**
     * Sets the max history time live.
     *
     * @param maxHistoryTimeLive the maxHistoryTimeLive to set
     */
    public void setMaxHistoryTimeLive(long maxHistoryTimeLive) {
        this.maxHistoryTimeLive = maxHistoryTimeLive;
    }
}
