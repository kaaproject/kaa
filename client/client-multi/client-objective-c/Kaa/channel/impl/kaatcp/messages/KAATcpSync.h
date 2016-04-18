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

#import "KAATcpKaaSync.h"

/**
 * Sync message class.<br>
 * The SYNC message is used as intermediate class for decoding messages
 * SyncRequest,SyncResponse
 *
 * Sync message extend KaaSync with  Payload Avro object.
 *
 *  Payload Avro object depend on Flags object can be zipped and than encrypted with AES SessionKey
 *  exchanged with the CONNECT message.
 */
@interface KAATcpSync : KAATcpKaaSync

@property (nonatomic, strong) NSData *avroObject; //Avro object byte representation

- (instancetype)initWithAvro:(NSData *)avroObject request:(BOOL)isRequest zipped:(BOOL)isZipped encypted:(BOOL)isEncrypted;

- (void)decodeAvroObjectFromInput:(NSInputStream *)input;

@end
