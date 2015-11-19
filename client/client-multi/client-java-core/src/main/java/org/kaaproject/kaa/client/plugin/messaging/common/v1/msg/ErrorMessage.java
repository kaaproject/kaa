/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.client.plugin.messaging.common.v1.msg;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class ErrorMessage extends AbstractPayloadMessage {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final ErrorCode errorCode;
    private final String msg;
    private final byte[] payload;

    public ErrorMessage(short methodId, ErrorCode errorCode, String message) {
        this(UUID.randomUUID(), methodId, errorCode, message);
    }

    public ErrorMessage(UUID uid, short methodId, ErrorCode errorCode) {
        this(uid, methodId, errorCode, null);
    }

    public ErrorMessage(UUID uid, short methodId, ErrorCode errorCode, String message) {
        super(uid, methodId);
        this.errorCode = errorCode;
        this.msg = message;
        if (message != null) {
            byte[] msgBytes = message.getBytes(UTF8);
            payload = new byte[4 + msgBytes.length];
            ByteBuffer bb = ByteBuffer.wrap(payload);
            bb.putInt(errorCode.getCode());
            bb.put(msgBytes);
        } else {
            payload = new byte[4];
            ByteBuffer.wrap(payload).putInt(errorCode.getCode());
        }
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public MessageType getType() {
        return MessageType.ERROR;
    }

    public ErrorMessageException getException() {
        return new ErrorMessageException(this.errorCode.getCode(), this.msg);
    }

}
