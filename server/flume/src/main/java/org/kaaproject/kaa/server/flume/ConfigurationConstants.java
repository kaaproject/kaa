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

import org.apache.hadoop.hdfs.DFSConfigKeys;

public interface ConfigurationConstants {

  String CONFIG_ROOT_HDFS_PATH = "rootHdfsPath";
  String DEFAULT_ROOT_HDFS_PATH = "hdfs://localhost:8020/logs";

  String CONFIG_STATISTICS_INTERVAL = "statisticsInterval";
  long DEFAULT_STATISTICS_INTERVAL = 60; //seconds

  String CONFIG_HDFS_TXN_EVENT_MAX = "hdfs.txnEventMax";
  long DEFAULT_HDFS_TXN_EVENT_MAX = 100;

  String CONFIG_HDFS_THREAD_POOL_SIZE = "hdfs.threadsPoolSize";
  int DEFAULT_HDFS_THREAD_POOL_SIZE = 10;

  String CONFIG_HDFS_WRITER_EXPIRATION_INTERVAL = "hdfs.writerExpirationInterval";
  int DEFAULT_HDFS_WRITER_EXPIRATION_INTERVAL = 60 * 60;

  String CONFIG_HDFS_CALL_TIMEOUT = "hdfs.callTimeout";
  long DEFAULT_HDFS_CALL_TIMEOUT = 10000;

  String CONFIG_HDFS_DEFAULT_BLOCK_SIZE = "hdfs.default.blockSize";
  long DEFAULT_HDFS_DEFAULT_BLOCK_SIZE = DFSConfigKeys.DFS_BLOCK_SIZE_DEFAULT;

  String CONFIG_HDFS_ROLL_TIMER_POOL_SIZE = "hdfs.rollTimerPoolSize";
  int DEFAULT_HDFS_ROLL_TIMER_POOL_SIZE = 1;

  String CONFIG_HDFS_MAX_OPEN_FILES = "hdfs.maxOpenFiles";
  int DEFAULT_HDFS_MAX_OPEN_FILES = 5000;

  String CONFIG_HDFS_CACHE_CLEANUP_INTERVAL = "hdfs.cacheCleanupInterval";
  int DEFAULT_HDFS_CACHE_CLEANUP_INTERVAL = 10 * 60;

  String CONFIG_HDFS_ROLL_INTERVAL = "hdfs.rollInterval";
  long DEFAULT_HDFS_ROLL_INTERVAL = 30;

  String CONFIG_HDFS_ROLL_SIZE = "hdfs.rollSize";
  long DEFAULT_HDFS_ROLL_SIZE = 1024;

  String CONFIG_HDFS_ROLL_COUNT = "hdfs.rollCount";
  long DEFAULT_HDFS_ROLL_COUNT = 10;

  String CONFIG_HDFS_BATCH_SIZE = "hdfs.batchSize";
  long DEFAULT_HDFS_BATCH_SIZE = 1;

  String CONFIG_HDFS_FILE_PREFIX = "hdfs.filePrefix";
  String DEFAULT_HDFS_FILE_PREFIX = "data";

  String CONFIG_HDFS_KERBEROS_PRINCIPAL = "hdfs.kerberosPrincipal";

  String CONFIG_HDFS_KERBEROS_KEYTAB = "hdfs.kerberosKeytab";

  String CONFIG_HDFS_PROXY_USER = "hdfs.proxyUser";

  String CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE = "avro.schema.source";

  String SCHEMA_SOURCE_REST = "rest";
  String SCHEMA_SOURCE_LOCAL = "local";

  String DEFAULT_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE = SCHEMA_SOURCE_REST;

  String CONFIG_KAA_REST_HOST = "kaa.rest.host";
  String DEFAULT_KAA_REST_HOST = "localhost";

  String CONFIG_KAA_REST_PORT = "kaa.rest.port";
  int DEFAULT_KAA_REST_PORT = 8080;

  String CONFIG_KAA_REST_USER = "kaa.rest.user";
  String CONFIG_KAA_REST_PASSWORD = "kaa.rest.password";

  String CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT = "avro.schema.local.root";

}
