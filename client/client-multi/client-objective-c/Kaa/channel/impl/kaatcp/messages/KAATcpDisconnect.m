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

#import "KAATcpDisconnect.h"

@implementation KAATcpDisconnect

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setMessageType:TCP_MESSAGE_TYPE_DISCONNECT];
    }
    return self;
}

- (instancetype)initWithDisconnectReason:(DisconnectReason)reason {
    self = [self init];
    if (self) {
        [self setReason:reason];
        self.remainingLength = DISCONNECT_REMAINING_LEGTH_V1;
    }
    return self;
}

- (void)pack {
    char zero = 0;
    [self.buffer appendBytes:&zero length:sizeof(char)];
    self.bufferPosition++;
    
    char reason = self.reason;
    [self.buffer appendBytes:&reason length:sizeof(char)];
    self.bufferPosition++;
}

- (void)decode {
    self.reason = ((const char*)[self.buffer bytes])[1];
}

- (BOOL)needToCloseConnection {
    return YES;
}

@end
