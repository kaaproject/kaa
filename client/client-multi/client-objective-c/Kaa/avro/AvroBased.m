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

#import "AvroBased.h"

@implementation AvroBased

- (instancetype)init {
    self = [super init];
    if (self) {
        _utils = [[AvroUtils alloc] init];
    }
    return self;
}

- (void)serialize:(avro_writer_t)writer {
#pragma unused (writer)
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
}

- (void)deserialize:(avro_reader_t)reader {
#pragma unused (reader)
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
}

- (size_t)getSize {
    return 0;
}

+ (NSString *)FQN {
    return nil;
}

@end
