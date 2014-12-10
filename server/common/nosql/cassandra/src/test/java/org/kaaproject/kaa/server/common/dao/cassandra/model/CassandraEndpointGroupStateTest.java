package org.kaaproject.kaa.server.common.dao.cassandra.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;

import java.util.Random;

import static org.junit.Assert.*;

public class CassandraEndpointGroupStateTest {

    public static final Random RANDOM = new Random();
    public static final int LIMIT = 1000000;

    @Test
    public void EndpointStateTest() {
        EndpointGroupStateDto state = new EndpointGroupStateDto();

        String groupId = String.valueOf(RANDOM.nextInt(LIMIT));
        String filterId = String.valueOf(RANDOM.nextInt(LIMIT));
        String configId = String.valueOf(RANDOM.nextInt(LIMIT));
        state.setConfigurationId(configId);
        state.setProfileFilterId(filterId);
        state.setEndpointGroupId(groupId);

        CassandraEndpointGroupState stateOne = new CassandraEndpointGroupState(state);
        CassandraEndpointGroupState stateTwo = new CassandraEndpointGroupState();
        stateTwo.setConfigurationId(configId);
        stateTwo.setProfileFilterId(filterId);
        stateTwo.setEndpointGroupId(groupId);
        Assert.assertEquals(stateOne, stateTwo);
        Assert.assertEquals(stateOne.hashCode(), stateTwo.hashCode());
        Assert.assertEquals(stateOne.toDto(), stateTwo.toDto());
        Assert.assertEquals(stateOne.toString(), stateTwo.toString());
    }

    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(CassandraEndpointGroupState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}