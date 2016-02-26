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

package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.hdfs.HDFSWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Internal API intended for HDFSSink use.
 * This class does file rolling and handles file formats and serialization.
 * Only the public methods in this class are thread safe.
 */
class BucketWriter {

  private static final Logger LOG = LoggerFactory
      .getLogger(BucketWriter.class);

  private static final String IN_USE_EXT = ".tmp";
  private static final String AVRO_EXT = ".avro";
  /**
   * This lock ensures that only one thread can open a file at a time.
   */
  private static final Integer staticLock = new Integer(1);

  private final HDFSWriter writer;
  private final long rollInterval;
  private final long rollSize;
  private final long rollCount;
  private final long batchSize;
  private final long defaultBlockSize;
  private final ScheduledThreadPoolExecutor timedRollerPool;
  private final UserGroupInformation user;

  private long eventCounter;
  private long processSize;

  private FileSystem fileSystem;

  private volatile String filePath;
  private volatile String bucketPath;
  private volatile long batchCounter;
  private volatile boolean isOpen;
  private volatile ScheduledFuture<Void> timedRollFuture;
  private final SinkCounter sinkCounter;

  BucketWriter(long rollInterval, long rollSize, long rollCount, long batchSize, long defaultBlockSize,
      Context context, String filePath, HDFSWriter writer,
      ScheduledThreadPoolExecutor timedRollerPool, UserGroupInformation user,
      SinkCounter sinkCounter) {
    this.rollInterval = rollInterval;
    this.rollSize = rollSize;
    this.rollCount = rollCount;
    this.batchSize = batchSize;
    this.defaultBlockSize = defaultBlockSize;
    this.filePath = filePath;
    this.writer = writer;
    this.timedRollerPool = timedRollerPool;
    this.user = user;
    this.sinkCounter = sinkCounter;

    isOpen = false;

    writer.configure(context);
  }

  /**
   * Allow methods to act as another user (typically used for HDFS Kerberos)
   * @param <T>
   * @param action
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  private <T> T runPrivileged(final PrivilegedExceptionAction<T> action)
      throws IOException, InterruptedException {

    if (user != null) {
      return user.doAs(action);
    } else {
      try {
        return action.run();
      } catch (IOException ex) {
        throw ex;
      } catch (InterruptedException ex) {
        throw ex;
      } catch (RuntimeException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new RuntimeException("Unexpected exception.", ex);
      }
    }
  }

  /**
   * Clear the class counters
   */
  private void resetCounters() {
    eventCounter = 0;
    processSize = 0;
    batchCounter = 0;
  }

  /**
   * open() is called by append()
   * @throws IOException
   */
  private void open(final long serial) throws IOException, InterruptedException {
    runPrivileged(new PrivilegedExceptionAction<Void>() {
      @Override
      public Void run() throws Exception {
        doOpen(serial);
        return null;
      }
    });
  }

