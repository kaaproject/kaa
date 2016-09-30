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

package org.kaaproject.kaa.client.channel.impl.channels.polling;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CancelableScheduledFuture<V> implements RunnableScheduledFuture<V> {

  private final CancelableRunnable cancelableRunnable;
  private final RunnableScheduledFuture<V> futureTask;

  public CancelableScheduledFuture(CancelableRunnable cancelableRunnable,
                                   RunnableScheduledFuture<V> futureTask) {
    this.cancelableRunnable = cancelableRunnable;
    this.futureTask = futureTask;
  }

  @Override
  public void run() {
    futureTask.run();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (mayInterruptIfRunning) {
      cancelableRunnable.cancel();
    }
    return this.futureTask.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return futureTask.isCancelled();
  }

  @Override
  public boolean isDone() {
    return futureTask.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return futureTask.get();
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws InterruptedException,
      ExecutionException, TimeoutException {
    return futureTask.get(timeout, unit);
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return futureTask.getDelay(unit);
  }

  @Override
  public int compareTo(Delayed delayed) {
    return futureTask.compareTo(delayed);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((futureTask == null) ? 0 : futureTask.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CancelableScheduledFuture other = (CancelableScheduledFuture) obj;
    if (futureTask == null) {
      if (other.futureTask != null) {
        return false;
      }
    } else if (!futureTask.equals(other.futureTask)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isPeriodic() {
    return futureTask.isPeriodic();
  }

}
