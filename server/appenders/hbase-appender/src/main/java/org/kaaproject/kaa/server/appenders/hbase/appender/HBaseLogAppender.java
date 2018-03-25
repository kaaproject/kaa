package org.kaaproject.kaa.server.appenders.hbase.appender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.HBaseAppenderConfiguration;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Sample appender implementation that uses {@link CustomAppenderConfiguration} as configuration.
 *
 */

public class HBaseLogAppender extends AbstractLogAppender<HBaseAppenderConfiguration> {

  private static final Logger LOG = LoggerFactory.getLogger(HBaseLogAppender.class);
  private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;

  private ExecutorService executor;
  private ExecutorService callbackExecutor;

  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private AtomicInteger successLogCount = new AtomicInteger();
  private AtomicInteger failureLogCount = new AtomicInteger();
  private AtomicInteger inputLogCount = new AtomicInteger();

  private LogEventDao logEventDao;
  private String tableName;
  private boolean closed = false;

  private ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>> converters = 
      new ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>>() {

    @Override
    protected Map<String, GenericAvroConverter<GenericRecord>> initialValue() {
      return new HashMap<String, GenericAvroConverter<GenericRecord>>();
    }

  };

  /**
   * Instantiates a new HBase LogAppender.
   */

  public HBaseLogAppender() {
    super(HBaseAppenderConfiguration.class);
    scheduler.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        long second = System.currentTimeMillis() / 1000;
        LOG.info("[{}] Received {} log record count, {} success hbase callbacks, {}"
            + "  failure hbase callbacks / second.",
            second, inputLogCount.getAndSet(0), successLogCount.getAndSet(0), 
            failureLogCount.getAndSet(0));
      }
    }, 0L, 1L, TimeUnit.SECONDS);
  }
  /**
   * Inits the appender from configuration.
   *
   */

  @Override
  protected void initFromConfiguration(LogAppenderDto appender,
      HBaseAppenderConfiguration configuration) {
    //Do some initialization here.
    LOG.info("Initializing new instance of HBase log appender using {}", configuration);
    try {
      logEventDao = new HBaseLogEventDao(configuration);
      tableName = logEventDao.createHbTable();
      int executorPoolSize = MAX_CALLBACK_THREAD_POOL_SIZE;
      int callbackPoolSize = MAX_CALLBACK_THREAD_POOL_SIZE;    
      executor = Executors.newFixedThreadPool(executorPoolSize);
      callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);
      LOG.info("HBase log appender initialized");
    } catch (Exception ex) {
      LOG.error("Failed to init HBase log appender: ", ex);
    }
  }
  /**
   * Consumes and delivers logs.
   *
   * @param logEventPack container for log events with some metadata like log event schema.
   */

  @Override
  public void doAppend(LogEventPack logEventPack, RecordHeader header,
      LogDeliveryCallback listener) {
    //Append logs to your system here.
    if (!closed) {
      executor.submit(new Runnable() {
        @Override
        public void run() {
          try {
            LOG.debug("[{}] appending {} logs to HBase ", tableName, 
                logEventPack.getEvents().size());
            GenericAvroConverter<GenericRecord> eventConverter = 
                getConverter(logEventPack.getLogSchema().getSchema());
            GenericAvroConverter<GenericRecord> headerConverter = 
                getConverter(header.getSchema().toString());

            List<HBaseLogEventDto> dtoList = generateCustomLogEvent(
                logEventPack, header, eventConverter);
            LOG.debug("[{}] saving {} objects", tableName, dtoList.size());
            if (!dtoList.isEmpty()) {
              int logCount = dtoList.size();
              inputLogCount.getAndAdd(logCount);
              logEventDao.save(dtoList, tableName, eventConverter, headerConverter);
              listener.onSuccess();
              successLogCount.getAndAdd(logCount);
              LOG.debug("[{}] appended {} logs to HBase collection", tableName, 
                  logEventPack.getEvents().size());

            } else  {
              listener.onInternalError();
            }

          } catch (Exception ex) {
            LOG.warn("Got exception. Can't process log events", ex);
            listener.onInternalError();
          }
        }
      });


    } else {
      LOG.info("Attempted to append to closed appender named [{}].", getName());
      listener.onConnectionError();
    }
  }




  protected List<HBaseLogEventDto> generateCustomLogEvent(
      LogEventPack logEventPack, RecordHeader header,
      GenericAvroConverter<GenericRecord> eventConverter) throws IOException  {
    LOG.debug("Generate LogEventDto objects from LogEventPack [{}] and header [{}]",
        logEventPack, header);
    List<HBaseLogEventDto> events = new ArrayList<>(logEventPack.getEvents().size());
    try {
      for (LogEvent logEvent : logEventPack.getEvents()) {
        LOG.debug("Convert log events [{}] to dto objects.", logEvent);
        if (logEvent == null | logEvent.getLogData() == null) {
          continue;
        }
        LOG.trace("Avro record converter [{}] with log data [{}]", 
            eventConverter, logEvent.getLogData());
        GenericRecord decodedLog = eventConverter.decodeBinary(logEvent.getLogData());
        events.add(new HBaseLogEventDto(header, decodedLog));
      }
    } catch (IOException ex) {
      LOG.error("Unexpected IOException while decoding LogEvents", ex);
      throw ex;
    }
    return events;
  }

  /**
   * Gets the converter.
   *
   * @param schema the schema
   * @return the converter
   */

  private GenericAvroConverter<GenericRecord> getConverter(String schema) {
    LOG.trace("Get converter for schema [{}]", schema);
    Map<String, GenericAvroConverter<GenericRecord>> converterMap = converters.get();
    GenericAvroConverter<GenericRecord> genAvroConverter = converterMap.get(schema);
    if (genAvroConverter == null) {
      LOG.trace("Create new converter for schema [{}]", schema);
      genAvroConverter = new GenericAvroConverter<GenericRecord>(schema);
      converterMap.put(schema, genAvroConverter);
      converters.set(converterMap);
    }
    LOG.trace("Get converter [{}] from map.", genAvroConverter);
    return genAvroConverter;
  }
  /**
   * Closes this appender and releases any resources associated with it.
   *
   */
  
  @Override
  public void close() {
    LOG.info("Try to stop hbase log appender...");
    if (!closed) {
      closed = true;
      if (logEventDao != null) {
        logEventDao.close();
      }
      if (executor != null) {
        executor.shutdownNow();
      }
      if (callbackExecutor != null) {
        callbackExecutor.shutdownNow();
      }
      if (scheduler != null) {
        scheduler.shutdownNow();
      }
    }
    LOG.info("HBase log appender stoped.");
  }
}
