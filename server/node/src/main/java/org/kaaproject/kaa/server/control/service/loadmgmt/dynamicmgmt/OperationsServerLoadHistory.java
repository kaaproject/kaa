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

package org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;

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
        private LoadInfo loadInfo;

        protected OperationsServerLoad(LoadInfo load) {
            time = System.currentTimeMillis();
            this.loadInfo = load;
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
        public LoadInfo getLoadInfo() {
            return loadInfo;
        }

        /**
         * Sets the registered users count.
         *
         * @param loadInfo the load info to set
         */
        public void setLoadInfo(LoadInfo loadInfo) {
            this.loadInfo = loadInfo;
        }
    }

    public OperationsServerLoadHistory(long maxHistoryTimeLiv) {
        setMaxHistoryTimeLive(maxHistoryTimeLiv);
        history = new CopyOnWriteArrayList<OperationsServerLoad>();
    }

    /**
     * Adds the Operations server load to the history
     *
     * @param load the load
     */
    public void addOpsServerLoad(LoadInfo load) {
        removeOldHistory();
        history.add(new OperationsServerLoad(load));
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
