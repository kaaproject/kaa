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

import java.io.Serializable;

public class FormEnum implements Serializable {

    private static final long serialVersionUID = 816172078410943534L;
    
    private final String enumSymbol;
    private final String displayValue;
    
    public FormEnum(String enumSymbol, String displayValue) {
        this.enumSymbol = enumSymbol;
        this.displayValue = displayValue;
    }

    public String getEnumSymbol() {
        return enumSymbol;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((enumSymbol == null) ? 0 : enumSymbol.hashCode());
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
        FormEnum other = (FormEnum) obj;
        if (enumSymbol == null) {
            if (other.enumSymbol != null) {
                return false;
            }
        } else if (!enumSymbol.equals(other.enumSymbol)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return enumSymbol;
    }

}
