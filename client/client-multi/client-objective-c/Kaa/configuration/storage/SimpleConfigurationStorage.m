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

#import "SimpleConfigurationStorage.h"
#import "NSData+Conversion.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"SimpleConfigurationStorage >>>"
#define _8KB (1024 * 8)

@interface SimpleConfigurationStorage ()

@property (nonatomic, strong) NSString *path;
@property (nonatomic, readonly) NSFileManager *fileManager;

@end

@implementation SimpleConfigurationStorage

+ (instancetype)storageWithPath:(NSString *)path {
    SimpleConfigurationStorage *storage = [[self alloc] init];
    storage.path = path;
    return storage;
}

- (NSFileManager *)fileManager {
    return [NSFileManager defaultManager];
}

- (void)saveConfiguration:(NSData *)buffer {
    DDLogVerbose(@"%@ Writing bytes to file: %@", TAG, [buffer hexadecimalString]);
    [self.fileManager createFileAtPath:self.path contents:buffer attributes:nil];
}

- (void)clearConfiguration {
    NSError *error;
    if ([self.fileManager isDeletableFileAtPath:self.path]) {
        BOOL success = [self.fileManager removeItemAtPath:self.path error:&error];
        if (!success) {
            DDLogInfo(@"%@ Deleting failed with error: %@", TAG, error);
        }
    } else {
        DDLogInfo(@"%@ File doesn't exist at path or doesn't have delete privileges", TAG);
    }
    
}

- (NSData *)loadConfiguration {
    if (![self.fileManager fileExistsAtPath:self.path]) {
        DDLogInfo(@"%@ There is no configuration in storage yet", TAG);
        return nil;
    }
    NSFileHandle *configFile = nil;
    NSMutableArray *chunks = [NSMutableArray array];
    int bytesRead = 0;
    @try {
        unsigned long long fileSize = [[self.fileManager attributesOfItemAtPath:self.path error:NULL] fileSize];
        configFile = [NSFileHandle fileHandleForReadingAtPath:self.path];
        if (!configFile) {
            [NSException raise:KaaUnableOpenFile format:@"Failed to open configuration file"];
        }
        NSData *buffer = nil;
        do {
            [configFile seekToFileOffset:bytesRead];
            buffer = [configFile readDataOfLength:_8KB];
            if ([buffer length] > 0) {
                bytesRead += [buffer length];
                [chunks addObject:buffer];
            }
            
        } while ([buffer length] == _8KB && bytesRead < fileSize);
        
    }
    @catch (NSException *exception) {
        DDLogError(@"%@ Error loading configuration: %@. Reason: %@", TAG, exception.name, exception.reason);
    }
    @finally {
        if (configFile) {
            [configFile closeFile];
        }
    }
    NSMutableData *configuration = nil;
    if (bytesRead > 0) {
        configuration = [NSMutableData data];
        for (NSData *chunk in chunks) {
            [configuration appendData:chunk];
        }
    }
    return configuration;
}

@end
