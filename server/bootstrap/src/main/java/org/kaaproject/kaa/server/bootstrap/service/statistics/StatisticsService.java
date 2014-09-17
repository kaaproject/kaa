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
package org.kaaproject.kaa.server.bootstrap.service.statistics;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.SessionTrackable;
import org.kaaproject.kaa.server.common.server.Track;

/**
 * @author Andrey Panasenko
 *
 */
public class StatisticsService extends Thread implements SessionTrackable {

    /**
     * 
     */
    public StatisticsService() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.SessionTrackable#newSession(java.util.UUID)
     */
    @Override
    public Track newSession(UUID uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.SessionTrackable#closeSession(java.util.UUID)
     */
    @Override
    public void closeSession(UUID uuid) {
        // TODO Auto-generated method stub
        
    }

}
