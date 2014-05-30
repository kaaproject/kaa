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

package org.kaaproject.kaa.server.operations.pojo;

import org.kaaproject.kaa.server.operations.service.delta.RawBinaryDelta;


/**
 * The Class for modeling of delta response. It is used to communicate with
 * {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService
 * DeltaService}
 * 
 * @author ashvayka
 */

public class GetDeltaResponse{

    /**
     * The Enum GetDeltaResponseType.
     */
    public static enum GetDeltaResponseType {
        /** The delta. */
        DELTA,
        /** The no delta. */
        NO_DELTA,
        /** The conf resync. */
        CONF_RESYNC
    }

    /** The response type. */
    private GetDeltaResponseType responseType;

    /** The delta. */
    private RawBinaryDelta delta;

    /** The conf schema. */
    private String confSchema;

    /** The sequence number. */
    private int sequenceNumber;

    /**
     * Instantiates a new delta response.
     * 
     * @param responseType
     *            the response type
     */
    public GetDeltaResponse(GetDeltaResponseType responseType) {
        this(responseType, 0);
    }

    /**
     * Instantiates a new delta response.
     * 
     * @param responseType
     *            the response type
     * @param sequenceNumber
     *            the sequence number
     */
    public GetDeltaResponse(GetDeltaResponseType responseType, int sequenceNumber) {
        this(responseType, sequenceNumber, null);
    }

    /**
     * Instantiates a new delta response.
     * 
     * @param responseType
     *            the response type
     * @param sequenceNumber
     *            the sequence number
     * @param delta
     *            the delta
     */
    public GetDeltaResponse(GetDeltaResponseType responseType, int sequenceNumber, RawBinaryDelta delta) {
        super();
        this.responseType = responseType;
        this.delta = delta;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the response type.
     * 
     * @return the response type
     */
    public GetDeltaResponseType getResponseType() {
        return responseType;
    }

    /**
     * Gets the delta.
     * 
     * @return the delta
     */
    public RawBinaryDelta getDelta() {
        return delta;
    }

    /**
     * Gets the sequence number.
     * 
     * @return the sequence number
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the conf schema.
     * 
     * @param confSchema
     *            the new conf schema
     */
    public void setConfSchema(String confSchema) {
        this.confSchema = confSchema;
    }

    /**
     * Gets the conf schema.
     * 
     * @return the conf schema
     */
    public String getConfSchema() {
        return confSchema;
    }
}
