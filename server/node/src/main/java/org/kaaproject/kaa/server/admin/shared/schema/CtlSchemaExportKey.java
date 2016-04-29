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

package org.kaaproject.kaa.server.admin.shared.schema;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;

public class CtlSchemaExportKey implements Serializable {
    
    private static final long serialVersionUID = -1732926678025232971L;
    
    private String ctlSchemaId;
    private CTLSchemaExportMethod exportMethod;
    
    public CtlSchemaExportKey() {
        super();
    }
    
    public CtlSchemaExportKey(String ctlSchemaId,
            CTLSchemaExportMethod exportMethod) {
        super();
        this.ctlSchemaId = ctlSchemaId;
        this.exportMethod = exportMethod;
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public CTLSchemaExportMethod getExportMethod() {
        return exportMethod;
    }

    public void setExportMethod(CTLSchemaExportMethod exportMethod) {
        this.exportMethod = exportMethod;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((ctlSchemaId == null) ? 0 : ctlSchemaId.hashCode());
        result = prime * result
                + ((exportMethod == null) ? 0 : exportMethod.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CtlSchemaExportKey other = (CtlSchemaExportKey) obj;
        if (ctlSchemaId == null) {
            if (other.ctlSchemaId != null) {
                return false;
            }
        } else if (!ctlSchemaId.equals(other.ctlSchemaId)) {
            return false;
        }
        if (exportMethod != other.exportMethod) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CtlSchemaExportKey [ctlSchemaId=");
        builder.append(ctlSchemaId);
        builder.append(", exportMethod=");
        builder.append(exportMethod);
        builder.append("]");
        return builder.toString();
    }

}
