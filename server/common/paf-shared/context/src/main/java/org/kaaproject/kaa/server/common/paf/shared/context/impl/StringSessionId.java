/**
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

package org.kaaproject.kaa.server.common.paf.shared.context.impl;

import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;

public class StringSessionId implements SessionId {

    private final String strSessionId;
    
    public StringSessionId(String strSessionId) {
        super();
        this.strSessionId = strSessionId;
    }

    public String getStrSessionId() {
        return strSessionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((strSessionId == null) ? 0 : strSessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StringSessionId other = (StringSessionId) obj;
        if (strSessionId == null) {
            if (other.strSessionId != null)
                return false;
        } else if (!strSessionId.equals(other.strSessionId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StringSessionId [strSessionId=" + strSessionId + "]";
    }
    
}
