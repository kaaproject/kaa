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

public class StyleUtils {

    private StyleUtils() {
    }

    public static String toLowerUnderScore(String camelCaseName) {
        StringBuilder convertedName = new StringBuilder();

        for (int i = 0; i < camelCaseName.length(); ++i) {
            char c = camelCaseName.charAt(i);
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
                if (convertedName.length() > 0 && ((i + 1) < camelCaseName.length())
                        && (Character.isLowerCase(camelCaseName.charAt(i + 1))
                                || Character.isLowerCase(camelCaseName.charAt(i - 1)))) {
                    convertedName.append("_");
                }
                convertedName.append(c);
            } else {
                convertedName.append(c);
            }
        }

        return convertedName.toString();
    }

    public static String toUpperUnderScore(String camelCaseName) {
        StringBuilder convertedName = new StringBuilder();

        for (int i = 0; i < camelCaseName.length(); ++i) {
            char c = camelCaseName.charAt(i);
            if (Character.isUpperCase(c)) {
                if (convertedName.length() > 0  && ((i + 1) < camelCaseName.length()) &&
                        (Character.isLowerCase(camelCaseName.charAt(i + 1))
                                || Character.isLowerCase(camelCaseName.charAt(i - 1)))) {
                    convertedName.append("_");
                }
                convertedName.append(c);
            } else {
                convertedName.append(Character.toUpperCase(c));
            }
        }

        return convertedName.toString();
    }

    public static String fixCamelHumps(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name couldn't be null or empty");
        }
        if (Character.isLowerCase(name.charAt(0))) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static String removePackageName(String fullClassName) {
        int index = fullClassName.lastIndexOf('.');
        return index == -1 ? fullClassName : fullClassName.substring(index + 1);
    }
}