  /**
   * doOpen() must only be called by open()
   * @throws IOException
   */
  private void doOpen(long serial) throws IOException {
    if ((filePath == null) || (writer == null)) {
      throw new IOException("Invalid file settings");
    }

    Configuration config = new Configuration();
    // disable FileSystem JVM shutdown hook
    config.setBoolean("fs.automatic.close", false);

    long blockSize = DFSConfigKeys.DFS_BLOCK_SIZE_DEFAULT;
    if (defaultBlockSize > 0) {
    	blockSize = defaultBlockSize;
    }
    config.set(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, ""+blockSize);

    // Hadoop is not thread safe when doing certain RPC operations,
    // including getFileSystem(), when running under Kerberos.
    // open() must be called by one thread at a time in the JVM.
    // NOTE: tried synchronizing on the underlying Kerberos principal previously
    // which caused deadlocks. See FLUME-1231.
    synchronized (staticLock) {
      try {
        bucketPath = filePath + "." + serial;
        // Need to get reference to FS using above config before underlying
        // writer does in order to avoid shutdown hook & IllegalStateExceptions
        fileSystem = new Path(bucketPath).getFileSystem(config);
        String currentBucket = bucketPath + IN_USE_EXT;
        LOG.debug("Creating " + currentBucket);
        writer.open(currentBucket);
      } catch (Exception ex) {
        sinkCounter.incrementConnectionFailedCount();
        if (ex instanceof IOException) {
          throw (IOException) ex;
        } else {
          throw Throwables.propagate(ex);
        }
      }
    }
    sinkCounter.incrementConnectionCreatedCount();
    resetCounters();

    // if time-based rolling is enabled, schedule the roll
    if (rollInterval > 0) {
      Callable<Void> action = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          LOG.debug("Rolling file ({}): Roll scheduled after {} sec elapsed.",
              bucketPath + IN_USE_EXT, rollInterval);
          try {
            close();
          } catch(Throwable t) { //NOSONAR
            LOG.error("Unexpected error", t);
          }
          return null;
        }
      };
      timedRollFuture = timedRollerPool.schedule(action, rollInterval,
          TimeUnit.SECONDS);
    }

    isOpen = true;
  }

  /**
   * Close the file handle and rename the temp file to the permanent filename.
   * Safe to call multiple times. Logs HDFSWriter.close() exceptions.
   * @throws IOException On failure to rename if temp file exists.
   */
  public synchronized void close() throws IOException, InterruptedException {
    flush();
    runPrivileged(new PrivilegedExceptionAction<Void>() {
      @Override
      public Void run() throws Exception {
        doClose();
        return null;
      }
    });
  }

  /**
   * doClose() must only be called by close()
   * @throws IOException
   */
  private void doClose() throws IOException {
	String currentBucket = bucketPath + IN_USE_EXT;
    LOG.debug("Closing {}", currentBucket);
    if (isOpen) {
      try {
        writer.close(); // could block
        sinkCounter.incrementConnectionClosedCount();
      } catch (IOException e) {
        LOG.warn("failed to close() HDFSWriter for file (" + currentBucket + "). Exception follows.", e);
        sinkCounter.incrementConnectionFailedCount();
      }
      isOpen = false;
    } else {
      LOG.info("HDFSWriter is already closed: {}", currentBucket);
    }

    // NOTE: timed rolls go through this codepath as well as other roll types
    if (timedRollFuture != null) {
        if (!timedRollFuture.isDone()) {
            timedRollFuture.cancel(false); // do not cancel myself if running!
        }
        timedRollerPool.remove((Runnable)timedRollFuture);
        timedRollFuture = null;
    }

    if (bucketPath != null && fileSystem != null) {
      renameBucket(); // could block or throw IOException
      fileSystem = null;
    }
  }

  /**
   * flush the data
   */
  public synchronized void flush() throws IOException, InterruptedException {
    if (!isBatchComplete()) {
      runPrivileged(new PrivilegedExceptionAction<Void>() {
        @Override
        public Void run() throws Exception {
          doFlush();
          return null;
        }
      });
    }
  }

  /**
   * doFlush() must only be called by flush()
   * @throws IOException
   */
  private void doFlush() throws IOException {
    writer.sync(); // could block
    batchCounter = 0;
  }

  /**
   * Open file handles, write data, update stats, handle file rolling and
   * batching / flushing. <br />
   * If the write fails, the file is implicitly closed and then the IOException
   * is rethrown. <br />
   * We rotate before append, and not after, so that the active file rolling
   * mechanism will never roll an empty file. This also ensures that the file
   * creation time reflects when the first event was written.
   */
  public synchronized void append(KaaRecordEvent event) throws IOException, InterruptedException {
    if (!isOpen) {
      open(generateSerial(event));
    }

    // check if it's time to rotate the file
    if (shouldRotate()) {
      close();
      open(generateSerial(event));
    }

    // write the event
    try {
      sinkCounter.incrementEventDrainAttemptCount();
      writer.append(event); // could block
    } catch (IOException e) {
      LOG.warn("Caught IOException writing to HDFSWriter ({}). Closing file (" +
          bucketPath + IN_USE_EXT + ") and rethrowing exception.",
          e.getMessage());
      try {
        close();
      } catch (IOException e2) {
        LOG.warn("Caught IOException while closing file (" +
             bucketPath + IN_USE_EXT + "). Exception follows.", e2);
      }
      throw e;
    }

    // update statistics
    processSize += event.getBody().length;
    eventCounter++;
    batchCounter++;

    if (batchCounter == batchSize) {
      flush();
    }
  }

  public synchronized void appendBatch(List<KaaRecordEvent> events) throws IOException, InterruptedException {
	if (events.isEmpty()) {
		return;
	}
    if (!isOpen) {
      open(generateSerial(events.get(0)));
    }

    // check if it's time to rotate the file
    if (shouldRotate()) {
      close();
      open(generateSerial(events.get(0)));
    }

    // write the event
    try {
      sinkCounter.addToEventDrainAttemptCount(events.size());
      for (Event event : events) {
    	  writer.append(event); // could block
    	  processSize += event.getBody().length;
      }
    } catch (IOException e) {
      LOG.warn("Caught IOException writing to HDFSWriter ({}). Closing file (" +
          bucketPath + IN_USE_EXT + ") and rethrowing exception.",
          e.getMessage());
      try {
        close();
      } catch (IOException e2) {
        LOG.warn("Caught IOException while closing file (" +
             bucketPath + IN_USE_EXT + "). Exception follows.", e2);
      }
      throw e;
    }

    // update statistics

    eventCounter += events.size();
    batchCounter += events.size();

    if (batchCounter == batchSize) {
      flush();
    }
  }
  /**
   * check if time to rotate the file
   */
  private boolean shouldRotate() {
    boolean doRotate = false;

    if ((rollCount > 0) && (rollCount <= eventCounter)) {
      LOG.debug("rolling: rollCount: {}, events: {}", rollCount, eventCounter);
      doRotate = true;
    }

    if ((rollSize > 0) && (rollSize <= processSize)) {
      LOG.debug("rolling: rollSize: {}, bytes: {}", rollSize, processSize);
      doRotate = true;
    }

    return doRotate;
  }

  /**
   * Rename bucketPath file from .tmp to permanent location.
   */
  private void renameBucket() throws IOException {
    Path srcPath = new Path(bucketPath + IN_USE_EXT);
    Path dstPath = new Path(bucketPath + AVRO_EXT);
    if(fileSystem.exists(srcPath)) { // could block
      LOG.info("Renaming " + srcPath + " to " + dstPath);
      fileSystem.rename(srcPath, dstPath); // could block
    }
  }

  @Override
  public String toString() {
    return "[ " + this.getClass().getSimpleName() + " filePath = " + filePath +
        ", bucketPath = " + bucketPath + " ]";
  }

  private boolean isBatchComplete() {
    return batchCounter == 0;
  }

  private long generateSerial (Event event) {
	  long timestamp = System.currentTimeMillis();
	  return Arrays.hashCode(event.getBody()) + ManagementFactory.getRuntimeMXBean().getName().hashCode() + (int)(timestamp ^ (timestamp >>> 32));
  }
}
