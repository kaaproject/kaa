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

package org.kaaproject.kaa.common.channels;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Andrey Panasenko
 *
 */
public class ParsingExceptionTest {

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.ParsingException#ParsingException(java.lang.String)}.
     */
    @Test
    public void testParsingExceptionString() {
        String testMessage = new String("test");
        try {
            throw new ParsingException(testMessage);
        } catch (ParsingException e) {
            assertEquals(testMessage, e.getMessage());
        }
    }


}
