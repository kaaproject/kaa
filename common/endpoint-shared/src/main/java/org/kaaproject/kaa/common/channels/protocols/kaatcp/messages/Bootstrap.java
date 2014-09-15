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

/**
 * Bootstrap message Class.
 * The Bootstrap message is used as intermediate class for decoding messages 
 * BootstrapResolve,BootstrapResponse
 * 
 * @author Andrey Panasenko
 *
 */
public class Bootstrap extends KaaSync {

    /**
     * Default constructor, used to create new Bootstrap message. 
     * @param isRequest
     */
    public Bootstrap(boolean isRequest) {
        super(isRequest, false, false);
        setKaaSyncMessageType(KaaSyncMessageType.BOOTSTRAP);
    }

    /**
     * Constructor for migrating from KaaSync to specific BootstrapResolve,BootstrapResponse
     * @param old KaaSync 
     */
    protected Bootstrap(KaaSync old) {
        super(old);
        setKaaSyncMessageType(KaaSyncMessageType.BOOTSTRAP);
    }
    
    /**
     * Default constructor.
     */
    public Bootstrap() {
        setKaaSyncMessageType(KaaSyncMessageType.BOOTSTRAP);
    }

}
