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

package org.kaaproject.kaa.server.transport.message;

import org.kaaproject.kaa.server.transport.session.SessionCreateListener;

/**
 * Represents a message that causes a session creation.
 * 
 * @author Andrew Shvayka
 *
 */
public interface SessionInitMessage extends Message, SessionCreateListener {

    /**
     * Return the encoded message data.
     * @return the encoded message data
     */
    byte[] getEncodedMessageData();

    /**
     * Return the encoded session key.
     * @return the encoded session key
     */
    byte[] getEncodedSessionKey();

    /**
     * Return the encoded session key signature.
     * @return the session key signature
     */
    byte[] getSessionKeySignature();

    /**
     * Returns a keep alive interval for this session
     * @return a keep alive interval
     */
    int getKeepAlive();

}
