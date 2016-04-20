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

package org.kaaproject.kaa.avro.avrogen;

public class GenerationContext {
    private enum DirectionType {
        IN,
        OUT,
        INOUT
    }

    private final String parentName;
    private final String fieldName;
    private DirectionType direction;

    public GenerationContext(String parentName, String fieldName, String direction) {
        this.parentName = parentName;
        this.fieldName = fieldName;

        this.direction = DirectionType.INOUT;

        if (direction != null) {
            if (direction.equalsIgnoreCase("out")) {
                this.direction = DirectionType.OUT;
            } else if (direction.equalsIgnoreCase("in")) {
                this.direction = DirectionType.IN;
            }

        }
    }

    public void updateDirection(GenerationContext context) {
        if (direction != DirectionType.INOUT && context != null && direction != context.direction) {
            direction = DirectionType.INOUT;
        }
    }

    public String getParentName() {
        return parentName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isTypeOut() {
        return direction != DirectionType.IN;
    }

    public boolean isTypeIn() {
        return direction != DirectionType.OUT;
    }

    public boolean isTypeInOut() {
        return direction == DirectionType.INOUT;
    }
}
