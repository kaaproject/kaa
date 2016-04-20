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

package org.kaaproject.kaa.server.admin.client.util;

public class SchemaErrorMessageCustomizer implements ErrorMessageCustomizer {

    private static final String  LEFT_SQUARE_BRACKET = "[";
    private static final String  RIGHT_SQUARE_BRACKET = "]";
    private static final String  SEMICOLON = ";";
    
    @Override
    public String customizeErrorMessage(Throwable caught) {
        String origMessage = caught.getLocalizedMessage();        
        String errorMessage = null;
        int leftSquareBracketIndex = origMessage.indexOf(LEFT_SQUARE_BRACKET);
        int rightSquareBracketIndex = -1;
        if (leftSquareBracketIndex != -1) {
            rightSquareBracketIndex = origMessage.indexOf(RIGHT_SQUARE_BRACKET, leftSquareBracketIndex);
        }        
        if (rightSquareBracketIndex != -1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Incorrect json schema: Please check your schema at");
            String[] array = origMessage.substring(leftSquareBracketIndex, rightSquareBracketIndex).split(SEMICOLON);
            if (array != null && array.length == 2) {
                builder.append(array[1]);
                errorMessage = builder.toString();
            }
        } 
        return errorMessage;
    }

}
