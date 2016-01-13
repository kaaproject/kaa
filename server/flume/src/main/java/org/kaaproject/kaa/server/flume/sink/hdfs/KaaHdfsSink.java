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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.sink.hdfs.HDFSDataStream;
import org.apache.flume.sink.hdfs.HDFSWriter;
import org.apache.flume.sink.hdfs.KerberosUser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod;
import org.kaaproject.kaa.server.flume.ConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class KaaHdfsSink extends AbstractSink implements Configurable, ConfigurationConstants,
                                                           RemovalListener<HdfsSinkKey, BucketWriter> {

      private static final Logger LOG = LoggerFactory.getLogger(KaaHdfsSink.class);

      private boolean started = false;

      private KaaEventFactory eventFactory;
      private Context context;
      private SinkCounter sinkCounter;

      //writers
      private LoadingCache<HdfsSinkKey, BucketWriter> writerCache;
      private BucketWriterLoader bucketWriterLoader;
      private Map<HdfsSinkKey, BucketWriter> writerFlushMap;
      private ExecutorService callTimeoutPool;
      private ScheduledThreadPoolExecutor timedRollerPool;
      private ScheduledExecutorService statisticsPool;
      private volatile ScheduledFuture<?> statisticsFuture;
      private long cacheCleanupStartInterval;

      /**
       * Singleton credential manager that manages static credentials for the
       * entire JVM
       */
      private static final AtomicReference<KerberosUser> staticLogin = new AtomicReference<>(); //NOSONAR

      private String kerbConfPrincipal;
      private String kerbKeytab;
      private String proxyUserName;
      private UserGroupInformation proxyTicket;

      //configurable part

      private String rootHdfsPath;
      private long txnEventMax;

      // writers configuration
      private long callTimeout;
      private int threadsPoolSize;
      private int rollTimerPoolSize;
      private int maxOpenFiles;
      private long cacheCleanupInterval;
      private int writerExpirationInterval;
      private long rollInterval;
      private long rollSize;
      private long rollCount;
      private long batchSize;
      private long defaultBlockSize;
      private String filePrefix;
      private long statisticsInterval;

      public KaaHdfsSink() {
      }

      @Override
      public void configure(Context context) {
        this.context = context;
        rootHdfsPath = context.getString(CONFIG_ROOT_HDFS_PATH, DEFAULT_ROOT_HDFS_PATH);
        Preconditions.checkNotNull(rootHdfsPath, "rootHdfsPath is required");
        txnEventMax = context.getLong(CONFIG_HDFS_TXN_EVENT_MAX, DEFAULT_HDFS_TXN_EVENT_MAX);
        statisticsInterval = context.getLong(CONFIG_STATISTICS_INTERVAL, DEFAULT_STATISTICS_INTERVAL);

        // writers
        threadsPoolSize = context.getInteger(CONFIG_HDFS_THREAD_POOL_SIZE, DEFAULT_HDFS_THREAD_POOL_SIZE);
        rollTimerPoolSize = context.getInteger(CONFIG_HDFS_ROLL_TIMER_POOL_SIZE, DEFAULT_HDFS_ROLL_TIMER_POOL_SIZE);
        maxOpenFiles = context.getInteger(CONFIG_HDFS_MAX_OPEN_FILES, DEFAULT_HDFS_MAX_OPEN_FILES);
        cacheCleanupInterval = context.getInteger(CONFIG_HDFS_CACHE_CLEANUP_INTERVAL, DEFAULT_HDFS_CACHE_CLEANUP_INTERVAL) * 1000;
        writerExpirationInterval = context.getInteger(CONFIG_HDFS_WRITER_EXPIRATION_INTERVAL, DEFAULT_HDFS_WRITER_EXPIRATION_INTERVAL);
        callTimeout = context.getLong(CONFIG_HDFS_CALL_TIMEOUT, DEFAULT_HDFS_CALL_TIMEOUT);

        rollInterval = context.getLong(CONFIG_HDFS_ROLL_INTERVAL, DEFAULT_HDFS_ROLL_INTERVAL);
        rollSize = context.getLong(CONFIG_HDFS_ROLL_SIZE, DEFAULT_HDFS_ROLL_SIZE);
        rollCount = context.getLong(CONFIG_HDFS_ROLL_COUNT, DEFAULT_HDFS_ROLL_COUNT);
        batchSize = context.getLong(CONFIG_HDFS_BATCH_SIZE, DEFAULT_HDFS_BATCH_SIZE);
        defaultBlockSize = context.getLong(CONFIG_HDFS_DEFAULT_BLOCK_SIZE, DEFAULT_HDFS_DEFAULT_BLOCK_SIZE);

        filePrefix = context.getString(CONFIG_HDFS_FILE_PREFIX, DEFAULT_HDFS_FILE_PREFIX);

        Preconditions.checkArgument(batchSize > 0,
                "batchSize must be greater than 0");
        Preconditions.checkArgument(txnEventMax > 0,
            "txnEventMax must be greater than 0");

        kerbConfPrincipal = context.getString(CONFIG_HDFS_KERBEROS_PRINCIPAL, "");
        kerbKeytab = context.getString(CONFIG_HDFS_KERBEROS_KEYTAB, "");
        proxyUserName = context.getString(CONFIG_HDFS_PROXY_USER, "");

        if (!authenticate()) {
            LOG.error("Failed to authenticate!");
        }

        if (sinkCounter == null) {
            sinkCounter = new SinkCounter(getName());
        }
      }

      /**
       * Execute the callable on a separate thread and wait for the completion
       * for the specified amount of time in milliseconds. In case of timeout
       * cancel the callable and throw an IOException
       */
      private <T> T callWithTimeout(Callable<T> callable)
          throws IOException, InterruptedException {
        Future<T> future = callTimeoutPool.submit(callable);
        try {
          if (callTimeout > 0) {
            return future.get(callTimeout, TimeUnit.MILLISECONDS);
          } else {
            return future.get();
          }
        } catch (TimeoutException eT) {
          future.cancel(true);
          sinkCounter.incrementConnectionFailedCount();
          throw new IOException("Callable timed out after " + callTimeout + " ms",
              eT);
        } catch (ExecutionException e1) {
          sinkCounter.incrementConnectionFailedCount();
          Throwable cause = e1.getCause();
          if (cause instanceof IOException) {
            throw (IOException) cause;
          } else if (cause instanceof InterruptedException) {
            throw (InterruptedException) cause;
          } else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          } else if (cause instanceof Error) {
            throw (Error)cause;
          } else {
            throw new RuntimeException(e1);
          }
        } catch (CancellationException ce) {
            LOG.error("Exception catched: ", ce);
          throw new InterruptedException(
              "Blocked callable interrupted by rotation event");
        } catch (InterruptedException ex) {
          LOG.warn("Unexpected Exception " + ex.getMessage(), ex);
          throw ex;
        }
      }

      @Override
      public Status process() throws EventDeliveryException {
            Channel channel = getChannel();
            Transaction transaction = channel.getTransaction();
            transaction.begin();
            try {
              Event event = null;
              int txnEventCount = 0;
              int sinkEventCount = 0;
              for (txnEventCount = 0; txnEventCount < txnEventMax; txnEventCount++) {
                event = channel.take();
                if (event == null) {
                    if((System.currentTimeMillis() - cacheCleanupStartInterval) >= cacheCleanupInterval){
                        LOG.info("Starting Writer cache cleanup.");
                        writerCache.cleanUp();
                        timedRollerPool.purge();
                        cacheCleanupStartInterval = System.currentTimeMillis();
                    }
                    break;
                }
                //else{
                //  cacheCleanupStartInterval = System.currentTimeMillis();
                //}

                Map<KaaSinkKey, List<KaaRecordEvent>> incomingEventsMap = eventFactory.processIncomingFlumeEvent(event);
                if (incomingEventsMap == null || incomingEventsMap.isEmpty()) {
                      if (LOG.isWarnEnabled()) {
                          LOG.warn("Unable to parse incoming event: " + event);
                      }
                      continue;
                }
                  for (KaaSinkKey key : incomingEventsMap.keySet()) {
                      HdfsSinkKey hdfsSinkKey = new HdfsSinkKey(rootHdfsPath, key);
                      BucketWriter bucketWriter;
                      // track the buckets getting written in this transaction
                      bucketWriter = writerCache.get(hdfsSinkKey);
                      writerFlushMap.put(hdfsSinkKey, bucketWriter);
                      // Write the data to HDFS
                      List<KaaRecordEvent> events = incomingEventsMap.get(key);
                      sinkEventCount += events.size();
                      appendBatch(bucketWriter, events);
                  }

              }
              if (txnEventCount == 0) {
                sinkCounter.incrementBatchEmptyCount();
              } else if (txnEventCount == txnEventMax) {
                sinkCounter.incrementBatchCompleteCount();
              } else {
                sinkCounter.incrementBatchUnderflowCount();
              }

              // flush all pending buckets before committing the transaction
              for (BucketWriter bucketWriter : writerFlushMap.values()) {
                  flush(bucketWriter);
              }

              writerFlushMap.clear();

              transaction.commit();

              if (sinkEventCount > 0) {
                sinkCounter.addToEventDrainSuccessCount(sinkEventCount);
              }

              if(event == null) {
                return Status.BACKOFF;
              }
              return Status.READY;
            } catch (IOException eIO) {
              transaction.rollback();
              LOG.warn("HDFS IO error", eIO);
              return Status.BACKOFF;
            } catch (Throwable th) { //NOSONAR
              transaction.rollback();
              LOG.error("process failed", th);
              if (th instanceof Error) {
                throw (Error) th;
              } else {
                throw new EventDeliveryException(th);
              }
            } finally {
              transaction.close();
            }
      }

      @Override
      public void start() {
        LOG.info("Starting {}...", this);

        eventFactory = new KaaEventFactory();

        String timeoutName = "hdfs-" + getName() + "-call-runner-%d";
        callTimeoutPool = Executors.newFixedThreadPool(threadsPoolSize,
            new ThreadFactoryBuilder().setNameFormat(timeoutName).build());

        String rollerName = "hdfs-" + getName() + "-roll-timer-%d";
        timedRollerPool = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(rollTimerPoolSize,
            new ThreadFactoryBuilder().setNameFormat(rollerName).build());

        if (statisticsInterval > 0) {
            String statisticsName = "hdfs-" + getName() + "-statistics-%d";
            statisticsPool = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder().setNameFormat(statisticsName).build());

              Runnable action = new Runnable() {
                  @Override
                  public void run() {
                      LOG.info("Statistics: Drain attempt events: " + sinkCounter.getEventDrainAttemptCount() + "; " +
                              "Drain success events: " + sinkCounter.getEventDrainSuccessCount());
                  }
                };
                statisticsFuture = statisticsPool.scheduleWithFixedDelay(action, 0, statisticsInterval, TimeUnit.SECONDS);
        }

        cacheCleanupStartInterval = System.currentTimeMillis();

        bucketWriterLoader = new BucketWriterLoader(rollInterval,
                     rollSize,
                     rollCount,
                     batchSize,
                     defaultBlockSize,
                     context,
                     filePrefix,
                     timedRollerPool,
                     proxyTicket,
                     sinkCounter);

        writerCache = CacheBuilder.newBuilder().
                maximumSize(maxOpenFiles).
                expireAfterWrite(writerExpirationInterval, TimeUnit.SECONDS).
                removalListener(this).
                build(bucketWriterLoader);

        writerFlushMap = new HashMap<HdfsSinkKey, BucketWriter>();

        sinkCounter.start();
        started = true;
        super.start();

        LOG.info("Kaa Hdfs Sink {} started.", getName());
      }

      @Override
      public void stop() {

        started = false;
       // do not constrain close() calls with a timeout
        Map<HdfsSinkKey, BucketWriter> writers = writerCache.asMap();
        for (Entry<HdfsSinkKey, BucketWriter> entry : writers.entrySet()) {
          LOG.info("Closing {}", entry.getKey());

          try {
            close(entry.getValue());
          } catch (Exception ex) {
            LOG.warn("Exception while closing " + entry.getKey() + ". " +
                    "Exception follows.", ex);
            if (ex instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
          }
        }
        if (statisticsFuture != null && !statisticsFuture.isDone()) {
            statisticsFuture.cancel(false); // do not cancel myself if running!
            statisticsFuture = null;
          }
        // shut down all our thread pools
        ExecutorService toShutdown[] = { callTimeoutPool, timedRollerPool, statisticsPool };
        for (ExecutorService execService : toShutdown) {
         if (execService != null) {
          execService.shutdown();
          try {
            while (execService.isTerminated() == false) {
              execService.awaitTermination(
                  Math.max(ConfigurationConstants.DEFAULT_HDFS_CALL_TIMEOUT, callTimeout), TimeUnit.MILLISECONDS);
            }
          } catch (InterruptedException ex) {
            LOG.warn("shutdown interrupted on " + execService, ex);
          }
         }
        }

        callTimeoutPool = null;
        timedRollerPool = null;
        statisticsPool = null;

        writerCache.cleanUp();
        writerCache = null;
        sinkCounter.stop();
        super.stop();
      }

      @Override
      public String toString() {
           return "{ Sink type:" + getClass().getSimpleName() + ", name:" + getName() +
                    " }";
      }

      public long getEventDrainSuccessCount () {
          return sinkCounter.getEventDrainSuccessCount();
      }

        @Override
        public void onRemoval(
                RemovalNotification<HdfsSinkKey, BucketWriter> entry) {
            if (started) {
                RemovalCause cause = entry.getCause();
                HdfsSinkKey key = entry.getKey();
                BucketWriter writer = entry.getValue();
                LOG.info("Stopping removed writer because of " + cause + " for key: " + entry.getKey());
               try {
                   writerFlushMap.remove(key);
                   writer.close();
                 } catch (IOException e) {
                   LOG.warn(entry.getKey().toString(), e);
                 } catch (InterruptedException e) {
                   LOG.warn(entry.getKey().toString(), e);
                   Thread.currentThread().interrupt();
                 }
            }
        }

          private boolean authenticate() {

                // logic for kerberos login
                boolean useSecurity = UserGroupInformation.isSecurityEnabled();

                LOG.info("Hadoop Security enabled: " + useSecurity);

                if (useSecurity) {

                  // sanity checking
                  if (kerbConfPrincipal.isEmpty()) {
                      LOG.error("Hadoop running in secure mode, but Flume config doesn't "
                              + "specify a principal to use for Kerberos auth.");
                    return false;
                  }
                  if (kerbKeytab.isEmpty()) {
                    LOG.error("Hadoop running in secure mode, but Flume config doesn't "
                            + "specify a keytab to use for Kerberos auth.");
                    return false;
                  } else {
                    //If keytab is specified, user should want it take effect.
                    //HDFSSink will halt when keytab file is non-exist or unreadable
                    File kfile = new File(kerbKeytab);
                    if (!(kfile.isFile() && kfile.canRead())) {
                      throw new IllegalArgumentException("The keyTab file: "
                          + kerbKeytab + " is nonexistent or can't read. "
                          + "Please specify a readable keytab file for Kerberos auth.");
                    }
                  }

                  String principal;
                  try {
                    // resolves _HOST pattern using standard Hadoop search/replace
                    // via DNS lookup when 2nd argument is empty
                    principal = SecurityUtil.getServerPrincipal(kerbConfPrincipal, "");
                  } catch (IOException e) {
                    LOG.error("Host lookup error resolving kerberos principal ("
                            + kerbConfPrincipal + "). Exception follows.", e);
                    return false;
                  }

                  Preconditions.checkNotNull(principal, "Principal must not be null");
                  KerberosUser prevUser = staticLogin.get();
                  KerberosUser newUser = new KerberosUser(principal, kerbKeytab);

                  // be cruel and unusual when user tries to login as multiple principals
                  // this isn't really valid with a reconfigure but this should be rare
                  // enough to warrant a restart of the agent JVM
                  // TODO: find a way to interrogate the entire current config state,
                  // since we don't have to be unnecessarily protective if they switch all
                  // HDFS sinks to use a different principal all at once.
                  Preconditions.checkState(prevUser == null || prevUser.equals(newUser),
                      "Cannot use multiple kerberos principals in the same agent. " +
                      " Must restart agent to use new principal or keytab. " +
                      "Previous = %s, New = %s", prevUser, newUser);

                  // attempt to use cached credential if the user is the same
                  // this is polite and should avoid flooding the KDC with auth requests
                  UserGroupInformation curUser = null;
                  if (prevUser != null && prevUser.equals(newUser)) {
                    try {
                      curUser = UserGroupInformation.getLoginUser();
                    } catch (IOException e) {
                      LOG.warn("User unexpectedly had no active login. Continuing with " +
                              "authentication", e);
                    }
                  }

                  if (curUser == null || !curUser.getUserName().equals(principal)) {
                    try {
                      // static login
                      kerberosLogin(this, principal, kerbKeytab);
                    } catch (IOException e) {
                      LOG.error("Authentication or file read error while attempting to "
                              + "login as kerberos principal (" + principal + ") using "
                              + "keytab (" + kerbKeytab + "). Exception follows.", e);
                      return false;
                    }
                  } else {
                    LOG.debug("{}: Using existing principal login: {}", this, curUser);
                  }

                  // we supposedly got through this unscathed... so store the static user
                  staticLogin.set(newUser);
                }

                // hadoop impersonation works with or without kerberos security
                proxyTicket = null;
                if (!proxyUserName.isEmpty()) {
                  try {
                    proxyTicket = UserGroupInformation.createProxyUser(
                        proxyUserName, UserGroupInformation.getLoginUser());
                  } catch (IOException e) {
                    LOG.error("Unable to login as proxy user. Exception follows.", e);
                    return false;
                  }
                }

                UserGroupInformation ugi = null;
                if (proxyTicket != null) {
                  ugi = proxyTicket;
                } else if (useSecurity) {
                  try {
                    ugi = UserGroupInformation.getLoginUser();
                  } catch (IOException e) {
                    LOG.error("Unexpected error: Unable to get authenticated user after " +
                            "apparent successful login! Exception follows.", e);
                    return false;
                  }
                }

                if (ugi != null) {
                  // dump login information
                  AuthenticationMethod authMethod = ugi.getAuthenticationMethod();
                  LOG.info("Auth method: {}", authMethod);
                  LOG.info(" User name: {}", ugi.getUserName());
                  LOG.info(" Using keytab: {}", ugi.isFromKeytab());
                  if (authMethod == AuthenticationMethod.PROXY) {
                    UserGroupInformation superUser;
                    try {
                      superUser = UserGroupInformation.getLoginUser();
                      LOG.info(" Superuser auth: {}", superUser.getAuthenticationMethod());
                      LOG.info(" Superuser name: {}", superUser.getUserName());
                      LOG.info(" Superuser using keytab: {}", superUser.isFromKeytab());
                    } catch (IOException e) {
                        LOG.error("Unexpected error: unknown superuser impersonating proxy.",
                                e);
                      return false;
                    }
                  }

                  LOG.info("Logged in as user {}", ugi.getUserName());

                  return true;
                }

                return true;
              }

          /**
           * Static synchronized method for static Kerberos login. <br/>
           * Static synchronized due to a thundering herd problem when multiple Sinks
           * attempt to log in using the same principal at the same time with the
           * intention of impersonating different users (or even the same user).
           * If this is not controlled, MIT Kerberos v5 believes it is seeing a replay
           * attach and it returns:
           * <blockquote>Request is a replay (34) - PROCESS_TGS</blockquote>
           * In addition, since the underlying Hadoop APIs we are using for
           * impersonation are static, we define this method as static as well.
           *
           * @param principal Fully-qualified principal to use for authentication.
           * @param keytab Location of keytab file containing credentials for principal.
           * @return Logged-in user
           * @throws IOException if login fails.
           */
          private static synchronized UserGroupInformation kerberosLogin(
              KaaHdfsSink sink, String principal, String keytab) throws IOException {

            // if we are the 2nd user thru the lock, the login should already be
            // available statically if login was successful
            UserGroupInformation curUser = null;
            try {
              curUser = UserGroupInformation.getLoginUser();
            } catch (IOException e) {
              // not a big deal but this shouldn't typically happen because it will
              // generally fall back to the UNIX user
              LOG.debug("Unable to get login user before Kerberos auth attempt.", e);
            }

            // we already have logged in successfully
            if (curUser != null && curUser.getUserName().equals(principal)) {
                LOG.debug("{}: Using existing principal ({}): {}",
                        new Object[]{sink, principal, curUser});

            // no principal found
            } else {

                LOG.info("{}: Attempting kerberos login as principal ({}) from keytab " +
                        "file ({})", new Object[]{sink, principal, keytab});

              // attempt static kerberos login
              UserGroupInformation.loginUserFromKeytab(principal, keytab);
              curUser = UserGroupInformation.getLoginUser();
            }

            return curUser;
          }


          /**
           * Append to bucket writer with timeout enforced
           */
          private void appendBatch(final BucketWriter bucketWriter, final List<KaaRecordEvent> events)
                  throws IOException, InterruptedException {
                // Write the data to HDFS
                callWithTimeout(new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    bucketWriter.appendBatch(events);
                    return null;
                  }
                });
              }

          /**
           * Flush bucket writer with timeout enforced
           */
          private void flush(final BucketWriter bucketWriter)
              throws IOException, InterruptedException {

            callWithTimeout(new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                bucketWriter.flush();
                return null;
              }
            });
          }

          /**
           * Close bucket writer with timeout enforced
           */
          private void close(final BucketWriter bucketWriter)
              throws IOException, InterruptedException {

            callWithTimeout(new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                bucketWriter.close();
                return null;
              }
            });
          }

      static class BucketWriterLoader extends CacheLoader<HdfsSinkKey, BucketWriter> {

          private final long rollInterval;
          private final long rollSize;
          private final long rollCount;
          private final long batchSize;
          private final long defaultBlockSize;
          private final Context context;
          private final String filePrefix;
          private final ScheduledThreadPoolExecutor timedRollerPool;
          private final UserGroupInformation proxyTicket;
          private final SinkCounter sinkCounter;

          public BucketWriterLoader (long rollInterval,
                                     long rollSize,
                                     long rollCount,
                                     long batchSize,
                                     long defaultBlockSize,
                                     Context context,
                                     String filePrefix,
                                     ScheduledThreadPoolExecutor timedRollerPool,
                                     UserGroupInformation proxyTicket,
                                     SinkCounter sinkCounter) {
              this.rollInterval = rollInterval;
              this.rollSize = rollSize;
              this.rollCount = rollCount;
              this.batchSize = batchSize;
              this.defaultBlockSize = defaultBlockSize;
              this.context = context;
              this.filePrefix = filePrefix;
              this.timedRollerPool = timedRollerPool;
              this.proxyTicket = proxyTicket;
              this.sinkCounter = sinkCounter;
          }


        @Override
        public BucketWriter load(HdfsSinkKey key) throws Exception {

            HDFSWriter hdfsWriter = new HDFSDataStream();
            String path = key.getPath() + Path.SEPARATOR + filePrefix;

            context.put("serializer", AvroKaaEventSerializer.Builder.class.getName());

            LOG.info("Creating new writer for key: " + key);

            return new BucketWriter(rollInterval, rollSize, rollCount,
                      batchSize, defaultBlockSize, context, path, hdfsWriter,
                      timedRollerPool, proxyTicket, sinkCounter);
        }

      }

    }