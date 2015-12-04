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

import java.io.Serializable;

public class CTLDataDto implements Serializable {

    private static final long serialVersionUID = -9107671325547868060L;

    private String ctlSchemaId;
    private String body;

    public CTLDataDto() {
    }

    public CTLDataDto(String ctlSchemaId, String body) {
        this.ctlSchemaId = ctlSchemaId;
        this.body = body;
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLDataDto that = (CTLDataDto) o;

        if (ctlSchemaId != null ? !ctlSchemaId.equals(that.ctlSchemaId) : that.ctlSchemaId != null) return false;
        return body != null ? body.equals(that.body) : that.body == null;

    }

    @Override
    public int hashCode() {
        int result = ctlSchemaId != null ? ctlSchemaId.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLDataDto{" +
                "ctlSchemaId='" + ctlSchemaId + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
