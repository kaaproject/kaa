/**
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

package org.kaaproject.kaa.server.common.paf.shared.common.data;

import java.util.Map;

import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

public class PafMessage<T> extends GenericMessage<T> {

    private static final long serialVersionUID = 8695930130716019189L;
    
    public static final String SESSION_ID = "sessionId";
    public static final String SESSION_TYPE = "sessionType";
    public static final String SYSTEM_LEVEL_REPLY_CHANNEL = "systemLevelReplyChannel";
    
    private final PafMetadata metaData;
    
    public PafMessage(T payload) {
        this(payload, new MessageHeaders(null));
    }
    
    public PafMessage(T payload, Map<String, Object> headers) {
        this(payload, new MessageHeaders(headers));
    }

    public PafMessage(T payload, MessageHeaders headers) {
        this(payload, headers, new PafMetadata());
    }
    
    public PafMessage(T payload, MessageHeaders headers, PafMetadata metadata) {
        super(payload, headers);
        this.metaData = metadata;
    }
    
    public PafMetadata getMetaData() {
        return metaData;
    }

    public SessionId getSessionId() {
        return metaData.get(SESSION_ID, SessionId.class);
    }
    
    public void setSessionId(SessionId sessionId) {
        metaData.put(SESSION_ID, sessionId);
    }
    
    public SessionType getSessionType() {
        return metaData.get(SESSION_TYPE, SessionType.class);
    }
    
    public void setSessionType(SessionType sessionType) {
        metaData.put(SESSION_TYPE, sessionType);
    }
    
    public void setSystemLevelReplyChannel(MessageChannel messageChannel) {
        metaData.put(SYSTEM_LEVEL_REPLY_CHANNEL, messageChannel);
    }

    public MessageChannel getSystemLevelReplyChannel() {
        return metaData.get(SYSTEM_LEVEL_REPLY_CHANNEL, MessageChannel.class);
    }
}
