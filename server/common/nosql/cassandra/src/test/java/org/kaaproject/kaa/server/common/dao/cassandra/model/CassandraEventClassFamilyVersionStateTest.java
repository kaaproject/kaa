package org.kaaproject.kaa.server.common.dao.cassandra.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;

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