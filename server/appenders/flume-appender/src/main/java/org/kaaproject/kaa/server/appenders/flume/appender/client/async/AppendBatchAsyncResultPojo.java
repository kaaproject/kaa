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

package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import java.util.List;

import org.apache.flume.Event;

public class AppendBatchAsyncResultPojo {
    public boolean isSuccessful;
    public List<Event> events;

    public AppendBatchAsyncResultPojo(boolean isSuccessful, List<Event> events) {
        super();
        this.isSuccessful = isSuccessful;
        this.events = events;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public List<Event> getEvents() {
        return events;
    }
}
