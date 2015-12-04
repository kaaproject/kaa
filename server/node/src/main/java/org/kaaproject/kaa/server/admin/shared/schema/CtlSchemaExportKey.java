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

package org.kaaproject.kaa.server.admin.shared.schema;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;

public class CtlSchemaExportKey implements Serializable {
    
    private static final long serialVersionUID = -1732926678025232971L;
    
    public static final String CTL_EXPORT_KEY_PARAMETER = "ctlExportKey";
    
    private String fqn;
    private int version;
    private CTLSchemaExportMethod exportMethod;
    
    public CtlSchemaExportKey() {
        super();
    }
    
    public CtlSchemaExportKey(String fqn, int version,
            CTLSchemaExportMethod exportMethod) {
        super();
        this.fqn = fqn;
        this.version = version;
        this.exportMethod = exportMethod;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
                + ((exportMethod == null) ? 0 : exportMethod.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + version;
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
        if (exportMethod != other.exportMethod) {
            return false;
        }
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CtlSchemaExportKey [fqn=");
        builder.append(fqn);
        builder.append(", version=");
        builder.append(version);
        builder.append(", exportMethod=");
        builder.append(exportMethod);
        builder.append("]");
        return builder.toString();
    }
    
}
