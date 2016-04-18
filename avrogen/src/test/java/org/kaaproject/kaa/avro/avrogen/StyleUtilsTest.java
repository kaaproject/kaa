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

import org.junit.Assert;
import org.junit.Test;


public class StyleUtilsTest {
    @Test
    public void testToLowerUnderScore() {
        String pattern1 = "endpoint_key_hash";
        Assert.assertTrue(StyleUtils.toLowerUnderScore("endpointKeyHash").equals(pattern1));

        String pattern2 = "event_class_fqn";
        Assert.assertTrue(StyleUtils.toLowerUnderScore("eventClassFQN").equals(pattern2));
    }

    @Test
    public void testToUpperUnderScore() {
        String pattern1 = "ENDPOINT_KEY_HASH";
        Assert.assertTrue(StyleUtils.toUpperUnderScore("endpointKeyHash").equals(pattern1));

        String pattern2 = "EVENT_CLASS_FQN";
        Assert.assertTrue(StyleUtils.toUpperUnderScore("eventClassFQN").equals(pattern2));
    }

    @Test
    public void testRemovePackageName() {
        String longName = "org.kaaproject.kaa.common.endpoint.gen";
        Assert.assertTrue(StyleUtils.removePackageName(longName).equals("gen"));
    }
}
