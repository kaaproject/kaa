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

package org.kaaproject.kaa.demo.smarthousedemo.command;

public class EndpointCommandKey {
    
    String fqn;
    String endpontKey;
    
    public EndpointCommandKey(String fqn, String endpontKey) {
        this.fqn = fqn;
        this.endpontKey = endpontKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((endpontKey == null) ? 0 : endpontKey.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
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
        EndpointCommandKey other = (EndpointCommandKey) obj;
        if (endpontKey == null) {
            if (other.endpontKey != null)
                return false;
        } else if (!endpontKey.equals(other.endpontKey))
            return false;
        if (fqn == null) {
            if (other.fqn != null)
                return false;
        } else if (!fqn.equals(other.fqn))
            return false;
        return true;
    }

}