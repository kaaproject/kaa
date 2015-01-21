/*
 * Copyright 2014 CyberVision, Inc.
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
 * Converts message data into objects specific to corresponding transport
 * channel
 * 
 * @author Andrew Shvayka
 *
 */
public interface MessageBuilder {

    /**
     * Convert message into objects specific to corresponding transport channel
     * 
     * @param messageData
     *            - data to convert
     * @param isEncrypted
     *            - information about encryption
     * @return result of conversion
     */
    Object[] build(byte[] messageData, boolean isEncrypted);

    /**
     * Convert message into objects specific to corresponding transport channel
     * 
     * @param messageData
     *            - data to convert
     * @param messageSignature
     *            - data signature
     * @param isEncrypted
     *            - information about encryption
     * @return result of conversion
     */
    Object[] build(byte[] messageData, byte[] messageSignature, boolean isEncrypted);
}
