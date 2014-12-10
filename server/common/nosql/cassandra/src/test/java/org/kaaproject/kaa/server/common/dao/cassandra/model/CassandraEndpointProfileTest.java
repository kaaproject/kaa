package org.kaaproject.kaa.server.common.dao.cassandra.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.*;

public class CassandraEndpointProfileTest {
    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraEndpointProfile.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}