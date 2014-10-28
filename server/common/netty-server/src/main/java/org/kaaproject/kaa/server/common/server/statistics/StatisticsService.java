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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.server.common.server.Config;
import org.kaaproject.kaa.server.common.server.SessionTrackable;
import org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater;
import org.kaaproject.kaa.server.common.server.Track;
import org.kaaproject.kaa.server.common.server.statistics.SessionHistory.RequestHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StatisticsService Class.
 * Singletone, use static getService() to get instance
 * Service collects:
 * 1.  private int processedRequestCount - Number of requests (SYNC, LongSYNC)
 *     processed completely during collect window period.
 * 2.  private int registeredUsersCount - average number of online users (LongSYNC)
 *     during collect window period.
 * 3.  private int deltaCalculationCount - average Delta calculation time during collect window period.
 *
 *  processedRequestCount -
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class StatisticsService extends Thread implements SessionTrackable {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(StatisticsService.class);

    /** Statistics service type */
    private ChannelType channelType;
    
    /** node updater */
    private StatisticsNodeUpdater nodeUpdater;

    /** Default thread name */
    private static final String THREAD_NAME = "StatisticsServiceThread";

    /** Default value for statistics update, how many times in statistics window*/
    private static final int DEFAULT_STATISTIC_UPDATE_TIMES = 60;

    /** Default value for statistics collect window in ms */
    //in ms, 5 minutes
    private static final long DEFAULT_STATISTIC_COLLECTION_WINDOW = 300000;

    /** Concurrent HashMap for storing sessions */
    private final ConcurrentHashMap<UUID, SessionHistory> sessions; //NOSONAR

    /** Timestamp of previous recalculation period */
    private long recalculationPreviouseTimestamp = 0;

    /** List for average calculation with sliding window */
    private final List<Integer> processedRequestsWindow;
    private final List<Integer> onlineSessionsWindow;
    private final List<Integer> deltaSyncWindow;

    /** Calculated average values */
    private int averageProcessedRequests = 0;
    private int averageOnlineSessions = 0;
    private int averageDeltaSync = 0;


    /** boolean which used to control operation mode in Thread run() cycle */
    private boolean operate = false;

    /** sync object */
    private final Object sync = new Object();

    /** Statistics collect window in ms */
    private long recalculationPeriod = DEFAULT_STATISTIC_COLLECTION_WINDOW/DEFAULT_STATISTIC_UPDATE_TIMES;

    /** Statistics Update Times */
    private int statsUpdateTimes = DEFAULT_STATISTIC_UPDATE_TIMES;
    
    private final Object onlineSessionsSync = new Object();
    private int onlineSessions = 0;
    private int onlineSessionCounter = 0;
    private int onlineSessionNumber = 0;

    /** Netty Config  */
    Config config;

    /**
     * @param type
     */
    public StatisticsService(ChannelType type, Config config, StatisticsNodeUpdater nodeUpdater) {
        this.channelType = type;
        this.nodeUpdater = nodeUpdater;
        this.config = config;
        sessions = new ConcurrentHashMap<UUID, SessionHistory>();
        processedRequestsWindow = new LinkedList<>();
        onlineSessionsWindow = new LinkedList<>();
        deltaSyncWindow = new LinkedList<>();
        initConfig();
    }

    private void initConfig() {
        if(config != null && config.getStatisticsUpdateTimes() > 0) {
            statsUpdateTimes = config.getStatisticsUpdateTimes();
            recalculationPeriod = config.getStatisticsCalculationWindow()*1000/statsUpdateTimes;
            LOG.debug("StatisticsService for {}: recalculation_period set to {}", channelType.toString(), recalculationPeriod);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#start()
     */
    @Override
    public void start() {
        LOG.trace("Statistics Service {} starting....", channelType.toString());
        operate = true;
        super.start();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        this.setName(THREAD_NAME+channelType.toString());
        LOG.trace("Statistics Service {} Started....", channelType.toString());
        while(operate) {
            synchronized (sync) {
                try {
                    LOG.trace("Statistics Service {} Recalculate Started....", channelType.toString());
                    long recalculationStart = System.currentTimeMillis();

                    recalculate(recalculationPreviouseTimestamp, recalculationStart);
                    recalculationPreviouseTimestamp = recalculationStart;
                    long recalculateDuration =  System.currentTimeMillis() - recalculationStart;

                    LOG.trace("Recalculated "+channelType.toString()+":\nAverage Processed Requests = "+averageProcessedRequests
                            +"\nAverage online sessions = "+averageOnlineSessions
                            +"\nAverage request processing time = "+averageDeltaSync);

                    if (recalculationPeriod > recalculateDuration) {
                        LOG.trace("Statistics Service Recalculate going to sleep "+(recalculationPeriod - recalculateDuration));
                        sync.wait(recalculationPeriod - recalculateDuration);
                    }
                } catch (InterruptedException e) {
                    LOG.trace("Statistics Service Interapted, shutdown service....");
                    operate = false;
                }
            }
        }
        LOG.info("Statistics Service stoped.");
    }

    /**
     * Stop Statistics Service.
     */
    public void shutdown() {
        LOG.info("Statistics Service shutdown....");
        operate = false;

        synchronized (sync) {
            sync.notify();
        }
        try {
            this.join(10000);
        } catch (InterruptedException e) {
            LOG.trace("Statistics Service shutdown join() Interupted");
        } finally {
            LOG.info("Statistics Service shutdown complete.");
        }
    }

    /**
     * Recalculate average statistics values.
     * First calculates values for current interval, from startInterval to stopInterval.
     * And then using sliding window.
     * @param startInterval - long, timestamp of start recalculation interval in ms
     * @param stopInterval - long stop timestamp
     */
    private void recalculate(long startInterval, long stopInterval) {
        // int processedRequestCount
        // int registeredUsersCount
        // int deltaCalculationCount
        int onlineSessions = 0;
        int processedRequests = 0;
        int deltaSum = 0;
        int deltaElementsNumber = 0;
        List<UUID> closedSessions = new LinkedList<>();
        for(SessionHistory session : sessions.values()) {
            List<RequestHistory> requests = session.getRequests();
            ListIterator<RequestHistory> lr = requests.listIterator(requests.size());
            while(lr.hasPrevious()) {
                RequestHistory rh = lr.previous();
                long requestClosed = rh.getRequestCloseTimestamp();
                if (requestClosed > startInterval) {
                    if (requestClosed <= stopInterval) {
                        processedRequests++;
                        deltaSum += rh.getSyncTime();
                        deltaElementsNumber++;
                    }
                } else {
                    //no need to iterate all requests, they pushes back to list.
                    break;
                }
            }
            if (!session.isSessionOpen()) {
                closedSessions.add(session.getUuid());
            }
        }
        //Cleanup sessions
        for(UUID uuid : closedSessions) {
            sessions.remove(uuid);
        }

        int deltaLastValue = 0;
        if (deltaElementsNumber > 0) {
            deltaLastValue = deltaSum/deltaElementsNumber;
        }

        synchronized (onlineSessionsSync) {
            LOG.debug("onlineSessionNumber={}, onlineSessionCounter={}", onlineSessionNumber, onlineSessionCounter);
            if (onlineSessionNumber > 0) {
                onlineSessions = onlineSessionCounter/onlineSessionNumber;
                onlineSessionNumber = 0;
                onlineSessionCounter = onlineSessions;
            }
        }
        
        LOG.debug("processedRequests="+processedRequests+"; onlineSessions="+onlineSessions+"; deltaLastValue="+deltaLastValue+";");

        averageProcessedRequests = calculateWindow(processedRequestsWindow, processedRequests);
        averageOnlineSessions = calculateWindow(onlineSessionsWindow, onlineSessions);
        averageDeltaSync = calculateWindow(deltaSyncWindow, deltaLastValue);

        LOG.debug("processedRequestsWindow.size()="+processedRequestsWindow.size()+"; onlineSessionsWindow.size()="+onlineSessionsWindow.size()+"; deltaSyncWindow.size()="+deltaSyncWindow.size()+";");
        LOG.debug("averageProcessedRequests="+averageProcessedRequests+"; averageOnlineSessions="+averageOnlineSessions+"; averageDeltaSync="+averageDeltaSync+";");
        updateNodeInfo();
    }

    /**
     * Recalculate average value for window
     * @param list - list of window elements.
     * @param lastValue - new recalculated value
     * @return - average
     */
    private int calculateWindow(List<Integer> list, int lastValue) {
        int average = 0;
        list.add(Integer.valueOf(lastValue));
        if (list.size() > statsUpdateTimes) {
            //Remove first update from window
            list.remove(0);
        }
        for(Integer pr : list) {
            average += pr.intValue();
        }
        if (!list.isEmpty()) {
            average = average/list.size();
        }
        return average;
    }

    /**
     * Updates calculates statistics values in ZooKepper OperationsNode
     */
    private void updateNodeInfo() {
        if (config != null && nodeUpdater != null) {
            nodeUpdater.setStatistics(averageProcessedRequests, averageOnlineSessions, averageDeltaSync);
        }
    }

        @Override
    public Track newSession(UUID uuid) {
        SessionHistory track = new SessionHistory(uuid);
        sessions.put(uuid, track);
        synchronized (onlineSessionsSync) {
            onlineSessions++;
            onlineSessionCounter += onlineSessions;
            onlineSessionNumber++;
        }
        LOG.debug("StatisticsService {}, New session {} created, online sessions {}",channelType.toString(), uuid.toString(), onlineSessions);
        return track;
    }

    @Override
    public void closeSession(UUID uuid) {
        SessionHistory track = sessions.get(uuid);
        if (track != null) {
            track.sessionClose();
            synchronized (onlineSessionsSync) {
                if (onlineSessions > 0)
                    onlineSessions--;
            }
        }
        LOG.debug("StatisticsService {}, session {} closed",channelType.toString(), uuid.toString());
    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Config config) {
        this.config = config;
        initConfig();
    }

    /**
     * @return the channelType
     */
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * @param channelType the channelType to set
     */
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }
}

