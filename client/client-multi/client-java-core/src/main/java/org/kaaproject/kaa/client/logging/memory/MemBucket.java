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

package org.kaaproject.kaa.client.logging.memory;

import org.kaaproject.kaa.client.logging.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemBucket {
  private static final Logger LOG = LoggerFactory.getLogger(MemBucket.class);
  protected final int id;
  protected final long maxSize;
  protected final int maxRecordCount;
  private final List<LogRecord> records;
  protected long size;
  private MemBucketState state;

  /**
   * All-args constructor.
   */
  public MemBucket(int id, long maxSize, int maxRecordCount) {
    super();
    this.id = id;
    this.maxSize = maxSize;
    this.maxRecordCount = maxRecordCount;
    this.records = new ArrayList<LogRecord>();
    this.state = MemBucketState.FREE;
  }

  public int getId() {
    return id;
  }

  public long getSize() {
    return size;
  }

  public int getCount() {
    return records.size();
  }

  public List<LogRecord> getRecords() {
    return records;
  }

  public MemBucketState getState() {
    return state;
  }

  public void setState(MemBucketState state) {
    this.state = state;
  }

  /**
   * Adds a record to a bucket.
   *
   * @param record record
   * @return       true if record is added otherwise false
   */
  public boolean addRecord(LogRecord record) {
    if (size + record.getSize() > maxSize) {
      LOG.trace("No space left in bucket. Current size: {}, record size: {}, max size: {}",
              size, record.getSize(), maxSize);
      return false;
    }
    if (getCount() + 1 > maxRecordCount) {
      LOG.trace("No space left in bucket. Current count: {}, max count: {}", getCount(),
              maxRecordCount);
      return false;
    }
    records.add(record);
    size += record.getSize();
    return true;
  }

  /**
   * Shrinks current bucket to the newSize.
   *
   * @param newSize  expected max size of a bucket inclusively
   * @param newCount the new count
   * @return removed from the bucket records
   */
  public List<LogRecord> shrinkToSize(long newSize, int newCount) {
    LOG.trace("Shrinking {} bucket to the new size: [{}] and count [{}]", this, newSize, newCount);
    if (newSize < 0 || newCount < 0) {
      throw new IllegalArgumentException("New size and count values must be non-negative");
    }

    if (newSize >= size && newCount >= getCount()) {
      return Collections.emptyList();
    }

    List<LogRecord> overSize = new ArrayList<>();
    int lastIndex = records.size() - 1;
    while ((size > newSize || getCount() > newCount) && lastIndex > 0) {
      LogRecord curRecord = records.remove(lastIndex--);
      overSize.add(curRecord);
      size -= curRecord.getSize();
    }

    LOG.trace("Shrink over-sized elements: [{}]. New bucket size: [{}] and count [{}]",
            overSize.size(), size, getCount());

    return overSize;
  }

  @Override
  public String toString() {
    return "MemBucket{"
            + "id=" + id
            + ", maxSize=" + maxSize
            + ", maxRecordCount=" + maxRecordCount
            + ", records count=" + records.size()
            + ", size=" + size
            + ", state=" + state
            + '}';
  }

  public static enum MemBucketState {
    FREE, FULL, PENDING
  }
}
