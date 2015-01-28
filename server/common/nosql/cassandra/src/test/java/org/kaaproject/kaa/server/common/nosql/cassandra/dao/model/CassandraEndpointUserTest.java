package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class CassandraEndpointUserTest {
    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraEndpointUser.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}