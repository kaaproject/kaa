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

package org.kaaproject.kaa.server.common.core.algorithms.delta;

import java.io.IOException;

/**
 * Simple container for resync delta encoded using base schema
 * 
 * @author Andrew Shvayka
 *
 */
public class BaseBinaryDelta implements RawBinaryDelta{

    private static final long serialVersionUID = -3689001649508558531L;
    
    private final byte[] data;
    
    public BaseBinaryDelta(byte[] data) {
        super();
        this.data = data;
    }

    @Override
    public byte[] getData() throws IOException {
        return data;
    }

    @Override
    public boolean hasChanges() {
        return true;
    }

}
