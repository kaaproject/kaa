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

@import UIKit;
#import "AvroUtils.h"
#import "EndpointGen.h"
#import <XCTest/XCTest.h>

@interface AvroUtilsTests : XCTestCase

@property (nonatomic, strong) AvroUtils *utils;

@end

@implementation AvroUtilsTests

- (void)setUp {
    [super setUp];
    self.utils = [[AvroUtils alloc] init];
}

- (void)testPrimitives {
    
    typedef enum {
        ANACONDA,
        ASP,
        ADDER,
        ADER
    } Snakes;
    
    NSNumber *intOrigin = @(23);
    NSNumber *longOrigin = @(234);
    NSNumber *floatOrigin = @((float)394.3);
    NSNumber *doubleOrigin = @(35235.54);
    NSNumber *boolTrue = @(YES);
    NSNumber *boolFalse = @(NO);
    NSNumber *enumOrigin = @(ADDER);
    
    size_t bufSize = [self.utils getIntSize:intOrigin]
        + [self.utils getLongSize:longOrigin]
        + [self.utils getFloatSize]
        + [self.utils getDoubleSize]
        + [self.utils getBooleanSize:boolTrue]
        + [self.utils getBooleanSize:boolFalse]
        + [self.utils getEnumSize:enumOrigin];
    char *buffer = (char *)malloc(bufSize * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, bufSize);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeInt:intOrigin to:writer];
    [self.utils serializeLong:longOrigin to:writer];
    [self.utils serializeFloat:floatOrigin to:writer];
    [self.utils serializeDouble:doubleOrigin to:writer];
    [self.utils serializeBoolean:boolTrue to:writer];
    [self.utils serializeBoolean:boolFalse to:writer];
    [self.utils serializeEnum:enumOrigin to:writer];
    
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->len];
    avro_writer_free(writer);
    
    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSNumber *intDes = [self.utils deserializeInt:reader];
    NSNumber *longDes = [self.utils deserializeLong:reader];
    NSNumber *floatDes = [self.utils deserializeFloat:reader];
    NSNumber *doubleDes = [self.utils deserializeDouble:reader];
    NSNumber *boolTrueDes = [self.utils deserializeBoolean:reader];
    NSNumber *boolFalseDes = [self.utils deserializeBoolean:reader];
    NSNumber *enumDes = [self.utils deserializeEnum:reader];
    avro_reader_free(reader);
    
    XCTAssertTrue([intOrigin isEqualToNumber:intDes]);
    XCTAssertTrue([longOrigin isEqualToNumber:longDes]);
    XCTAssertTrue([floatOrigin isEqualToNumber:floatDes]);
    XCTAssertTrue([doubleOrigin isEqualToNumber:doubleDes]);
    XCTAssertTrue([boolTrue isEqualToNumber:boolTrueDes]);
    XCTAssertTrue([boolFalse isEqualToNumber:boolFalseDes]);
    XCTAssertTrue([enumOrigin isEqualToNumber:enumDes]);
}

- (void)testBytes {
    int randomInt = arc4random();
    NSData *data = [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
    char *buffer = (char *)malloc(([self.utils getBytesSize:data]) * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, [self.utils getBytesSize:data]);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeBytes:data to:writer];
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }
    
    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSData *deserialized = [self.utils deserializeBytes:reader];
    avro_reader_free(reader);
    XCTAssertTrue([data isEqualToData:deserialized]);
}

- (void)testFixed {
    int randomInt = arc4random();
    NSData *data = [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
    char *buffer = (char *)malloc(([self.utils getFixedSize:data]) * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, [self.utils getFixedSize:data]);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeFixed:data to:writer];
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }
    
    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSData *deserialized = [self.utils deserializeFixed:reader size:@((long)[data length])];
    avro_reader_free(reader);
    XCTAssertTrue([data isEqualToData:deserialized]);
}

- (void)testString {
    
    NSString *origin = @"Avro Utils Tests";
    char *buffer = (char *)malloc(([self.utils getStringSize:origin]) * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, [self.utils getStringSize:origin]);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeString:origin to:writer];
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }

    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSString *deserialized = [self.utils deserializeString:reader];
    avro_reader_free(reader);
    XCTAssertTrue([origin isEqualToString:deserialized]);
}


- (void)testArrayWithStrings {
    
    NSArray *array = [NSArray arrayWithObjects:@"Object1", @"Object2", @"Object3", nil];
    size_t size = [self.utils getArraySize:array withSelector:@selector(getStringSize:) parameterized:YES target:self.utils];
    char *buffer = (char *)malloc(size * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, size);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeArray:array to:writer withSelector:@selector(serializeString:to:) target:self.utils];
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }
    
    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSArray *desirealized = [self.utils deserializeArray:reader withSelector:@selector(deserializeString:) andParam:nil target:self.utils];
    avro_reader_free(reader);
    XCTAssertEqual([array count], [desirealized count]);
    for (int i = 0; i < [array count]; i++) {
        [array[i] isEqualToString:desirealized[i]];
    }
}


- (void)testArrayOfRecords {
    
    SubscriptionCommand *command1 = [[SubscriptionCommand alloc] init];
    command1.topicId = 1;
    command1.command = SUBSCRIPTION_COMMAND_TYPE_REMOVE;
    SubscriptionCommand *command2 = [[SubscriptionCommand alloc] init];
    command2.topicId = 2;
    command2.command = SUBSCRIPTION_COMMAND_TYPE_REMOVE;
    SubscriptionCommand *command3 = [[SubscriptionCommand alloc] init];
    command3.topicId = 3;
    command3.command = SUBSCRIPTION_COMMAND_TYPE_REMOVE;
    
    NSArray *array = [NSArray arrayWithObjects:command1, command2, command3, nil];
    
    size_t size = [self.utils getArraySize:array withSelector:@selector(getSize) parameterized:NO target:nil];
    char *buffer = (char *)malloc(size * sizeof(char));
    avro_writer_t writer = avro_writer_memory(buffer, size);
    if (!writer) {
        XCTFail(@"Can't allocate memory!");
    }
    [self.utils serializeArray:array to:writer withSelector:@selector(serializeRecord:to:) target:nil];
    NSData *serialized = [NSData dataWithBytes:writer->buf length:writer->written];
    avro_writer_free(writer);
    if (buffer) {
        free(buffer);
    }
    
    avro_reader_t reader = avro_reader_memory([serialized bytes], [serialized length]);
    NSArray *desirealized = [self.utils deserializeArray:reader withSelector:@selector(deserializeRecord:as:) andParam:[SubscriptionCommand class] target:nil];
    avro_reader_free(reader);
    XCTAssertEqual([array count], [desirealized count]);
}

@end
