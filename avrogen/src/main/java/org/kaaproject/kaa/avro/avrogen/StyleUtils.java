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

  /**
   * Convert to lower underscore format.
   *
   * @param   camelCaseName the name in camel case format
   * @return  the string in lower underscore format
   */
  public static String toLowerUnderScore(String camelCaseName) {
    StringBuilder convertedName = new StringBuilder();

    for (int i = 0; i < camelCaseName.length(); ++i) {
      char character = camelCaseName.charAt(i);
      if (Character.isUpperCase(character)) {
        character = Character.toLowerCase(character);
        if (convertedName.length() > 0 && ((i + 1) < camelCaseName.length())
            && (Character.isLowerCase(camelCaseName.charAt(i + 1))
            || Character.isLowerCase(camelCaseName.charAt(i - 1)))) {
          convertedName.append("_");
        }
        convertedName.append(character);
      } else {
        convertedName.append(character);
      }
    }

    return convertedName.toString();
  }

  /**
   * Convert to upper underscore format.
   *
   * @param   camelCaseName the input name
   * @return  the string in upper underscore format
   */
  public static String toUpperUnderScore(String camelCaseName) {
    StringBuilder convertedName = new StringBuilder();

    for (int i = 0; i < camelCaseName.length(); ++i) {
      char character = camelCaseName.charAt(i);
      if (Character.isUpperCase(character)) {
        if (convertedName.length() > 0 && ((i + 1) < camelCaseName.length())
                && (Character.isLowerCase(camelCaseName.charAt(i + 1))
                || Character.isLowerCase(camelCaseName.charAt(i - 1)))) {
          convertedName.append("_");
        }
        convertedName.append(character);
      } else {
        convertedName.append(Character.toUpperCase(character));
      }
    }

    return convertedName.toString();
  }

  /**
   * Fix camel humps.
   * @param   name the input name
   * @return  the string with fixed camel humps
   */
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
