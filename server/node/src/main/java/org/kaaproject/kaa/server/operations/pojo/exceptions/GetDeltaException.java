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

package org.kaaproject.kaa.server.operations.pojo.exceptions;

/**
 * Class for modeling exception of delta request.
 * It is used to communicate with {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService DeltaService}
 *
 * @author ashvayka
 */
public class GetDeltaException extends Exception {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new delta exception.
     *
     * @param message the message
     */
    public GetDeltaException(String message){
        super(message);
    }
    
    /**
     * Instantiates a new delta exception.
     *
     * @param e the e
     */
    public GetDeltaException(Exception e){
        super(e);
    }

    
}
