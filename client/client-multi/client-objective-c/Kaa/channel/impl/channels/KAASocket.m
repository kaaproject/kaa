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

#import "KAASocket.h"

@interface KAASocket ()

@property (nonatomic, strong) NSString *host;
@property (nonatomic) int port;

@end

@implementation KAASocket

+ (instancetype)socketWithHost:(NSString *)host port:(int)port {
    KAASocket *socket = [[KAASocket alloc] init];
    socket.host = host;
    socket.port = port;
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)host, port, &readStream, &writeStream);
    socket.input = (__bridge NSInputStream *)readStream;
    socket.output = (__bridge NSOutputStream *)writeStream;
    return socket;
}

- (void)open {
    [self.input open];
    [self.output open];
}

- (void)close {
    if (self.input) {
        [self.input close];
    }
    if (self.output) {
        [self.output close];
    }
}

- (BOOL)isEqual:(id)object {
    if (!object) {
        return NO;
    }
    
    if (self == object) {
        return YES;
    }
    
    if ([object isKindOfClass:[KAASocket class]]) {
        KAASocket *other = (KAASocket *)object;
        if ([self.host isEqualToString:other.host] && self.port == other.port) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"KAASocket [host:%@ port:%i]", self.host, self.port];
}

@end
