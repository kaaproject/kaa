package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class CassandraEndpointConfigurationTest {

    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraEndpointConfiguration.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}