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

#import "AvroBytesConverter.h"
#import "AvroUtils.h"
#import "KaaLogging.h"

#define TAG @"AvroBytesConverter >>>"

@implementation AvroBytesConverter

- (NSData *)toBytes:(id<Avro>)object {
    size_t objSize = [object getSize];
    char *buffer = (char *)malloc((objSize) * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, objSize);
    if (!writer) {
        free(buffer);
        DDLogError(@"%@ Unable to allocate '%li'bytes for avro writer", TAG, objSize);
        return nil;
    }
    [object serialize:writer];
    NSData *bytes = [NSData dataWithBytes:writer->buf length:(NSUInteger)writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }
    return bytes;
}

- (id)fromBytes:(NSData *)bytes object:(id<Avro>)object {
    avro_reader_t reader = avro_reader_memory([bytes bytes], [bytes length]);
    [object deserialize:reader];
    avro_reader_free(reader);
    return object;
}

@end
