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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEventClassFamilyVersionState;

public class CassandraEventClassFamilyVersionStateTest {

    @Test
    public void basicTest() {
        CassandraEventClassFamilyVersionState state = new CassandraEventClassFamilyVersionState();
        state.setEcfId("testID");
        state.setVersion(42);

        EventClassFamilyVersionStateDto dto = state.toDto();

        CassandraEventClassFamilyVersionState state2 = new CassandraEventClassFamilyVersionState(dto);

        Assert.assertEquals(state.getEcfId(), state2.getEcfId());
        Assert.assertEquals(state.getVersion(), state2.getVersion());
    }

    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraEventClassFamilyVersionState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}