/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.client.channel;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.client.plugin.ExtensionId;
import org.kaaproject.kaa.common.TransportType;

public class DefaultSyncTask implements ChannelSyncTask {

    private final Map<TransportType, ChannelDirection> tasks;
    private final Set<ExtensionId> exts;
    private final boolean syncAllExtensions;
    private final boolean ackOnly;

    public DefaultSyncTask(Map<TransportType, ChannelDirection> tasks, boolean syncAllExtensions) {
        this(tasks, null, syncAllExtensions, false);
    }

    public DefaultSyncTask(Map<TransportType, ChannelDirection> tasks, Set<ExtensionId> exts, boolean ackOnly) {
        this(tasks, exts, false, ackOnly);
    }

    private DefaultSyncTask(Map<TransportType, ChannelDirection> tasks, Set<ExtensionId> exts, boolean syncAllExtensions, boolean ackOnly) {
        super();
        this.syncAllExtensions = syncAllExtensions;
        this.ackOnly = ackOnly;
        if (tasks != null) {
            this.tasks = tasks;
        } else {
            this.tasks = Collections.emptyMap();
        }
        if (exts != null) {
            this.exts = exts;
        } else {
            this.exts = Collections.emptySet();
        }
    }

    @Override
    public Map<TransportType, ChannelDirection> getTypes() {
        return Collections.unmodifiableMap(tasks);
    }

    @Override
    public Set<ExtensionId> getExtensions() {
        return Collections.unmodifiableSet(exts);
    }

    @Override
    public boolean isSyncAllExtensions() {
        return syncAllExtensions;
    }

    @Override
    public boolean isAckOnly() {
        return ackOnly;
    }

    @Override
    public String toString() {
        return "DefaultSyncTask [tasks=" + tasks + ", exts=" + exts + ", syncAllExtensions=" + syncAllExtensions + ", ackOnly=" + ackOnly
                + "]";
    }

}
