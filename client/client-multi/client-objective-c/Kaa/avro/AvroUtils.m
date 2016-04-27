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

#import "AvroUtils.h"

#define AVRO_FLOAT_SIZE     4
#define AVRO_DOUBLE_SIZE    8

@interface AvroUtils ()

- (size_t)getPlainLongSize:(int64_t)data;

@end

@implementation AvroUtils

- (size_t)getPlainLongSize:(int64_t)data {
    int64_t len = 0;
    uint64_t n = (data << 1) ^ (data >> 63);
    while (n & ~0x7F) {
        len++;
        n >>= 7;
    }
    len++;
    return (size_t)len;
}

- (size_t)getStringSize:(NSString *)data {
    const char *raw = [data cStringUsingEncoding:NSUTF8StringEncoding];
    size_t size = strlen(raw);
    return [self getPlainLongSize:size] + size;
}

- (NSString *)deserializeString:(avro_reader_t)reader {
    char *string;
    avro_binary_encoding.read_string(reader, &string, NULL);
    return [NSString stringWithCString:string encoding:NSUTF8StringEncoding];
}

- (void)serializeString:(NSString *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_string(writer, [data cStringUsingEncoding:NSUTF8StringEncoding]);
}

- (size_t)getBytesSize:(NSData *)data {
    return [data length] + [self getPlainLongSize:[data length]];
}

- (NSData *)deserializeBytes:(avro_reader_t)reader {
    char *buffer;
    int64_t size;
    avro_binary_encoding.read_bytes(reader, &buffer, &size);
    return [NSData dataWithBytes:buffer length:(NSUInteger)size];
}

- (void)serializeBytes:(NSData *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_bytes(writer, [data bytes], [data length]);
}

- (size_t)getFixedSize:(NSData *)data {
    return [data length];
}

- (NSData *)deserializeFixed:(avro_reader_t)reader size:(NSNumber *)size {
    long plainSize = [size longValue];
    uint8_t *buffer = (uint8_t *)malloc(plainSize * sizeof(uint8_t));
    avro_read(reader, buffer, plainSize);
    return [NSData dataWithBytes:buffer length:plainSize];
}

- (void)serializeFixed:(NSData *)data to:(avro_writer_t)writer {
    if (data)
        avro_write(writer, (char *)[data bytes], [data length]);
}

- (size_t)getBooleanSize:(NSNumber *)data {
    return [self getPlainLongSize:(int8_t)[data boolValue]];
}

- (NSNumber *)deserializeBoolean:(avro_reader_t)reader {
    int8_t *data = (int8_t *)malloc(sizeof(int8_t));
    avro_binary_encoding.read_boolean(reader, data);
    return @(data[0] > 0 ? YES : NO);
}

- (void)serializeBoolean:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_boolean(writer, [data boolValue]);
}

- (size_t)getIntSize:(NSNumber *)data {
    return [self getPlainLongSize:[data intValue]];
}

- (NSNumber *)deserializeInt:(avro_reader_t)reader {
    int32_t data;
    avro_binary_encoding.read_int(reader, &data);
    return @(data);
}

- (void)serializeInt:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_int(writer, [data intValue]);
}

- (size_t)getLongSize:(NSNumber *)data {
    return [self getPlainLongSize:[data longLongValue]];
}

- (NSNumber *)deserializeLong:(avro_reader_t)reader {
    int64_t data;
    avro_binary_encoding.read_long(reader, &data);
    return @(data);
}

- (void)serializeLong:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_long(writer, [data longLongValue]);
}

- (size_t)getFloatSize {
    return AVRO_FLOAT_SIZE;
}

- (NSNumber *)deserializeFloat:(avro_reader_t)reader {
    float data;
    avro_binary_encoding.read_float(reader, &data);
    return @(data);
}

- (void)serializeFloat:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_float(writer, [data floatValue]);
}

- (size_t)getDoubleSize {
    return AVRO_DOUBLE_SIZE;
}

