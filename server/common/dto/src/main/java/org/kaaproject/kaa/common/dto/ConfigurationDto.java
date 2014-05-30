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

package org.kaaproject.kaa.common.dto;

import java.io.UnsupportedEncodingException;

public class ConfigurationDto extends AbstractStructureDto {

    private static final String UTF8 = "UTF-8";
    private static final long serialVersionUID = 1766336602276590007L;

    private String protocolSchema;

    public String getProtocolSchema() {
        return protocolSchema;
    }

    public void setProtocolSchema(String protocolBody) {
        this.protocolSchema = protocolBody;
    }

    public byte[] getBinaryBody() {
        if (body != null) {
            try {
                return body.getBytes(UTF8);
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        return null;
    }

    public void setBinaryBody(byte[] binaryBody) {
        if (binaryBody != null) {
            try {
                this.body = new String(binaryBody, UTF8);
            } catch (UnsupportedEncodingException e) {
                body = null;
            }
        } else {
            body = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ConfigurationDto that = (ConfigurationDto) o;

        if (protocolSchema != null ? !protocolSchema.equals(that.protocolSchema) : that.protocolSchema != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (protocolSchema != null ? protocolSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationDto{" + super.toString() +
                "protocolSchema='" + protocolSchema + '\'' +
                '}';
    }
}
