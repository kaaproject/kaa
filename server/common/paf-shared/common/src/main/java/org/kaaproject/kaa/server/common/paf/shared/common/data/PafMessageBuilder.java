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

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.Assert;

public final class PafMessageBuilder {

    private final byte[] payload;

    private MessageHeaderAccessor headerAccessor;
    private PafMetadata metaData;

    private PafMessageBuilder(Message<byte[]> originalMessage) {
        Assert.notNull(originalMessage, "Message must not be null");
        this.payload = originalMessage.getPayload();
        this.headerAccessor = new MessageHeaderAccessor(originalMessage);
        if (originalMessage instanceof PafMessage) {
            this.metaData = ((PafMessage)originalMessage).getMetaData();
        } else {
            this.metaData = new PafMetadata();
        }
    }

    private PafMessageBuilder(byte[] payload, MessageHeaderAccessor accessor, PafMetadata metaData) {
        Assert.notNull(payload, "Payload must not be null");
        Assert.notNull(accessor, "MessageHeaderAccessor must not be null");
        Assert.notNull(metaData, "MetaData must not be null");
        this.payload = payload;
        this.headerAccessor = accessor;
        this.metaData = metaData;
    }


    /**
     * Set the message headers to use by providing a {@code MessageHeaderAccessor}.
     * @param accessor the headers to use
     */
    public PafMessageBuilder setHeaders(MessageHeaderAccessor accessor) {
        Assert.notNull(accessor, "MessageHeaderAccessor must not be null");
        this.headerAccessor = accessor;
        return this;
    }

    /**
     * Set the value for the given header name. If the provided value is {@code null},
     * the header will be removed.
     */
    public PafMessageBuilder setHeader(String headerName, Object headerValue) {
        this.headerAccessor.setHeader(headerName, headerValue);
        return this;
    }

    /**
     * Set the value for the given header name only if the header name is not already
     * associated with a value.
     */
    public PafMessageBuilder setHeaderIfAbsent(String headerName, Object headerValue) {
        this.headerAccessor.setHeaderIfAbsent(headerName, headerValue);
        return this;
    }

    /**
     * Removes all headers provided via array of 'headerPatterns'. As the name suggests
     * the array may contain simple matching patterns for header names. Supported pattern
     * styles are: "xxx*", "*xxx", "*xxx*" and "xxx*yyy".
     */
    public PafMessageBuilder removeHeaders(String... headerPatterns) {
        this.headerAccessor.removeHeaders(headerPatterns);
        return this;
    }
    /**
     * Remove the value for the given header name.
     */
    public PafMessageBuilder removeHeader(String headerName) {
        this.headerAccessor.removeHeader(headerName);
        return this;
    }

    /**
     * Copy the name-value pairs from the provided Map. This operation will overwrite any
     * existing values. Use { {@link #copyHeadersIfAbsent(Map)} to avoid overwriting
     * values. Note that the 'id' and 'timestamp' header values will never be overwritten.
     */
    public PafMessageBuilder copyHeaders(Map<String, ?> headersToCopy) {
        this.headerAccessor.copyHeaders(headersToCopy);
        return this;
    }

    /**
     * Copy the name-value pairs from the provided Map. This operation will <em>not</em>
     * overwrite any existing values.
     */
    public PafMessageBuilder copyHeadersIfAbsent(Map<String, ?> headersToCopy) {
        this.headerAccessor.copyHeadersIfAbsent(headersToCopy);
        return this;
    }

    public PafMessageBuilder setReplyChannel(MessageChannel replyChannel) {
        this.headerAccessor.setReplyChannel(replyChannel);
        return this;
    }

    public PafMessageBuilder setReplyChannelName(String replyChannelName) {
        this.headerAccessor.setReplyChannelName(replyChannelName);
        return this;
    }

    public PafMessageBuilder setErrorChannel(MessageChannel errorChannel) {
        this.headerAccessor.setErrorChannel(errorChannel);
        return this;
    }

    public PafMessageBuilder setErrorChannelName(String errorChannelName) {
        this.headerAccessor.setErrorChannelName(errorChannelName);
        return this;
    }
    
    public PafMessageBuilder setMetadata(PafMetadata metaData) {
        Assert.notNull(metaData, "MetaData must not be null");
        this.metaData = metaData;
        return this;
    }

    @SuppressWarnings("unchecked")
    public PafMessage build() {
        MessageHeaders headersToUse = this.headerAccessor.toMessageHeaders();
        return new PafMessage(this.payload, headersToUse, this.metaData);
    }


    /**
     * Create a builder for a new {@link Message} instance pre-populated with all of the
     * headers copied from the provided message. The payload of the provided Message will
     * also be used as the payload for the new message.
     * @param message the Message from which the payload and all headers will be copied
     */
    public static PafMessageBuilder fromMessage(Message<byte[]> message) {
        return new PafMessageBuilder(message);
    }

    /**
     * Create a new builder for a message with the given payload.
     * @param payload the payload
     */
    public static PafMessageBuilder withPayload(byte[] payload) {
        return new PafMessageBuilder(payload, new MessageHeaderAccessor(), new PafMetadata());
    }

    /**
     * A shortcut factory method for creating a message with the given payload
     * and {@code MessageHeaders}.
     * <p><strong>Note:</strong> the given {@code MessageHeaders} instance is used
     * directly in the new message, i.e. it is not copied.
     * @param payload the payload to use (never {@code null})
     * @param messageHeaders the headers to use (never {@code null})
     * @param metaData the meta data to use (never {@code null})
     * @return the created message
     */
    @SuppressWarnings("unchecked")
    public static PafMessage createMessage(byte[] payload, 
            MessageHeaders messageHeaders, PafMetadata metaData) {
        Assert.notNull(payload, "Payload must not be null");
        Assert.notNull(messageHeaders, "MessageHeaders must not be null");
        Assert.notNull(metaData, "MetaData must not be null");
        return new PafMessage(payload, messageHeaders, metaData);
    }

}
