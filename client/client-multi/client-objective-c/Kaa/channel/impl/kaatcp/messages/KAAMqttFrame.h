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

#define MQTT_FIXED_HEADER_LEGTH (2)
#define KAA_TCP_NAME_LENGTH (6)

typedef enum {
    TCP_MESSAGE_TYPE_UNKNOWN = 0x00,
    TCP_MESSAGE_TYPE_CONNECT = 0x01,
    TCP_MESSAGE_TYPE_CONNACK = 0x02,
    TCP_MESSAGE_TYPE_PINGREQ = 0x0C,
    TCP_MESSAGE_TYPE_PINGRESP = 0x0D,
    TCP_MESSAGE_TYPE_DISCONNECT = 0x0E,
    TCP_MESSAGE_TYPE_KAASYNC = 0x0F
} TCPMessageType;

typedef enum {
    FRAME_PARSING_STATE_NONE,
    FRAME_PARSING_STATE_PROCESSING_LENGTH,
    FRAME_PARSING_STATE_PROCESSING_PAYLOAD
} FrameParsingState;


/**
  Basic Mqtt message.
 
  Fixed header format
 <table>
 <tr align="center"> <td>bit</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td> <td>0</td> </tr>
 <tr align="center"> <td>byte 1</td> <td colspan="4">Message Type</td> <td>Dup flag</td> <td colspan="2">QoS level</td> <td>RETAIN</td> </tr>
 <tr align="center"> <td>byte 2</td> <td colspan="8">Remaining length</td> </tr>
 </table>
 */
@interface KAAMqttFrame : NSObject

@property (nonatomic, strong) NSMutableData *buffer;
@property (nonatomic) int32_t bufferPosition;

@property (nonatomic) TCPMessageType messageType;
@property (nonatomic) BOOL frameDecodeComplete;
/**
 * Remaining length of mqtt frame
 */
@property (nonatomic) int32_t remainingLength;
@property (nonatomic) int32_t multiplier;
@property (nonatomic) FrameParsingState currentState;

- (instancetype)initWithOld:(KAAMqttFrame *)old;

- (NSData *)getFrame;

/**
 * Pack message into mqtt frame
 */
- (void)pack;

/**
 * Decode message from mqttFrame buffer
 * @exception KaaTcpProtocolException TCP exception
 */
- (void)decode;

/**
 * Check if this Mqtt frame should be last frame on connection and connection should be closed.
 * @return YES if connection should be closed after frame transmition.
 */
- (BOOL)needToCloseConnection;

/**
 * Fill mqtt frame fixed header
 * @return number of packet bytes
 */
- (int32_t)fillFixedHeader:(NSMutableData *)header remainingLength:(int32_t)remainingLength;

- (void)onFrameDone;

/**
 * Push bytes of frame
 * @param bytes Data to push
 * @param position Position in buffer
 * @return Count of used bytes from buffer
 * @exception KaaTcpProtocolException TCP exception
 */
- (int32_t)pushBytes:(NSData *)bytes toPosition:(int32_t)position;

/**
 * Used in case if Frame Class should be changed during frame decode.
 * Used for migration from KaaSync general frame to specific classes like Sync, Bootstrap.
 * Default implementation returns self.
 * @return New MqttFrame as specific class.
 * @exception KaaTcpProtocolException TCP exception
 */
- (KAAMqttFrame *)upgradeFrame;

- (NSInputStream *)remainingStream;

@end
