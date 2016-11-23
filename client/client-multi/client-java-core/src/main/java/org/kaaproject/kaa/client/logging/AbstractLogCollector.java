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

package org.kaaproject.kaa.client.logging;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.memory.MemLogStorage;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Reference implementation of @see LogCollector.
 *
 * @author Andrew Shvayka
 */
public abstract class AbstractLogCollector implements LogCollector, LogProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractLogCollector.class);

  protected final ExecutorContext executorContext;
  protected final Map<Integer, List<RecordFuture>> deliveryFuturesMap = new HashMap<>();
  protected final Map<Integer, BucketInfo> bucketInfoMap = new ConcurrentHashMap<>();
  private final LogTransport transport;
  private final ConcurrentHashMap<Integer, Future<?>> timeouts = new ConcurrentHashMap<>();
  private final KaaChannelManager channelManager;
  private final FailoverManager failoverManager;
  private final LogFailoverCommand controller;
  private final Object uploadCheckLock = new Object();
  protected LogStorage storage;
  private LogUploadStrategy strategy;
  private LogDeliveryListener logDeliveryListener;
  private boolean uploadCheckInProgress = false;

  /**
   * All-args constructor.
   */
  public AbstractLogCollector(LogTransport transport, ExecutorContext executorContext,
                              KaaChannelManager channelManager, FailoverManager failoverManager) {
    this.strategy = new DefaultLogUploadStrategy();
    this.storage = new MemLogStorage();
    this.controller = new DefaultLogUploadController();
    this.channelManager = channelManager;
    this.transport = transport;
    this.executorContext = executorContext;
    this.failoverManager = failoverManager;
  }

  @Override
  public void setStrategy(LogUploadStrategy strategy) {
    if (strategy == null) {
      throw new IllegalArgumentException("Strategy is null!");
    }
    this.strategy = strategy;
    LOG.info("New log upload strategy was set: {}", strategy);
  }

  @Override
  public void setStorage(LogStorage storage) {
    if (storage == null) {
      throw new IllegalArgumentException("Storage is null!");
    }
    this.storage = storage;
    LOG.info("New log storage was set {}", storage);
  }

  @Override
  public void fillSyncRequest(LogSyncRequest request) {
    if (!isUploadAllowed()) {
      return;
    }

    LogBucket bucket = storage.getNextBucket();

    if (bucket == null || bucket.getRecords().isEmpty()) {
      LOG.trace("No logs to send");
      return;
    }

    List<LogRecord> recordList = bucket.getRecords();

    LOG.trace("Sending {} log records", recordList.size());

    List<LogEntry> logs = new LinkedList<>();
    for (LogRecord record : recordList) {
      logs.add(new LogEntry(ByteBuffer.wrap(record.getData())));
    }

    request.setRequestId(bucket.getBucketId());
    request.setLogEntries(logs);

    final int bucketId = bucket.getBucketId();
    Future<?> timeoutFuture = executorContext.getScheduledExecutor().schedule(new Runnable() {
      @Override
      public void run() {
        if (!Thread.currentThread().isInterrupted()) {
          checkDeliveryTimeout(bucketId);
        } else {
          LOG.debug("Timeout check worker for log bucket: {} was interrupted", bucketId);
        }
      }
    }, strategy.getTimeout(), TimeUnit.SECONDS);

    LOG.info("Adding following bucket id [{}] for timeout tracking", bucket.getBucketId());
    timeouts.put(bucket.getBucketId(), timeoutFuture);
  }

  @Override
  public void onLogResponse(LogSyncResponse logSyncResponse) throws IOException {
    if (logSyncResponse.getDeliveryStatuses() != null) {
      boolean isAlreadyScheduled = false;
      for (LogDeliveryStatus response : logSyncResponse.getDeliveryStatuses()) {
        final int requestId = response.getRequestId();
        final BucketInfo bucketInfo = bucketInfoMap.get(requestId);
        final long arriveTime = System.currentTimeMillis();
        if (bucketInfo != null) {
          bucketInfoMap.remove(requestId);
          if (response.getResult() == SyncResponseResultType.SUCCESS) {
            storage.removeBucket(response.getRequestId());

            if (logDeliveryListener != null) {
              executorContext.getCallbackExecutor().execute(new Runnable() {
                @Override
                public void run() {
                  logDeliveryListener.onLogDeliverySuccess(bucketInfo);
                }
              });
            }

            executorContext.getCallbackExecutor().execute(new Runnable() {
              @Override
              public void run() {
                notifyDeliveryFuturesOnSuccess(bucketInfo, arriveTime);
              }
            });
          } else {
            storage.rollbackBucket(response.getRequestId());

            final LogDeliveryErrorCode errorCode = response.getErrorCode();
            final LogFailoverCommand controller = this.controller;

            executorContext.getCallbackExecutor().execute(new Runnable() {
              @Override
              public void run() {
                strategy.onFailure(controller, errorCode);
              }
            });

            if (logDeliveryListener != null) {
              executorContext.getCallbackExecutor().execute(new Runnable() {
                @Override
                public void run() {
                  logDeliveryListener.onLogDeliveryFailure(bucketInfo);
                }
              });
            }

            isAlreadyScheduled = true;
          }
        } else {
          LOG.warn("BucketInfo is null");
        }
        LOG.info("Removing bucket id from timeouts: {}", response.getRequestId());
        Future<?> timeoutFuture = timeouts.remove(response.getRequestId());
        if (timeoutFuture != null) {
          timeoutFuture.cancel(true);
        } else {
          LOG.warn("TimeoutFuture is null and cannot be canceled");
        }
      }

      if (!isAlreadyScheduled) {
        processUploadDecision(strategy.isUploadNeeded(storage.getStatus()));
      }
    }
  }

  @Override
  public void stop() {
    LOG.debug("Closing storage");
    storage.close();
    LOG.debug("Clearing timeouts map");
    for (Future<?> timeoutFuture : timeouts.values()) {
      timeoutFuture.cancel(true);
    }
    timeouts.clear();
  }

  private void processUploadDecision(LogUploadStrategyDecision decision) {
    switch (decision) {
      case UPLOAD:
        if (isUploadAllowed()) {
          LOG.debug("Going to upload logs");
          transport.sync();
        }
        break;
      case NOOP:
        if (strategy.getUploadCheckPeriod() > 0 && storage.getStatus().getRecordCount() > 0) {
          scheduleUploadCheck();
        }
        break;
      default:
        break;
    }
  }

  protected void scheduleUploadCheck() {
    LOG.trace("Attempt to execute upload check: {}", uploadCheckInProgress);
    synchronized (uploadCheckLock) {
      if (!uploadCheckInProgress) {
        LOG.trace("Scheduling upload check with timeout: {}", strategy.getUploadCheckPeriod());
        uploadCheckInProgress = true;
        executorContext.getScheduledExecutor().schedule(new Runnable() {
          @Override
          public void run() {
            synchronized (uploadCheckLock) {
              uploadCheckInProgress = false;
            }
            uploadIfNeeded();
          }
        }, strategy.getUploadCheckPeriod(), TimeUnit.SECONDS);
      } else {
        LOG.trace("Upload check is already scheduled!");
      }
    }
  }

  private void checkDeliveryTimeout(final int bucketId) {
    LOG.debug("Checking for a delivery timeout of the bucket with id: [{}] ", bucketId);
    Future<?> timeoutFuture = timeouts.remove(bucketId);

    if (timeoutFuture != null) {
      LOG.info("Log delivery timeout detected for the bucket with id: [{}]", bucketId);

      storage.rollbackBucket(bucketId);

      final LogFailoverCommand controller = this.controller;
      executorContext.getCallbackExecutor().execute(new Runnable() {
        @Override
        public void run() {
          strategy.onTimeout(controller);
        }
      });

      if (logDeliveryListener != null) {
        executorContext.getCallbackExecutor().execute(new Runnable() {
          @Override
          public void run() {
            logDeliveryListener.onLogDeliveryTimeout(bucketInfoMap.get(bucketId));
          }
        });
      }

      timeoutFuture.cancel(true);
    } else {
      LOG.trace("No log delivery timeout for the bucket with id [{}] was detected", bucketId);
    }
  }

  private boolean isUploadAllowed() {
    if (timeouts.size() >= strategy.getMaxParallelUploads()) {
      LOG.debug("Ignore log upload: too much pending requests {}, max allowed {}",
              timeouts.size(), strategy.getMaxParallelUploads());
      return false;
    }
    return true;
  }

  protected void uploadIfNeeded() {
    processUploadDecision(strategy.isUploadNeeded(storage.getStatus()));
  }

  @Override
  public void setLogDeliveryListener(LogDeliveryListener logDeliveryListener) {
    this.logDeliveryListener = logDeliveryListener;
  }

  protected void addDeliveryFuture(BucketInfo info, RecordFuture future) {
    synchronized (deliveryFuturesMap) {
      List<RecordFuture> deliveryFutures = deliveryFuturesMap.get(info.getBucketId());
      if (deliveryFutures == null) {
        deliveryFutures = new LinkedList<RecordFuture>();
        deliveryFuturesMap.put(info.getBucketId(), deliveryFutures);
      }

      deliveryFutures.add(future);
    }
  }

  protected void notifyDeliveryFuturesOnSuccess(BucketInfo info, Long arriveTime) {
    synchronized (deliveryFuturesMap) {
      List<RecordFuture> deliveryFutures = deliveryFuturesMap.get(info.getBucketId());
      if (deliveryFutures != null) {
        for (RecordFuture future : deliveryFutures) {
          RecordInfo recordInfo = new RecordInfo(info);
          future.setValue(recordInfo, arriveTime);
        }

        deliveryFuturesMap.remove(info.getBucketId());
      }
    }
  }

  private class DefaultLogUploadController implements LogFailoverCommand {
    @Override
    public void switchAccessPoint() {
      TransportConnectionInfo server = channelManager.getActiveServer(TransportType.LOGGING);
      if (server != null) {
        failoverManager.onServerFailed(server, FailoverStatus.OPERATION_SERVERS_NA);
      } else {
        LOG.warn("Failed to switch Operation server. No channel is used for logging transport");
      }
    }

    @Override
    public void retryLogUpload() {
      uploadIfNeeded();
    }

    @Override
    public void retryLogUpload(int delay) {
      executorContext.getScheduledExecutor().schedule(new Runnable() {
        @Override
        public void run() {
          uploadIfNeeded();
        }
      }, delay, TimeUnit.SECONDS);
    }
  }
}
