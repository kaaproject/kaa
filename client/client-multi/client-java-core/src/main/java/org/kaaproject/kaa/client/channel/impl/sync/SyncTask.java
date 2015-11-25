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
package org.kaaproject.kaa.client.channel.impl.sync;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.client.plugin.ExtensionId;
import org.kaaproject.kaa.common.TransportType;

public class SyncTask {
    private static final Set<TransportType> EMPTY_TYPES = Collections.emptySet();
    private static final Set<ExtensionId> EMPTY_EXTS = Collections.emptySet();
    private final Set<TransportType> types;
    private final Set<ExtensionId> exts;
    private final boolean ackOnly;
    private final boolean all;

    public SyncTask(TransportType type, boolean ackOnly, boolean all) {
        this(Collections.singleton(type), EMPTY_EXTS, ackOnly, all);
    }

    public SyncTask(ExtensionId ext, boolean ackOnly, boolean all) {
        this(EMPTY_TYPES, Collections.singleton(ext), ackOnly, all);
    }

    public SyncTask(Set<TransportType> types, Set<ExtensionId> extensions, boolean ackOnly, boolean all) {
        super();
        this.types = types;
        this.exts = extensions;
        this.ackOnly = ackOnly;
        this.all = all;
    }

    public Set<TransportType> getTypes() {
        return types;
    }

    public Set<ExtensionId> getExts() {
        return exts;
    }

    public boolean isAckOnly() {
        return ackOnly;
    }

    public boolean isAll() {
        return all;
    }

    public static SyncTask merge(SyncTask task, List<SyncTask> additionalTasks) {
        Set<TransportType> types = new HashSet<TransportType>();
        Set<ExtensionId> exts = new HashSet<ExtensionId>();
        types.addAll(task.types);
        exts.addAll(task.exts);
        boolean ack = task.ackOnly;
        boolean all = task.all;
        for (SyncTask aTask : additionalTasks) {
            types.addAll(aTask.types);
            exts.addAll(aTask.exts);
            ack = ack && aTask.ackOnly;
            all = all || aTask.all;
        }
        return new SyncTask(types, exts, ack, all);
    }

    @Override
    public String toString() {
        return "SyncTask [types=" + types + ", extensions=" + exts + ", ackOnly=" + ackOnly + ", all=" + all + "]";
    }

}