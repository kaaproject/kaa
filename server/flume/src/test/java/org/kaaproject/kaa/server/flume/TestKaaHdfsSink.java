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
package org.kaaproject.kaa.server.flume;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.mapred.FsInput;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.Sink;
import org.apache.flume.SinkProcessor;
import org.apache.flume.SinkRunner;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.conf.Configurables;
import org.apache.flume.sink.DefaultSinkProcessor;
import org.apache.flume.source.AvroSource;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobConf;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordData;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.flume.channel.KaaLoadChannelSelector;
import org.kaaproject.kaa.server.flume.sink.hdfs.KaaHdfsSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TestKaaHdfsSink {

    private static final Logger LOG = LoggerFactory.getLogger(TestKaaHdfsSink.class);

    private static AvroSource source;
    private static KaaHdfsSink sink;
    private static Channel channel;
    private static MiniDFSCluster dfsCluster = null;
    private static FileSystem fileSystem = null;

    private static String applicationToken = "42342342";
    private static byte[] endpointKeyHash = new byte[]{6,3,8,4,7,5,3,6};
    private static int logSchemaVersion = 1;
    private static File logSchemasRootDir;

    private SinkRunner sinkRunner;

    private long flushRecordsCountSmall = 1000;
    private long blockSizeSmall = 2097152;

    private long flushRecordsCount = flushRecordsCountSmall;
    private long blockSize = blockSizeSmall;


    @BeforeClass
    public static void setUp() throws IOException {
        int dataNodes = 1;
        int port = 29999;
        JobConf conf = new JobConf();
        channel = new MemoryChannel();
        conf.set("dfs.block.access.token.enable", "false");
        conf.set("dfs.permissions", "true");
        conf.set("hadoop.security.authentication", "simple");
        conf.set("fs.default.name", "hdfs://localhost:29999");
        dfsCluster = new MiniDFSCluster(port, conf, dataNodes, true, true, null, null);
        fileSystem = dfsCluster.getFileSystem();
        fileSystem.delete(new Path("/logs"), true);
        source = new AvroSource();
        sink = new KaaHdfsSink();
        logSchemasRootDir = new File("schemas");
        if (logSchemasRootDir.exists()) {
            logSchemasRootDir.delete();
        }
        prepareSchema(logSchemasRootDir);
    }

    private static void prepareSchema(File rootDir) throws IOException {
        File schemaDir = new File(rootDir, ""+applicationToken);
        if (!schemaDir.exists()) {
            schemaDir.mkdirs();
        }
        File schemaFile = new File(schemaDir, "schema_v"+logSchemaVersion);
        FileUtils.write(schemaFile, TestLogData.getClassSchema().toString());
    }

    @Test
    public void testLogDataEvents() throws Exception {
        source.setName("testLogDataSource");
        sink.setName("testLogDataSink");
        Context context = prepareContext();
        runTestAndCheckResult(context);
    }

    private void runTestAndCheckResult(Context context) throws IOException {
        Configurables.configure(source, context);
        Configurables.configure(channel, context);

        ChannelSelector cs = new KaaLoadChannelSelector();
        cs.setChannels(Lists.newArrayList(channel));

        Configurables.configure(cs, context);

        source.setChannelProcessor(new ChannelProcessor(cs));

        Configurables.configure(sink, context);
        sink.setChannel(channel);

        sinkRunner = new SinkRunner();
        SinkProcessor policy = new DefaultSinkProcessor();
        List<Sink> sinks = new ArrayList<Sink>();
        sinks.add(sink);
        policy.setSinks(sinks);
        sinkRunner.setSink(policy);

        sinkRunner.start();
        source.start();

        RecordHeader header = new RecordHeader();
        header.setApplicationToken(applicationToken);
        header.setEndpointKeyHash(new String(endpointKeyHash));
        header.setHeaderVersion(1);
        header.setTimestamp(System.currentTimeMillis());

        List<TestLogData> testLogs = generateAndSendRecords(header);

        LOG.info("Sent records count: " + testLogs.size());
        LOG.info("Waiting for sink...");

        int maxWaitTime = 5000;
        int elapsed = 0;

        while (sink.getEventDrainSuccessCount() < testLogs.size() && elapsed < maxWaitTime) {
            try {
                Thread.sleep(1000);
                elapsed += 1000;
            } catch (InterruptedException e) {}
        }

        Assert.assertTrue(sink.getEventDrainSuccessCount() == testLogs.size());

        source.stop();
        sinkRunner.stop();

        readAndCheckResultsFromHdfs(header, testLogs);
    }

    private List<TestLogData> generateAndSendRecords(RecordHeader header) throws IOException {
        int count = 100;

        List<TestLogData> testLogs = new ArrayList<>();

        RecordData logData = new RecordData();

        logData.setRecordHeader(header);
        logData.setApplicationToken(applicationToken);
        logData.setSchemaVersion(logSchemaVersion);
        List<ByteBuffer> events = new ArrayList<>();

        SpecificDatumWriter<TestLogData> avroWriter = new SpecificDatumWriter<>(TestLogData.class);
        ByteArrayOutputStream baos;
        BinaryEncoder encoder = null;
        for (int i=0;i<count;i++) {
            TestLogData testLogData = new TestLogData();
            testLogData.setLevel(i%2==0 ? Level.INFO : Level.DEBUG);
            testLogData.setTag("TestKaaHdfsSink");
            testLogData.setMessage("Test log message # " + i);
            baos = new ByteArrayOutputStream();
            encoder = EncoderFactory.get().binaryEncoder(baos, encoder);
            avroWriter.write(testLogData, encoder);
            encoder.flush();
            byte[] data = baos.toByteArray();
            events.add(ByteBuffer.wrap(data));
            testLogs.add(testLogData);
        }

        logData.setEventRecords(events);

        SpecificDatumWriter<RecordData> logDataAvroWriter = new SpecificDatumWriter<>(RecordData.class);
        baos = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().binaryEncoder(baos, encoder);
        logDataAvroWriter.write(logData, encoder);
        encoder.flush();
        byte[] data = baos.toByteArray();

        AvroFlumeEvent eventToSend = new AvroFlumeEvent();
        eventToSend.setHeaders(new HashMap<CharSequence,CharSequence>());
        eventToSend.setBody(ByteBuffer.wrap(data));

        source.append(eventToSend);

        return testLogs;
    }

    private void readAndCheckResultsFromHdfs (RecordHeader header, List<TestLogData> testLogs) throws IOException {
        Path logsPath = new Path("/logs" + Path.SEPARATOR + applicationToken + Path.SEPARATOR + logSchemaVersion + Path.SEPARATOR + "data*");
        FileStatus[] statuses = fileSystem.globStatus(logsPath);
        List<TestLogData> resultTestLogs = new ArrayList<>();
        Schema wrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(TestLogData.getClassSchema().toString());
        for (FileStatus status : statuses) {
            FileReader<GenericRecord> fileReader = null;
            try {
                SeekableInput input = new FsInput(status.getPath(), fileSystem.getConf());
                DatumReader<GenericRecord> datumReader = new SpecificDatumReader<>(wrapperSchema);
                fileReader = DataFileReader.openReader(input, datumReader);
                for (GenericRecord record : fileReader) {
                    RecordHeader recordHeader = (RecordHeader)record.get(RecordWrapperSchemaGenerator.RECORD_HEADER_FIELD);
                    Assert.assertEquals(header, recordHeader);
                    TestLogData recordData = (TestLogData)record.get(RecordWrapperSchemaGenerator.RECORD_DATA_FIELD);
                    resultTestLogs.add(recordData);
                }
            }
            finally {
                IOUtils.closeQuietly(fileReader);
            }
        }
        Assert.assertEquals(testLogs, resultTestLogs);
    }

    private Context prepareContext() throws IOException {
        Context context = new Context();

        // Channel parameters
        context.put("capacity", "100000000");
        context.put("transactionCapacity", "10000000");
        context.put("keep-alive", "1");

        context.put("port", "31333");
        context.put("bind", "localhost");

        context.put(ConfigurationConstants.CONFIG_ROOT_HDFS_PATH, fileSystem.makeQualified(new Path("/logs")).toString());
        context.put(ConfigurationConstants.CONFIG_HDFS_TXN_EVENT_MAX, "100000");
        context.put(ConfigurationConstants.CONFIG_HDFS_THREAD_POOL_SIZE, "20");
        context.put(ConfigurationConstants.CONFIG_HDFS_ROLL_TIMER_POOL_SIZE, "1");
        context.put(ConfigurationConstants.CONFIG_HDFS_MAX_OPEN_FILES, "5000");
        context.put(ConfigurationConstants.CONFIG_HDFS_CALL_TIMEOUT, "10000");
        context.put(ConfigurationConstants.CONFIG_HDFS_ROLL_INTERVAL, "86400000"); // milliseconds
        context.put(ConfigurationConstants.CONFIG_HDFS_ROLL_SIZE, "0"); // bytes (0 means don't roll by size)
        context.put(ConfigurationConstants.CONFIG_HDFS_ROLL_COUNT, "5500000"); // records count
        context.put(ConfigurationConstants.CONFIG_HDFS_BATCH_SIZE, "" + flushRecordsCount); // flush records count
        context.put(ConfigurationConstants.CONFIG_HDFS_DEFAULT_BLOCK_SIZE, "" + blockSize); // default dfs block size in bytes
        context.put(ConfigurationConstants.CONFIG_HDFS_FILE_PREFIX, "data");
        context.put(ConfigurationConstants.CONFIG_STATISTICS_INTERVAL, "10");

        context.put("serializer.compressionCodec", "null");
        context.put("serializer.avro.schema.source", "local");
        context.put("serializer.avro.schema.local.root", logSchemasRootDir.getAbsolutePath());

        return context;
    }

    @AfterClass
    public static void deleteTempDirectory() {
        dfsCluster.shutdown();
        try {
            FileUtils.deleteDirectory(new File("build"));
            FileUtils.deleteDirectory(new File("schemas"));
        }
        catch (IOException e) {}
    }



}
