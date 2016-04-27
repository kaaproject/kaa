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

package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;

public class ValidatorTest {

    @Test
    public void isValidSqlObjectTest() {
        HasId hasId = new HasId() {
            private String id;

            @Override
            public String getId() {
                return id;
            }

            @Override
            public void setId(String id) {
                this.id = id;
            }
        };
        hasId.setId("not number");
        boolean valid = Validator.isValidSqlObject(hasId);
        Assert.assertFalse(valid);
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateSqlIdTest() {
        Validator.validateSqlId("", "err msg");
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateStringTest() {
        Validator.validateString(null, "err msg");
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateObjectTest() {
        Validator.validateObject(null, "err msg");
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateSqlObjectTest() {
        Validator.validateSqlObject(null, "err msg");
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateHashTest() {
        Validator.validateHash(null, "err msg");
    }
}
