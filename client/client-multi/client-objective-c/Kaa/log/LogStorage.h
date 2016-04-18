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

#ifndef Kaa_LogStorage_h
#define Kaa_LogStorage_h

#import <Foundation/Foundation.h>
#import "LogBucket.h"
#import "LogRecord.h"
#import "BucketInfo.h"

/**
 * Interface for a log storage status.
 *
 * Retrieves information about current status of the log storage. Used by
 * a log upload strategy on each adding of new log record in order to check
 * whether to send logs to the server or clean up local storage.
 *
 * Reference implementation is present and use by default < MemoryLogStorage >
 */
@protocol LogStorageStatus

/**
 * Retrieves current log storage size used by added records.
 * @return Amount of bytes consumed by added records.
 */
- (int64_t)getConsumedVolume;

/**
 * Retrieves current number of added records.
 * @return Number of records in a local storage.
 */
- (int64_t)getRecordCount;

@end

/**
 * Interface for log storage.
 *
 * Persists each new log record, forms on demand new log bucket for sending
 * it to the Operation server, removes already sent records, cleans up older
 * records in case if there is some limitation on a size of log storage.
 *
 * Reference implementation used by default < MemLogStorage >
 */
@protocol LogStorage

/**
 * Persists a log record.
 */
- (BucketInfo *)addLogRecord:(LogRecord *)record;

/**
 * Returns log storage status.
 */
- (id<LogStorageStatus>)getStatus;

/**
 * Returns a log bucket or nil if there are no logs.
 */
- (LogBucket *)getNextBucket;

/**
 * Removes already sent log records by its bucket id.
 *
 * Use in case of a successful upload.
 *
 * @param bucketId Unique id of sent bucket
 */
- (void)removeBucketWithId:(int32_t)bucketId;

/**
 * Notifies if sending of a log bucket with a specified id was failed.
 *
 * @param bucketId Unique id of log bucket.
 */
- (void)rollbackBucketWithId:(int32_t)bucketId;

/**
 * Closes log storage and releases all used resources (if any)
 */
- (void)close;

@end

#endif
