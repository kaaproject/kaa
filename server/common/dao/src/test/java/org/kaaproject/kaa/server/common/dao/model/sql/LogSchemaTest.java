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

package org.kaaproject.kaa.server.common.dao.model.sql;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

public class LogSchemaTest {

    private static final long TEST_ID = 1L;

    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(LogSchema.class).usingGetClass().verify();
        }

    @Test
    public void basicLogSchemaTest() {
        LogSchema logSchema = new LogSchema(TEST_ID);
        Assert.assertEquals((Long) TEST_ID, logSchema.getId());
    }
}
