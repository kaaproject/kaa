/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.common.dto;

import org.kaaproject.avro.ui.shared.RecordField;

import java.io.Serializable;

public class EndpointProfileRecordFieldDto implements Serializable {

    private static final long serialVersionUID = -3360718445400939841L;

    private RecordField profileRecord;
    private byte[] endpointKeyHash;
    private String ctlSchemaId;

    public RecordField getProfileRecord() {
        return profileRecord;
    }

    public void setProfileRecord(RecordField profileRecord) {
        this.profileRecord = profileRecord;
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }
}
