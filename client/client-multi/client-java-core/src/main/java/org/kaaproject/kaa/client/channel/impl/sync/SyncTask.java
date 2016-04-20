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

package org.kaaproject.kaa.client.channel.impl.sync;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.common.TransportType;

public class SyncTask {
    private final Set<TransportType> types;
    private final boolean ackOnly;
    private final boolean all;

    public SyncTask(TransportType type, boolean ackOnly, boolean all) {
        this(Collections.singleton(type), ackOnly, all);
    }

    public SyncTask(Set<TransportType> types, boolean ackOnly, boolean all) {
        super();
        this.types = types;
        this.ackOnly = ackOnly;
        this.all = all;
    }

    public Set<TransportType> getTypes() {
        return types;
    }

    public boolean isAckOnly() {
        return ackOnly;
    }

    public boolean isAll() {
        return all;
    }

    public static SyncTask merge(SyncTask task, List<SyncTask> additionalTasks) {
        Set<TransportType> types = new HashSet<TransportType>();
        types.addAll(task.types);
        boolean ack = task.ackOnly;
        boolean all = task.all;
        for (SyncTask aTask : additionalTasks) {
            types.addAll(aTask.types);
            ack = ack && aTask.ackOnly;
            all = all || aTask.all;
        }
        return new SyncTask(types, ack, all);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SyncTask [types=");
        builder.append(types);
        builder.append(", ackOnly=");
        builder.append(ackOnly);
        builder.append(", all=");
        builder.append(all);
        builder.append("]");
        return builder.toString();
    }
}