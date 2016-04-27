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

#import <Foundation/Foundation.h>
#import "io.h"
#import "encoding.h"

/**
 * The main protocol, each avro-compatible object should conform.
 */
@protocol Avro

/**
 * Serializes object fields to avro writer structure.
 */
- (void)serialize:(avro_writer_t)writer;

/**
 * Deserializes object fields from avro reader structure.
 */
- (void)deserialize:(avro_reader_t)reader;

/**
 * Returns size of avro object.
 */
- (size_t)getSize;

/**
 * Returns Fully Qualified Name of avro object.
 * (e.g. com.example.utils)
 */
+ (NSString *)FQN;

@end

/**
 * Utils responsible for serialization/deserialization of all typical Avro types.
 * NOTE: Maps currently not supported.
 */
@interface AvroUtils : NSObject

- (size_t)getStringSize:(NSString *)data;
- (NSString *)deserializeString:(avro_reader_t)reader;
- (void)serializeString:(NSString *)data to:(avro_writer_t)writer;

- (size_t)getBytesSize:(NSData *)data;
- (NSData *)deserializeBytes:(avro_reader_t)reader;
- (void)serializeBytes:(NSData *)data to:(avro_writer_t)writer;

- (size_t)getFixedSize:(NSData *)data;
- (NSData *)deserializeFixed:(avro_reader_t)reader size:(NSNumber *)size;
- (void)serializeFixed:(NSData *)data to:(avro_writer_t)writer;

- (size_t)getBooleanSize:(NSNumber *)data;
- (NSNumber *)deserializeBoolean:(avro_reader_t)reader;
- (void)serializeBoolean:(NSNumber *)data to:(avro_writer_t)writer;

- (size_t)getIntSize:(NSNumber *)data;
- (NSNumber *)deserializeInt:(avro_reader_t)reader;
- (void)serializeInt:(NSNumber *)data to:(avro_writer_t)writer;

- (size_t)getLongSize:(NSNumber *)data;
- (NSNumber *)deserializeLong:(avro_reader_t)reader;
- (void)serializeLong:(NSNumber *)data to:(avro_writer_t)writer;

- (size_t)getFloatSize;
- (NSNumber *)deserializeFloat:(avro_reader_t)reader;
- (void)serializeFloat:(NSNumber *)data to:(avro_writer_t)writer;

- (size_t)getDoubleSize;
- (NSNumber *)deserializeDouble:(avro_reader_t)reader;
- (void)serializeDouble:(NSNumber *)data to:(avro_writer_t)writer;

- (size_t)getEnumSize:(NSNumber *)data;
- (NSNumber *)deserializeEnum:(avro_reader_t)reader;
- (void)serializeEnum:(NSNumber *)data to:(avro_writer_t)writer;

- (id<Avro>)deserializeRecord:(avro_reader_t)reader as:(Class)cls;
- (void)serializeRecord:(id<Avro>)data to:(avro_writer_t)writer;

- (size_t)getArraySize:(NSArray *)array withSelector:(SEL)sizeFunc parameterized:(BOOL)parameterized target:(id)target;
- (NSArray *)deserializeArray:(avro_reader_t)reader
                withSelector:(SEL)deserializeFunc
                    andParam:(id)param
                      target:(id)target;
- (void)serializeArray:(NSArray *)array
                    to:(avro_writer_t)writer
          withSelector:(SEL)serializeFunc
                target:(id)target;

@end
