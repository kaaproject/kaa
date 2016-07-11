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

package org.kaaproject.kaa.server.operations.pojo;

import org.kaaproject.kaa.server.common.core.algorithms.delta.RawBinaryDelta;

/**
 * The Class for modeling of delta response. It is used to communicate with
 * {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService
 * DeltaService}
 *
 * @author ashvayka
 */

public class GetDeltaResponse {


    public static enum GetDeltaResponseType {
        DELTA,
        NO_DELTA,
        CONF_RESYNC
    }

    private final GetDeltaResponseType responseType;


    private final RawBinaryDelta delta;


    private String confSchema;


    public GetDeltaResponse(GetDeltaResponseType responseType) {
        this(responseType, null);
    }


    public GetDeltaResponse(GetDeltaResponseType responseType, RawBinaryDelta delta) {
        super();
        this.responseType = responseType;
        this.delta = delta;
    }

    public GetDeltaResponseType getResponseType() {
        return responseType;
    }


    public RawBinaryDelta getDelta() {
        return delta;
    }


    public void setConfSchema(String confSchema) {
        this.confSchema = confSchema;
    }


    public String getConfSchema() {
        return confSchema;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GetDeltaResponse [responseType=");
        builder.append(responseType);
        builder.append(", delta=");
        builder.append(delta);
        builder.append(", confSchema=");
        builder.append(confSchema);
        builder.append("]");
        return builder.toString();
    }
}
