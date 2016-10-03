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

import org.kaaproject.kaa.common.TransportType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncTask {
  private final Set<TransportType> types;
  private final boolean ackOnly;
  private final boolean all;

  /**
   * Instantiates the SyncTask.
   */
  public SyncTask(TransportType type, boolean ackOnly, boolean all) {
    this(Collections.singleton(type), ackOnly, all);
  }

  /**
   * Instantiates the SyncTask.
   */
  public SyncTask(Set<TransportType> types, boolean ackOnly, boolean all) {
    super();
    this.types = types;
    this.ackOnly = ackOnly;
    this.all = all;
  }

  /**
   * Merges sync tasks.
   *
   * @param syncTask        sync task
   * @param additionalTasks additional sync tasks
   * @return                merged sync task
   */
  public static SyncTask merge(SyncTask syncTask, List<SyncTask> additionalTasks) {
    Set<TransportType> types = new HashSet<>();
    types.addAll(syncTask.types);
    boolean ack = syncTask.ackOnly;
    boolean all = syncTask.all;
    for (SyncTask task : additionalTasks) {
      types.addAll(task.types);
      ack = ack && task.ackOnly;
      all = all || task.all;
    }
    return new SyncTask(types, ack, all);
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