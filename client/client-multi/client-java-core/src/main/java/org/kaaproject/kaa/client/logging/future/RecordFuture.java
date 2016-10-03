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

package org.kaaproject.kaa.client.logging.future;

import org.kaaproject.kaa.client.logging.RecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordFuture implements Future<RecordInfo> {

  private static final Logger LOG = LoggerFactory.getLogger(RecordFuture.class);

  private static AtomicInteger recordFutureCounter = new AtomicInteger(0);
  private final int recordFutureId = recordFutureCounter.getAndIncrement();
  private final long recordAddedTimestampMs;
  private BlockingQueue<ExecutionResult<RecordInfo>> queue = new ArrayBlockingQueue<>(1);
  private volatile State state = State.WAITING;

  public RecordFuture() {
    super();
    recordAddedTimestampMs = System.currentTimeMillis();
  }

  public int getRecordFutureId() {
    return recordFutureId;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return state == State.DONE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + recordFutureId;
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
    RecordFuture other = (RecordFuture) obj;
    if (recordFutureId != other.recordFutureId) {
      return false;
    }
    return true;
  }

  /**
   * Sets value.
   *
   * @param value      value
   * @param arriveTime arrive time
   */
  public void setValue(RecordInfo value, Long arriveTime) {
    try {
      value.setRecordAddedTimestampMs(recordAddedTimestampMs);
      value.setRecordDeliveryTimeMs(arriveTime - recordAddedTimestampMs);
      this.queue.put(new ExecutionResult<>(value, null));
    } catch (InterruptedException ex) {
      LOG.warn("Failed to push value", ex);
    }
    state = State.DONE;
  }

  @Override
  public RecordInfo get() throws InterruptedException, ExecutionException {
    ExecutionResult<RecordInfo> result = queue.take();
    return processResult(result);
  }

  @Override
  public RecordInfo get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
    ExecutionResult<RecordInfo> result = queue.poll(timeout, unit);
    return processResult(result);
  }

  private RecordInfo processResult(ExecutionResult<RecordInfo> result) throws ExecutionException {
    if (result.getE() == null) {
      return result.get();
    } else {
      throw new ExecutionException(result.getE());
    }
  }

  private enum State {
    WAITING, DONE
  }
}
