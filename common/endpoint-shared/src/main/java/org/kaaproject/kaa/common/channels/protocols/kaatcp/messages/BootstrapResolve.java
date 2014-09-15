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
package org.kaaproject.kaa.common.channels.protocols.kaatcp.messages;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;

/**
 * Bootstrap Resolve Class.
 * Extends KaaSync message, {@link KaaSync} with:
 * 
 * Application_token UTF-8 String, 
 * Length of Application_token is calculated from all frame length minus 12 byte of variable header.
 * 
 * @author Andrey Panasenko
 *
 */
public class BootstrapResolve extends Bootstrap {

    private String applicationToken;

    /**
     * Constructor used to create new BootstrapResolve message.
     * 
     * @param applicationToken String 
     */
    public BootstrapResolve(String applicationToken) {
        super(true);
        setApplicationToken(applicationToken);
    }

    /**
     * Constructor for migrating from KaaSync to BootstrapResolve
     * @param old KaaSync 
     * @throws KaatcpProtocolException
     */
    protected BootstrapResolve(KaaSync old) 
            throws KaaTcpProtocolException {
        super(old);
        setRequest(true);
        decodeApplicationToken();
    }
    
    /**
     *  Default constructor.
     */
    public BootstrapResolve() {
        setRequest(true);
    }

    /**
     * Application token getter.
     * @return String applicationToken
     */
    public String getApplicationToken() {
        return applicationToken;
    }

    /**
     * Application token setter.
     * @param applicationToken String
     */
    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
        remainingLength = KAASYNC_VERIABLE_HEADER_LENGTH_V1 + applicationToken.getBytes().length;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        super.pack();
        buffer.put(applicationToken.getBytes());
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#decode()
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        super.decode();
        decodeApplicationToken();
    }
    
    /**
     * Decode application token from buffer.
     */
    private void decodeApplicationToken() {
        int appTokenLength = buffer.remaining();
        if (appTokenLength > 0) {
            byte[] appTokenBytes = new byte[appTokenLength];
            buffer.get(appTokenBytes);
            applicationToken = new String(appTokenBytes);
        }
    }
}
