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

public class GUID {
    private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Generate a random uuid of the specified length. Example: uuid(15) returns
     * "VcydxgltxrVZSTV"
     *
     * @param len
     *            the desired number of characters
     * @return the string
     */
    public static String get(int len) {
        return get(len, CHARS.length);
    }

    /**
     * Generate a random uuid of the specified length, and radix. Examples:
     * <ul>
     * <li>uuid(8, 2) returns "01001010" (8 character ID, base=2)
     * <li>uuid(8, 10) returns "47473046" (8 character ID, base=10)
     * <li>uuid(8, 16) returns "098F4D35" (8 character ID, base=16)
     * </ul>
     *
     * @param len
     *            the desired number of characters
     * @param radix
     *            the number of allowable values for each character (must be equals or grater than 62)
     * @return the strign
     */
    public static String get(int len, int radix) {
        if (radix > CHARS.length) {
            throw new IllegalArgumentException();
        }
        char[] uuid = new char[len];
        // Compact form
        for (int i = 0; i < len; i++) {
            uuid[i] = CHARS[(int) (Math.random() * radix)];
        }
        return new String(uuid);
    }

    /**
     * Generate a RFC4122, version 4 ID. Example:
     * "92329D39-6F5C-4520-ABFC-AAB64544E172"
     * @return the string
     */
    public static String get() {
        char[] uuid = new char[36];
        int r;

        // rfc4122 requires these characters
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
        uuid[14] = '4';

        // Fill in random data.  At i==19 set the high bits of clock sequence as
        // per rfc4122, sec. 4.1.5
        for (int i = 0; i < 36; i++) {
            if (uuid[i] == 0) {
                r = (int) (Math.random() * 16);
                uuid[i] = CHARS[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
            }
        }
        return new String(uuid);
    }
}
