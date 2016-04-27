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

/**
 * Converts the message data into objects specific to the corresponding transport channel.
 * 
 * @author Andrew Shvayka
 *
 */
public interface MessageBuilder {

    /**
     * Convert the message into objects specific to the corresponding transport channel.
     * 
     * @param messageData
     *            the data to convert
     * @param isEncrypted
     *            the data signature
     * @return the conversion result
     */
    Object[] build(byte[] messageData, boolean isEncrypted);

    /**
     * Convert the message into objects specific to the corresponding transport channel.
     * 
     * @param messageData
     *            the data to convert
     * @param messageSignature
     *            the data signature
     * @param isEncrypted
     *            information about encryption
     * @return the conversion result
     */
    Object[] build(byte[] messageData, byte[] messageSignature, boolean isEncrypted);
}