- (NSNumber *)deserializeDouble:(avro_reader_t)reader {
    double data;
    avro_binary_encoding.read_double(reader, &data);
    return @(data);
}

- (void)serializeDouble:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_double(writer, [data doubleValue]);
}

- (size_t)getEnumSize:(NSNumber *)data {
    return [self getPlainLongSize:[data intValue]];
}

- (NSNumber *)deserializeEnum:(avro_reader_t)reader {
    int64_t data;
    avro_binary_encoding.read_long(reader, &data);
    return @((int32_t)data);
}

- (void)serializeEnum:(NSNumber *)data to:(avro_writer_t)writer {
    if (data)
        avro_binary_encoding.write_long(writer, [data intValue]);
}

- (id<Avro>)deserializeRecord:(avro_reader_t)reader as:(Class)cls {
    id<Avro> object = [[cls alloc] init];
    [object deserialize:reader];
    return object;
}

- (void)serializeRecord:(id<Avro>)data to:(avro_writer_t)writer {
    if (data)
        [data serialize:writer];
}

- (size_t)getArraySize:(NSArray *)array withSelector:(SEL)sizeFunc parameterized:(BOOL)parameterized target:(id)target {
    size_t size = 0;
    if ([array count] > 0) {
        Class targetClass = [(target ? target : [array firstObject]) class];
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:
                                    [targetClass instanceMethodSignatureForSelector:sizeFunc]];
        [invocation setSelector:sizeFunc];
        for (int i = 0; i < array.count; i++) {
            __unsafe_unretained id object = array[i];
            if (parameterized) {
                [invocation setArgument:&object atIndex:2];
            }
            [invocation invokeWithTarget:(target ? target : object)];
            size_t objSize;
            [invocation getReturnValue:&objSize];
            size += objSize;
        }
        size += [self getPlainLongSize:[array count]];
    }
    size += [self getPlainLongSize:0];
    return size;
}

- (NSArray *)deserializeArray:(avro_reader_t)reader
                 withSelector:(SEL)deserializeFunc
                     andParam:(id)param
                       target:(id)target {
    int64_t size;
    avro_binary_encoding.read_long(reader, &size);
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:(NSUInteger)size];
    if (size != 0 && [(target ? target : self) respondsToSelector:deserializeFunc]) {
        if (size < 0) {
            int64_t temp;
            size *= (-1);
            avro_binary_encoding.read_long(reader, &temp);
        }
        __unsafe_unretained id parameter = param;
        NSMethodSignature *signature = [(target ? target : self) methodSignatureForSelector:deserializeFunc];
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
        NSUInteger argsCount = [signature numberOfArguments];
        [invocation setSelector:deserializeFunc];
        [invocation setTarget:(target ? target : self)];
        [invocation setArgument:&reader atIndex:2];
        if (argsCount > 3 && parameter) {
            [invocation setArgument:&parameter atIndex:3];
        }
        __unsafe_unretained id object;
        int index;
        for (index = 0; index < size; index++) {
            [invocation invoke];
            [invocation getReturnValue:&object];
            [array addObject:object];
        }
        
        avro_binary_encoding.read_long(reader, &size);
    }
    return array;
}

- (void)serializeArray:(NSArray *)array
                    to:(avro_writer_t)writer
          withSelector:(SEL)serializeFunc
                target:(id)target {
    if (array) {
        long size = [array count];
        if (size > 0 && [(target ? target : self) respondsToSelector:serializeFunc]) {
            avro_binary_encoding.write_long(writer, size);
            for (int i = 0; i < size; i++) {
                __unsafe_unretained id object = array[i];
                NSMethodSignature *signature = [(target ? target : self) methodSignatureForSelector:serializeFunc];
                NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
                [invocation setSelector:serializeFunc];
                [invocation setArgument:&object atIndex:2];
                [invocation setArgument:&writer atIndex:3];
                [invocation invokeWithTarget:(target ? target : self)];
            }
        }
    }
    avro_binary_encoding.write_long(writer, 0);
}

@end