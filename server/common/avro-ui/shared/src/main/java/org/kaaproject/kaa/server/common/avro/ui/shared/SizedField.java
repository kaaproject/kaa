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

package org.kaaproject.kaa.server.common.avro.ui.shared;

public abstract class SizedField extends FormField {

    private static final long serialVersionUID = 6539576598668221454L;
    
    private static final int DEFAULT_MAX_LENGTH = 255;
    
    private Integer maxLength;
    
    public SizedField() {
        super();
    }
    
    public SizedField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }
    
    public int getMaxLength() {
        if (maxLength != null) {
            return maxLength.intValue();
        }
        else {
            return DEFAULT_MAX_LENGTH;
        }
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = Integer.valueOf(maxLength);
    }

}
