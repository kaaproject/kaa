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

    public static final String CONFIG_ROOT_HDFS_PATH = "rootHdfsPath";
    public static final String DEFAULT_ROOT_HDFS_PATH = "hdfs://localhost:8020/logs";
    
    public static final String CONFIG_STATISTICS_INTERVAL = "statisticsInterval";
    public static final long DEFAULT_STATISTICS_INTERVAL = 60; //seconds
    
    public static final String CONFIG_HDFS_TXN_EVENT_MAX = "hdfs.txnEventMax";
    public static final long DEFAULT_HDFS_TXN_EVENT_MAX = 100;
    
    public static final String CONFIG_HDFS_THREAD_POOL_SIZE = "hdfs.threadsPoolSize";
    public static final int DEFAULT_HDFS_THREAD_POOL_SIZE = 10;

    public static final String CONFIG_HDFS_WRITER_EXPIRATION_INTERVAL = "hdfs.writerExpirationInterval";
    public static final int DEFAULT_HDFS_WRITER_EXPIRATION_INTERVAL = 60 * 60;

    public static final String CONFIG_HDFS_CALL_TIMEOUT = "hdfs.callTimeout";
    public static final long DEFAULT_HDFS_CALL_TIMEOUT = 10000;

    public static final String CONFIG_HDFS_DEFAULT_BLOCK_SIZE = "hdfs.default.blockSize";
    public static final long DEFAULT_HDFS_DEFAULT_BLOCK_SIZE = DFSConfigKeys.DFS_BLOCK_SIZE_DEFAULT;
    
    public static final String CONFIG_HDFS_ROLL_TIMER_POOL_SIZE = "hdfs.rollTimerPoolSize";
    public static final int DEFAULT_HDFS_ROLL_TIMER_POOL_SIZE = 1;
    
    public static final String CONFIG_HDFS_MAX_OPEN_FILES = "hdfs.maxOpenFiles";
    public static final int DEFAULT_HDFS_MAX_OPEN_FILES = 5000;
    
    public static final String CONFIG_HDFS_CACHE_CLEANUP_INTERVAL = "hdfs.cacheCleanupInterval";
    public static final int DEFAULT_HDFS_CACHE_CLEANUP_INTERVAL = 10 * 60;

    public static final String CONFIG_HDFS_ROLL_INTERVAL = "hdfs.rollInterval";
    public static final long DEFAULT_HDFS_ROLL_INTERVAL = 30;
  
    public static final String CONFIG_HDFS_ROLL_SIZE = "hdfs.rollSize";
    public static final long DEFAULT_HDFS_ROLL_SIZE = 1024;
    
    public static final String CONFIG_HDFS_ROLL_COUNT = "hdfs.rollCount";
    public static final long DEFAULT_HDFS_ROLL_COUNT = 10;

    public static final String CONFIG_HDFS_BATCH_SIZE = "hdfs.batchSize";
    public static final long DEFAULT_HDFS_BATCH_SIZE = 1;
    
    public static final String CONFIG_HDFS_FILE_PREFIX = "hdfs.filePrefix";
    public static final String DEFAULT_HDFS_FILE_PREFIX = "data";
    
    public static final String CONFIG_HDFS_KERBEROS_PRINCIPAL = "hdfs.kerberosPrincipal";
    
    public static final String CONFIG_HDFS_KERBEROS_KEYTAB = "hdfs.kerberosKeytab";
    
    public static final String CONFIG_HDFS_PROXY_USER = "hdfs.proxyUser";
    
    public static final String CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE = "avro.schema.source";
    
    public static final String SCHEMA_SOURCE_REST = "rest";
    public static final String SCHEMA_SOURCE_LOCAL = "local";
    
    public static final String DEFAULT_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE = SCHEMA_SOURCE_REST;
    
    public static final String CONFIG_KAA_REST_HOST = "kaa.rest.host";
    public static final String DEFAULT_KAA_REST_HOST = "localhost";
    
    public static final String CONFIG_KAA_REST_PORT = "kaa.rest.port";
    public static final int DEFAULT_KAA_REST_PORT = 8080;
    
    public static final String CONFIG_KAA_REST_USER = "kaa.rest.user";
    public static final String CONFIG_KAA_REST_PASSWORD = "kaa.rest.password";
    
    public static final String CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT = "avro.schema.local.root";
    
}
