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

package org.kaaproject.kaa.server.operations.service.delta;

import java.io.IOException;
import java.util.Arrays;

import org.kaaproject.kaa.server.common.core.algorithms.delta.RawBinaryDelta;

public class TestRawBinaryDelta implements RawBinaryDelta {

    /**
     *
     */
    private static final long serialVersionUID = 6942802579307423075L;

    byte[] delta;

    public TestRawBinaryDelta(String delta) {
        this.delta = delta.getBytes();
    }

    @Override
    public byte[] getData() throws IOException {
        return delta;
    }

    @Override
    public boolean hasChanges() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(delta);
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
        TestRawBinaryDelta other = (TestRawBinaryDelta) obj;
        if (!Arrays.equals(delta, other.delta)) {
            return false;
        }
        return true;
    }

}