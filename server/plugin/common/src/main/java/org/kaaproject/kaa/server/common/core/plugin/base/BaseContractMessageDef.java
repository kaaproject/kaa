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
package org.kaaproject.kaa.server.common.core.plugin.base;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;

public class BaseContractMessageDef implements ContractMessageDef {

    private static final long serialVersionUID = -7309289183547651496L;

    private final String fqn;
    private final int version;

    public BaseContractMessageDef(String fqn, int version) {
        super();
        this.fqn = fqn;
        this.version = version;
    }

    @Override
    public String getFqn() {
        return fqn;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        BaseContractMessageDef other = (BaseContractMessageDef) obj;
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

}