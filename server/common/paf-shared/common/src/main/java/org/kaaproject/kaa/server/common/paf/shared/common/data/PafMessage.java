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

import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafMessagingException;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.RegistrationResult;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

public class PafMessage extends GenericMessage<byte[]> {

    private static final long serialVersionUID = 8695930130716019189L;
    
    public static final String SESSION_ID = "sessionId";
    public static final String SESSION_TYPE = "sessionType";
    public static final String APPLICATION_ROUTE = "applicationRoute";
    public static final String APPLICATION_PROFILE_ROUTE = "applicationProfileRoute";
    public static final String ENDPOINT_ID = "endpointId";
    
    public static final String REGISTRATION_RESULT = "registrationResult";
    
    public static final String SYSTEM_LEVEL_REPLY_CHANNEL = "systemLevelReplyChannel";
    
    
    public static final String PAF_ERROR_CODE = "pafErrorCode";
    public static final String PAF_ERROR_MESSAGE = "pafErrorMessage";
    
    private final PafMetadata metaData;
    
    public PafMessage(byte[] payload) {
        this(payload, new MessageHeaders(null));
    }
    
    public PafMessage(byte[] payload, Map<String, Object> headers) {
        this(payload, new MessageHeaders(headers));
    }

    public PafMessage(byte[] payload, MessageHeaders headers) {
        this(payload, headers, new PafMetadata());
    }
    
    public PafMessage(byte[] payload, MessageHeaders headers, PafMetadata metadata) {
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

    public ApplicationRoute getApplicationRoute() {
        return metaData.get(APPLICATION_ROUTE, ApplicationRoute.class);
    }

    public void setApplicationRoute(ApplicationRoute applicationRoute) {
        metaData.put(APPLICATION_ROUTE, applicationRoute);
    }
    
    public ApplicationProfileRoute getApplicationProfileRoute() {
        return metaData.get(APPLICATION_PROFILE_ROUTE, ApplicationProfileRoute.class);
    }

    public void setApplicationProfileRoute(ApplicationProfileRoute applicationProfileRoute) {
        metaData.put(APPLICATION_PROFILE_ROUTE, applicationProfileRoute);
    }
    
    public EndpointId getEndpointId() {
        return metaData.get(ENDPOINT_ID, EndpointId.class);
    }

    public void setEndpointId(EndpointId endpointId) {
        metaData.put(ENDPOINT_ID, endpointId);
    }
    
    public RegistrationResult getRegistrationResult() {
        return metaData.get(REGISTRATION_RESULT, RegistrationResult.class);
    }
    
    public void setRegistrationResult(RegistrationResult registrationResult) {
        metaData.put(REGISTRATION_RESULT, registrationResult);
    }
    
    public void setSystemLevelReplyChannel(MessageChannel messageChannel) {
        metaData.put(SYSTEM_LEVEL_REPLY_CHANNEL, messageChannel);
    }

    public MessageChannel getSystemLevelReplyChannel() {
        return metaData.get(SYSTEM_LEVEL_REPLY_CHANNEL, MessageChannel.class);
    }
    
    public void setPafErrorCode(PafErrorCode errorCode) {
        metaData.put(PAF_ERROR_CODE, errorCode);
    }
    
    public PafErrorCode getPafErrorCode() {
        return metaData.get(PAF_ERROR_CODE, PafErrorCode.class);
    }
    
    public void setPafErrorMessage(String message) {
        metaData.put(PAF_ERROR_MESSAGE, message);
    }
    
    public String getPafErrorMessage() {
        return metaData.get(PAF_ERROR_MESSAGE, String.class);
    }

    public boolean isOk() {
        return getPafErrorCode() == null || getPafErrorCode() == PafErrorCode.OK;
    }
    
    public PafMessagingException error(PafErrorCode errorCode, String errorMessage) {
        return new PafMessagingException(this, errorCode, errorMessage);
    }
}
