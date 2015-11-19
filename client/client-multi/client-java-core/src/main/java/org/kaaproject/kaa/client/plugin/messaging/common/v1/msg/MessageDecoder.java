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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDecoder {

    public static List<Message> decode(ByteBuffer bb) {
        int msgCount = bb.getInt();
        List<Message> msgs = new ArrayList<Message>(msgCount);
        for (int i = 0; i < msgCount; i++) {
            UUID uid = new UUID(bb.getLong(), bb.getLong());
            short methodId = bb.getShort();
            short typeCode = bb.getShort();
            MessageType type = MessageType.get(typeCode);
            switch (type) {
            case ENTITY:
                int entityDataLength = bb.getInt();
                byte[] entityData = new byte[entityDataLength];
                bb.get(entityData);
                msgs.add(new EntityMessage(uid, entityData, methodId));
                break;
            case ERROR:
                int errorCode = bb.getInt();
                int msgDataLength = bb.getInt();
                String msg;
                if (msgDataLength > 0) {
                    byte[] msgData = new byte[msgDataLength];
                    bb.get(msgData);
                    msg = new String(msgData, ErrorMessage.UTF8);
                } else {
                    msg = null;
                }
                msgs.add(new ErrorMessage(uid, methodId, ErrorCode.get(errorCode), msg));
                break;
            case ACK:
                msgs.add(new AckMessage(uid, methodId));
                break;
            case VOID:
                msgs.add(new VoidMessage(uid, methodId));
                break;
            }
        }
        return msgs;
    }

}
